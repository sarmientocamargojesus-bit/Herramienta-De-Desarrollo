import { Component } from '@angular/core';

@Component({
  selector: 'app-encabezado',
  template: `
    <header class="site-header">
      <a class="brand" href="#inicio" aria-label="Fast Food, ir al inicio"><span class="brand-mark">F<span>F</span></span><span>FAST<br>FOOD</span></a>
      <nav aria-label="Navegación principal">
        <a href="#inicio">Inicio</a>
        <a href="#menu">Menú</a>
        <a href="#nosotros">Nosotros</a>
      </nav>
      <div class="header-actions">
        <span class="future-action" title="Se habilitará con el inicio de sesión">Iniciar sesión</span>
        <span class="bag" title="El carrito se incorporará en la siguiente etapa" aria-label="Carrito, próximamente">Mi pedido <span>0</span></span>
      </div>
    </header>
  `,
  styles: [`
    .site-header{height:88px;display:flex;align-items:center;justify-content:space-between;gap:24px;max-width:1240px;margin:auto;padding:0 28px}
    .brand{display:flex;align-items:center;gap:10px;text-decoration:none;color:#171716;font-weight:950;font-size:17px;line-height:.85;letter-spacing:-.07em}
    .brand-mark{display:grid;place-items:center;width:42px;height:42px;background:#e53a23;color:white;transform:rotate(-12deg);font-size:19px;letter-spacing:-.14em}
    .brand-mark span{color:#ffd365}
    nav{display:flex;gap:36px}nav a{color:#24211c;text-decoration:none;font-weight:750;font-size:14px}nav a:hover{color:#cf2d1b}
    .header-actions{display:flex;align-items:center;gap:18px;font-size:13px;font-weight:800}.future-action{color:#8d8a82}.bag{background:#1d1c1a;color:#fff;padding:13px 17px;border-radius:6px;white-space:nowrap}.bag span{color:#ffd365;margin-left:8px}
    @media(max-width:760px){.site-header{height:auto;min-height:78px;flex-wrap:wrap;padding:15px 20px;gap:14px}nav{order:3;width:100%;justify-content:space-between;gap:10px}.header-actions{margin-left:auto}.future-action{display:none}}
  `]
})
export class Encabezado {}
