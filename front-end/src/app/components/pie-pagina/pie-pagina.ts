import { Component } from '@angular/core';

@Component({
  selector: 'app-pie-pagina',
  template: `<footer><div class="inner"><div><a class="logo" href="#inicio">FAST<span>FOOD.</span></a><p>Proyecto académico de menú y pedidos de comida rápida.</p></div><nav aria-label="Navegación del pie de página"><a href="#inicio">Inicio</a><a href="#menu">Menú</a><a href="#nosotros">Nosotros</a></nav></div><div class="bottom">Vista inicial del frontend Angular · Datos de muestra</div></footer>`,
  styles: [`footer{background:#1d1b19;color:#fff;padding:55px 28px 24px}.inner{max-width:1240px;margin:auto;display:flex;justify-content:space-between;gap:30px}.logo{color:white;text-decoration:none;font-weight:1000;font-size:29px;letter-spacing:-.075em}.logo span{color:#f04a30}.inner p{color:#bdb6aa;font-size:13px}.inner nav{display:flex;gap:28px;align-items:start}.inner nav a{color:#eee8de;text-decoration:none;font-weight:800;font-size:13px}.bottom{max-width:1240px;margin:55px auto 0;padding-top:18px;border-top:1px solid #4a453e;color:#97918a;font-size:12px}@media(max-width:650px){.inner{display:block}.inner nav{margin-top:30px}.bottom{margin-top:35px}}`]
})
export class PiePagina {}
