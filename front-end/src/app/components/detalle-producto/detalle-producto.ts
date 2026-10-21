import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { Producto } from '../../models/product';
import { ImagenProducto } from '../imagen-producto/imagen-producto';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [RouterLink, IndicadorCarga, MensajeError, ImagenProducto], selector: 'app-detalle-producto', templateUrl: './detalle-producto.html', styleUrl: './detalle-producto.scss' })
export class DetalleProducto implements OnInit {
  private readonly ruta = inject(ActivatedRoute); private readonly catalogo = inject(CatalogoService); private readonly carrito = inject(CarritoService);
  readonly producto = signal<Producto | null>(null); readonly cargando = signal(true); readonly error = signal(''); readonly aviso = signal('');
  ngOnInit(): void { const id = Number(this.ruta.snapshot.paramMap.get('id')); if (!Number.isInteger(id) || id < 1) { this.error.set('Producto no válido.'); this.cargando.set(false); return; }
    this.catalogo.producto(id).subscribe({ next: p => { this.producto.set(p); this.cargando.set(false); }, error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); } }); }
  agregar(): void { const p = this.producto(); if (!p) return; try { this.carrito.agregar(p).subscribe({ next: () => this.aviso.set('Producto agregado al carrito.'), error: e => this.error.set(mensajeError(e)) }); } catch (e) { this.error.set(mensajeError(e)); } }
}
