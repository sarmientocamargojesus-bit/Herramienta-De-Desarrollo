import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/product';
import { CarritoService } from '../../services/carrito.service';
import { SesionService } from '../../services/sesion.service';
import { ImagenProducto } from '../../shared/imagen-producto/imagen-producto';

// Portada de la tienda. Recibe de Inicio las categorías y el producto destacado (input)
// y le avisa qué categoría eligió el cliente con "Ver combos" (output).
@Component({
  selector: 'app-portada',
  imports: [DecimalPipe, ImagenProducto, RouterLink],
  templateUrl: './portada.html',
  styleUrl: './portada.scss'
})
export class Portada implements OnInit {
  private readonly sesion = inject(SesionService);
  private readonly carrito = inject(CarritoService);

  readonly categorias = input<{ label: string; value: string }[]>([]);
  readonly destacado = input<Producto | null>(null);
  readonly categoriaElegida = output<string>();

  readonly esCliente = computed(() => this.sesion.rol() === 'CLIENTE');
  readonly unidades = computed(() => this.carrito.items().reduce((total, item) => total + item.cantidad, 0));
  // "Ver combos" solo aparece si la categoría COMBOS está activa.
  readonly tieneCombos = computed(() => this.categorias().some(c => c.value === 'COMBOS'));

  ngOnInit(): void {
    // El carrito del cliente vive en el servidor: se carga para mostrar su pedido en curso.
    if (this.esCliente() && !this.carrito.items().length) this.carrito.cargar().subscribe({ error: () => undefined });
  }
}
