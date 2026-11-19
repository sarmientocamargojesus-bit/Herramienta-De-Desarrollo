import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { mensajeError } from '../../shared/errores';
import { AvisosService } from '../../services/avisos.service';
import { PedidosService } from '../../services/pedidos.service';
import { DetallePedido as Linea, Pedido } from '../../models/product';
import { nombreEstado, SIGUIENTE_ESTADO } from '../../shared/estados-pedido';
import { EtiquetaEstado } from '../../shared/etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports: [EtiquetaEstado, DatePipe, DecimalPipe, RouterLink, MensajeError], selector: 'app-detalle-pedido', templateUrl: './detalle-pedido.html', styleUrl: './detalle-pedido.scss' })
export class DetallePedido implements OnInit {
  private readonly ruta = inject(ActivatedRoute); private readonly servicio = inject(PedidosService); private readonly avisos = inject(AvisosService);
  // El mismo detalle se abre desde /gestion/pedidos/:id y desde /mis-pedidos/:id; el regreso depende de ese origen.
  readonly enGestion = this.ruta.snapshot.pathFromRoot.flatMap(r => r.url.map(s => s.path))[0] === 'gestion';
  readonly pedido = signal<Pedido|null>(null); readonly detalles = signal<Linea[]>([]); readonly error = signal(''); readonly cargando = signal(true);
  readonly avanzando = signal(false);
  // Solo en Gestión y solo si el estado actual tiene un paso siguiente (no en PENDIENTE, ENTREGADO ni CANCELADO).
  readonly siguiente = computed(() => { const p = this.pedido(); return this.enGestion && p ? SIGUIENTE_ESTADO[p.estado] ?? null : null; });
  ngOnInit(): void { const id=Number(this.ruta.snapshot.paramMap.get('id')); forkJoin({pedido:this.servicio.obtener(id),detalles:this.servicio.detalles(id)}).subscribe({next:r=>{this.pedido.set(r.pedido);this.detalles.set(r.detalles);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}}); }
  avanzar(): void {
    const p = this.pedido(); const paso = this.siguiente();
    if (!p || !paso || this.avanzando()) return;
    this.avanzando.set(true); this.error.set('');
    this.servicio.cambiarEstado(p.id, paso.estado).subscribe({
      next: actualizado => { this.pedido.set(actualizado); this.avanzando.set(false); this.avisos.exito(`Pedido #${p.id}: ${nombreEstado(actualizado.estado)}.`); },
      error: e => { this.error.set(mensajeError(e)); this.avanzando.set(false); }
    });
  }
}
