import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { esReintentable, mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { PedidosService } from '../../core/pedidos.service';
import { estadoStock } from '../../core/stock';
import { Inventario, Pedido } from '../../models/product';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';

// Página inicial del panel: indicadores de ventas y stock calculados con endpoints existentes de la API.
@Component({
  imports: [DatePipe, DecimalPipe, RouterLink, EtiquetaEstado, IndicadorCarga, MensajeError],
  selector: 'app-resumen-gestion',
  templateUrl: './resumen-gestion.html',
  styleUrl: './resumen-gestion.scss',
})
export class ResumenGestion implements OnInit {
  private readonly pedidosServicio = inject(PedidosService);
  private readonly gestion = inject(GestionService);
  readonly pedidos = signal<Pedido[]>([]);
  readonly totalVendido = signal(0);
  readonly porReponer = signal<Inventario[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly reintentable = signal(false);

  readonly confirmados = computed(() => this.pedidos().filter(p => p.estado === 'CONFIRMADO').length);
  readonly cancelados = computed(() => this.pedidos().filter(p => p.estado === 'CANCELADO').length);
  readonly agotados = computed(() => this.porReponer().filter(i => i.producto.stock === 0).length);
  readonly ultimos = computed(() => [...this.pedidos()].sort((a, b) => b.fecha.localeCompare(a.fecha)).slice(0, 5));
  readonly reponer = computed(() => [...this.porReponer()].sort((a, b) => a.producto.stock - b.producto.stock));

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.error.set(''); this.reintentable.set(false); this.cargando.set(true);
    forkJoin({
      pedidos: this.pedidosServicio.todos(),
      total: this.pedidosServicio.totalPorEstado('CONFIRMADO'),
      porReponer: this.gestion.inventariosBajoMinimo(),
    }).subscribe({
      next: ({ pedidos, total, porReponer }) => {
        this.pedidos.set(pedidos); this.totalVendido.set(Number(total) || 0); this.porReponer.set(porReponer);
        this.cargando.set(false);
      },
      error: e => { this.error.set(mensajeError(e)); this.reintentable.set(esReintentable(e)); this.cargando.set(false); }
    });
  }

  estado(i: Inventario): string { return estadoStock(i.producto.stock, i.stockMinimo); }
}
