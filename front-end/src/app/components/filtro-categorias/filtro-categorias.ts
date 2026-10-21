import { Component, input, output } from '@angular/core';
type CategoryValue = string;

@Component({
  selector: 'app-filtro-categorias',
  templateUrl: './filtro-categorias.html',
  styleUrl: './filtro-categorias.scss'
})
export class FiltroCategorias {
  categories = input.required<{ label: string; value: CategoryValue }[]>();
  selected = input.required<CategoryValue>();
  categorySelected = output<CategoryValue>();
}
