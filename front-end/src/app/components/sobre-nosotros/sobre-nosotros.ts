import { Component } from '@angular/core';

@Component({
  selector: 'app-sobre-nosotros',
  template: `
    <section id="nosotros" class="story"><div class="inner"><div class="number">01<span> / FAST FOOD</span></div><div><p class="eyebrow">EL PROYECTO</p><h2>BUEN SABOR.<br><em>SIN COMPLICACIONES.</em></h2><p class="copy">Esta página reúne el menú de Fast Food en una vista clara. Más adelante, el cliente podrá iniciar sesión, guardar productos en su carrito y confirmar un pedido con el backend del proyecto.</p></div><div class="stamp" aria-hidden="true">FF<br><span>FAST FOOD</span></div></div></section>
  `,
  styles: [`
    .story{background:#e83b25;color:#fff;padding:80px 28px}.inner{max-width:1240px;margin:auto;display:grid;grid-template-columns:minmax(0,1fr) minmax(0,2.3fr) minmax(0,.7fr);gap:32px;align-items:center}.number{align-self:start;font-size:52px;font-weight:1000;letter-spacing:-.08em}.number span{font-size:11px;letter-spacing:.12em}.eyebrow{font-size:12px;letter-spacing:.15em;font-weight:900}h2{font-size:clamp(2.7rem,5vw,5rem);line-height:.94;letter-spacing:-.07em;margin:18px 0 22px;font-weight:1000;overflow-wrap:anywhere}h2 em{font-style:normal;color:#ffd46e}.copy{max-width:600px;line-height:1.65;font-size:15px;color:#ffede7}.stamp{width:136px;height:136px;display:grid;align-content:center;justify-content:center;text-align:center;border:3px solid #fff;border-radius:50%;font-weight:1000;font-size:50px;line-height:.8;transform:rotate(12deg)}.stamp span{font-size:10px;letter-spacing:.1em;margin-top:12px}@media(max-width:850px){.inner{grid-template-columns:minmax(0,1fr)}.number{font-size:28px}.stamp{display:none}}@media(max-width:480px){.story{padding:64px 20px}.inner{gap:20px}h2{font-size:clamp(2rem,9vw,2.7rem)}}
  `]
})
export class SobreNosotros {}
