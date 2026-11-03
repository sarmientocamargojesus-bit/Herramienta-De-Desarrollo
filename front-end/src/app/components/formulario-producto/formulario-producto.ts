import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { noSoloEspacios } from '../../core/validadores';
import { Categoria, Producto } from '../../models/product';
import { ImagenProducto } from '../imagen-producto/imagen-producto';
import { MensajeError } from '../mensaje-error/mensaje-error';

@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError, ImagenProducto], selector: 'app-formulario-producto', templateUrl: './formulario-producto.html', styleUrl: './formulario-producto.scss' })
export class FormularioProducto implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly servicio = inject(CatalogoService);
  readonly id = signal<number | null>(Number(this.ruta.snapshot.paramMap.get('id')) || null);
  readonly categorias = signal<Categoria[]>([]);
  readonly producto = signal<Producto | null>(null);
  readonly archivo = signal<File | null>(null);
  readonly imagenVersion = signal(0);
  readonly tieneImagen = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly enviando = signal(false);
  readonly form = this.fb.nonNullable.group({ nombre: ['', [Validators.required, noSoloEspacios]], descripcion: [''], precio: [0, [Validators.required, Validators.min(0.01)]], stock: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]], categoria: ['', Validators.required] });

  ngOnInit(): void {
    const id = this.id();
    if (id) {
      forkJoin({ producto: this.servicio.producto(id), categorias: this.servicio.categorias() }).subscribe({
        next: ({ producto, categorias }) => { this.producto.set(producto); this.categorias.set(categorias); this.form.patchValue(producto); },
        error: error => this.error.set(mensajeError(error))
      });
    } else {
      this.servicio.categorias().subscribe({ next: categorias => this.categorias.set(categorias), error: error => this.error.set(mensajeError(error)) });
    }
  }

  seleccionarArchivo(evento: Event): void {
    const archivo = (evento.target as HTMLInputElement).files?.[0] ?? null;
    if (!archivo) { this.archivo.set(null); return; }
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(archivo.type) || archivo.size > 2_000_000 || archivo.size === 0) {
      this.archivo.set(null);
      (evento.target as HTMLInputElement).value = '';
      this.error.set('Selecciona una imagen PNG, JPEG o WebP de hasta 2 MB.');
      return;
    }
    this.error.set('');
    this.archivo.set(archivo);
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.enviando.set(true);
    this.error.set('');
    const datos = this.form.getRawValue();
    const id = this.id();
    const peticion = id ? this.servicio.actualizarProducto(id, datos, this.producto()?.stock) : this.servicio.crearProducto(datos);
    peticion.subscribe({
      next: producto => {
        this.id.set(producto.id);
        this.producto.set(producto);
        const archivo = this.archivo();
        if (archivo) this.subirImagen(producto.id, archivo);
        else { this.enviando.set(false); void this.router.navigateByUrl('/gestion/productos'); }
      },
      error: error => { this.error.set(mensajeError(error)); this.enviando.set(false); }
    });
  }

  quitarImagen(): void {
    const id = this.id();
    if (!id || !this.tieneImagen()) return;
    this.enviando.set(true);
    this.servicio.quitarImagen(id).subscribe({
      next: () => { this.imagenVersion.update(version => version + 1); this.tieneImagen.set(false); this.aviso.set('La imagen se quitó. Se mostrará la ilustración de la categoría.'); this.enviando.set(false); },
      error: error => { this.error.set(mensajeError(error)); this.enviando.set(false); }
    });
  }

  private subirImagen(id: number, archivo: File): void {
    this.servicio.subirImagen(id, archivo).subscribe({
      next: () => { this.archivo.set(null); this.imagenVersion.update(version => version + 1); this.enviando.set(false); void this.router.navigateByUrl('/gestion/productos'); },
      error: error => { this.error.set('El producto se guardó, pero no se pudo subir la imagen. Puedes volver a intentarlo sin crear otro producto. ' + mensajeError(error)); this.enviando.set(false); }
    });
  }
}
