import { Component } from '@angular/core';

@Component({
  selector: 'app-portada',
  template: `
    <section class="hero" id="inicio" aria-labelledby="hero-title">
      <div class="hero-inner">
        <div class="hero-content">
          <p class="eyebrow">SABOR PARA CADA ANTOJO <span>✳</span></p>
          <h1 id="hero-title">RÁPIDO.<br><em>BRUTAL.</em><br>DELICIOSO.</h1>
          <p class="lead">Hamburguesas, combos, piqueos y bebidas. Explora el menú y encuentra lo que se te antoja.</p>
          <a class="hero-button" href="#menu">EXPLORAR EL MENÚ <span aria-hidden="true">↗</span></a>
          <p class="demo-label">Vista de muestra · los productos se conectarán a la API</p>
        </div>
      </div>
      <span class="side-tag" aria-hidden="true">FAST FOOD · FAST FOOD · FAST FOOD</span>
    </section>
  `,
  styles: [`
    .hero{position:relative;min-height:580px;background:#201e1b url('/assets/demo/hero-fast-food.png') center right/cover no-repeat;color:#fff;overflow:hidden}
    .hero:before{content:'';position:absolute;inset:0;background:linear-gradient(90deg,rgba(15,14,13,.97) 0%,rgba(15,14,13,.86) 40%,rgba(15,14,13,.10) 100%)}
    .hero-inner{position:relative;max-width:1240px;margin:auto;padding:76px 28px 88px}.hero-content{max-width:620px}
    .eyebrow{font-size:12px;letter-spacing:.19em;font-weight:900;color:#ffd166}.eyebrow span{color:#ef4d2f;margin-left:12px}
    h1{font-size:clamp(4.5rem,8vw,8rem);line-height:.9;letter-spacing:-.085em;margin:29px 0 26px;font-weight:1000}h1 em{font-style:normal;color:#f2482e}
    .lead{font-size:17px;line-height:1.55;max-width:475px;color:#eee5d8;margin-bottom:30px}
    .hero-button{display:inline-flex;align-items:center;gap:30px;text-decoration:none;color:#fff;background:#e53a23;border-radius:4px;padding:17px 22px;font-size:13px;font-weight:950;letter-spacing:.04em}.hero-button:hover{background:#bc2818}.hero-button span{font-size:18px}
    .demo-label{font-size:11px;color:#bfb9af;margin-top:20px}.side-tag{position:absolute;right:20px;top:45%;writing-mode:vertical-rl;font-size:10px;letter-spacing:.22em;font-weight:900;color:#fce8bd}
    @media(max-width:750px){.hero{min-height:620px;background-position:67% center}.hero:before{background:linear-gradient(90deg,rgba(15,14,13,.94),rgba(15,14,13,.70) 70%,rgba(15,14,13,.20))}.hero-inner{padding:70px 20px 90px}h1{font-size:clamp(4rem,16vw,6rem)}.side-tag{display:none}}
  `]
})
export class Portada {}
