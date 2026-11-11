import { Component, inject } from '@angular/core';
import { AvisosService } from '../../core/avisos.service';

@Component({
  imports: [],
  selector: 'app-aviso-exito',
  templateUrl: './aviso-exito.html',
  styleUrl: './aviso-exito.scss',
})
export class AvisoExito {
  readonly avisos = inject(AvisosService);
}
