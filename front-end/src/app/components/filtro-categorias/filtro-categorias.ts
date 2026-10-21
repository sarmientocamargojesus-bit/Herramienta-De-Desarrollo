import { Component, input, output } from '@angular/core';
import { ProductCategory } from '../../models/product';

type CategoryValue = ProductCategory | 'TODAS';

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
