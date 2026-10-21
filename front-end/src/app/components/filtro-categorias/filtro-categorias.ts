import { Component, input, output } from '@angular/core';
import { ProductCategory } from '../../models/product';

type CategoryValue = ProductCategory | 'TODAS';

@Component({
  selector: 'app-filtro-categorias',
  template: `
    <div class="filters" role="group" aria-label="Filtrar productos por categoría">
      @for (category of categories(); track category.value) {
        <button type="button" [class.active]="selected() === category.value" [attr.aria-pressed]="selected() === category.value" (click)="categorySelected.emit(category.value)">{{ category.label }}</button>
      }
    </div>
  `,
  styles: [`
    .filters{display:flex;gap:9px;overflow-x:auto;padding:2px 0 10px}.filters button{white-space:nowrap;border:1px solid #d9d1c5;background:transparent;border-radius:4px;padding:12px 21px;color:#38342f;font-size:13px;font-weight:800;cursor:pointer}.filters button:hover,.filters button.active{background:#1d1c1a;border-color:#1d1c1a;color:white}
  `]
})
export class FiltroCategorias {
  categories = input.required<{ label: string; value: CategoryValue }[]>();
  selected = input.required<CategoryValue>();
  categorySelected = output<CategoryValue>();
}
