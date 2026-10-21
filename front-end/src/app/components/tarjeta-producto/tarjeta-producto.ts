import { Component, input } from '@angular/core';
import { Product } from '../../models/product';

@Component({
  selector: 'app-tarjeta-producto',
  template: `
    <article class="card">
      <div class="image-wrap"><img [src]="product().imagen" [alt]="'Imagen ilustrativa de ' + product().nombre" loading="lazy"><span class="category">{{ product().categoria }}</span></div>
      <div class="details"><div class="top"><h3>{{ product().nombre }}</h3><strong>S/ {{ product().precio.toFixed(2) }}</strong></div><p>{{ product().descripcion }}</p><span class="availability">Pedido disponible en una próxima etapa <span aria-hidden="true">↗</span></span></div>
    </article>
  `,
  styles: [`
    .card{height:100%;min-width:0;background:#fff;border:1px solid #e2dbd0;border-radius:7px;overflow:hidden;box-shadow:0 10px 26px rgba(38,25,12,.045)}.image-wrap{width:100%;aspect-ratio:4/3;position:relative;background:#29211d;overflow:hidden}.image-wrap img{width:100%;height:100%;object-fit:cover;transition:transform .3s}.card:hover img{transform:scale(1.05)}.category{position:absolute;left:16px;top:16px;background:#f5eddf;color:#1f1e1b;padding:7px 9px;border-radius:3px;font-size:10px;font-weight:900;letter-spacing:.08em}.details{padding:20px}.top{display:flex;flex-wrap:wrap;justify-content:space-between;align-items:flex-start;gap:12px}.top h3{font-size:21px;letter-spacing:-.04em;margin:0;font-weight:950}.top strong{white-space:nowrap;color:#d83120;font-size:17px}.details p{font-size:13px;line-height:1.5;color:#6f6a62;min-height:39px;margin:12px 0 18px}.availability{display:flex;justify-content:space-between;gap:8px;border-top:1px solid #ebe7df;padding-top:15px;color:#817b72;font-size:11px;font-weight:750}.availability span{font-size:16px;color:#d83120}
  `]
})
export class TarjetaProducto { product = input.required<Product>(); }
