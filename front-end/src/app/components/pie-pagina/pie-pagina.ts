import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SesionService } from '../../services/sesion.service';

@Component({
  selector: 'app-pie-pagina',
  imports: [RouterLink],
  templateUrl: './pie-pagina.html',
  styleUrl: './pie-pagina.scss'
})
export class PiePagina {
  // Los enlaces de "Tu cuenta" cambian según quién inició sesión.
  readonly sesion = inject(SesionService);
  readonly anio = new Date().getFullYear();

  subir(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
