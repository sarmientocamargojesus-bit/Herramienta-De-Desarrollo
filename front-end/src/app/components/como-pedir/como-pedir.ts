import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SesionService } from '../../services/sesion.service';

// Parte final de la página de inicio: los pasos para pedir, las preguntas frecuentes
// y el llamado a empezar el pedido. Es contenido fijo, salvo el acceso a "Mi pedido".
@Component({
  selector: 'app-como-pedir',
  imports: [RouterLink],
  templateUrl: './como-pedir.html',
  styleUrl: './como-pedir.scss'
})
export class ComoPedir {
  private readonly sesion = inject(SesionService);
  // El personal no compra desde la tienda, así que no se le muestra "Ver mi pedido".
  readonly puedeComprar = computed(() => !this.sesion.autenticado() || this.sesion.rol() === 'CLIENTE');

  readonly pasos = [
    { titulo: 'Elige', texto: 'Agrega productos a tu pedido. Si aún no inicias sesión, se guarda en este navegador.' },
    { titulo: 'Confirma', texto: 'Indica recojo o delivery y paga con Yape o tarjeta de prueba. El stock se revisa en ese momento.' },
    { titulo: 'Recoge o recibe', texto: 'Sigue el estado en Mis pedidos: confirmado, en preparación, listo y entregado.' },
  ];

  readonly preguntas = [
    { pregunta: '¿Necesito una cuenta para pedir?', respuesta: 'Puedes armar tu pedido sin cuenta. Para confirmarlo inicias sesión o te registras, y los productos que elegiste se conservan.' },
    { pregunta: '¿Se cobra dinero real?', respuesta: 'No. Los pagos con Yape o tarjeta son simulados porque Fast Food es un proyecto académico.' },
    { pregunta: '¿Qué pasa si un producto se agota?', respuesta: 'El stock se revisa al confirmar. Si ya no alcanza, el pedido no se confirma y te avisamos para que lo ajustes.' },
    { pregunta: '¿Cómo sigo mi pedido?', respuesta: 'En Mis pedidos ves cada pedido con su estado, que avanza a medida que el local lo prepara y lo entrega.' },
  ];
}
