import { DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AvisosService } from '../../services/avisos.service';
import { CarritoService } from '../../services/carrito.service';
import { CatalogoService } from '../../services/catalogo.service';
import { esReintentable, mensajeError } from '../../shared/errores';
import { Producto } from '../../models/product';
import { ImagenProducto } from '../../shared/imagen-producto/imagen-producto';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
import { SelectorCantidad } from '../../shared/selector-cantidad/selector-cantidad';
@Component({ imports: [DecimalPipe, RouterLink, IndicadorCarga, MensajeError, ImagenProducto, SelectorCantidad], selector: 'app-detalle-producto', templateUrl: './detalle-producto.html', styleUrl: './detalle-producto.scss' })
export class DetalleProducto implements OnInit {
  private readonly ruta = inject(ActivatedRoute); private readonly catalogo = inject(CatalogoService); private readonly carrito = inject(CarritoService); private readonly avisos = inject(AvisosService);
  readonly producto = signal<Producto | null>(null); readonly cargando = signal(true); readonly error = signal(''); readonly reintentable = signal(false);
  readonly cantidad = signal(1);
  // Falso si la categoría del producto está inactiva: se informa en lugar de ofrecer agregarlo al carrito.
  readonly disponible = signal(true);
  ngOnInit(): void { this.cargar(); }
  cargar(): void { const id = Number(this.ruta.snapshot.paramMap.get('id')); this.error.set(''); this.reintentable.set(false); if (!Number.isInteger(id) || id < 1) { this.error.set('Producto no válido.'); this.cargando.set(false); return; }
    this.cargando.set(true);
    forkJoin({ producto: this.catalogo.producto(id), categorias: this.catalogo.categorias() }).subscribe({ next: ({ producto: p, categorias }) => { this.producto.set(p); this.disponible.set(p.categoriaId == null || categorias.some(c => c.id === p.categoriaId)); this.cargando.set(false); }, error: e => { this.error.set(mensajeError(e)); this.reintentable.set(esReintentable(e)); this.cargando.set(false); } }); }
  maximo(p: Producto): number { return Math.min(p.stock, 100); }
  agregar(): void { const p = this.producto(); if (!p) return; const cantidad = this.cantidad(); this.reintentable.set(false); this.error.set('');
    try { this.carrito.agregar(p, cantidad).subscribe({ next: () => { this.cantidad.set(1); this.avisos.exito(`${cantidad} × ${p.nombre} se agregó al carrito.`, 4000, { texto: 'Ver carrito', ruta: '/carrito' }); }, error: e => this.error.set(mensajeError(e)) }); } catch (e) { this.error.set(mensajeError(e)); } }
}
