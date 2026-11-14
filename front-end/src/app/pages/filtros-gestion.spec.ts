import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { GestionInventario } from './gestion-inventario/gestion-inventario';
import { GestionPedidos } from './gestion-pedidos/gestion-pedidos';
import { GestionProductos } from './gestion-productos/gestion-productos';
import { GestionUsuarios } from './gestion-usuarios/gestion-usuarios';

describe('Filtros de las tablas de gestión', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  it('Productos filtra por nombre sin tildes y por categoría, y muestra el contador', () => {
    const fixture = TestBed.createComponent(GestionProductos);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/productos').flush([
      { id: 1, nombre: 'Limonada Iqueña', descripcion: '', precio: 7.5, stock: 30, categoria: 'BEBIDAS', tieneImagen: false },
      { id: 2, nombre: 'Cola Grande', descripcion: '', precio: 6.5, stock: 30, categoria: 'BEBIDAS', tieneImagen: false },
      { id: 3, nombre: 'Papas Fuego', descripcion: 'Picantes', precio: 12.5, stock: 0, categoria: 'PIQUEOS', tieneImagen: false },
    ]);
    const productos = fixture.componentInstance;

    expect(productos.categoriasDisponibles()).toEqual(['BEBIDAS', 'PIQUEOS']);
    productos.busqueda.set('iquena');
    expect(productos.visibles().map(p => p.id)).toEqual([1]);

    productos.busqueda.set('');
    productos.categoria.set('PIQUEOS');
    expect(productos.visibles().map(p => p.id)).toEqual([3]);

    fixture.detectChanges();
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('1 de 3 productos');
    expect(texto).toContain('S/ 12.50');
  });

  it('Pedidos muestra primero los más recientes y filtra por estado y por número', () => {
    const fixture = TestBed.createComponent(GestionPedidos);
    fixture.detectChanges();
    const pedido = (id: number, estado: string, fecha: string) =>
      ({ id, estado, fecha, total: 10, usuario: { nombre: `Cliente ${id}`, correo: `c${id}@correo.com` } });
    TestBed.inject(HttpTestingController).expectOne('/api/pedidos').flush([
      pedido(1, 'CONFIRMADO', '2026-11-01T10:00:00'),
      pedido(2, 'CANCELADO', '2026-11-03T10:00:00'),
      pedido(3, 'CONFIRMADO', '2026-11-02T10:00:00'),
    ]);
    const pedidos = fixture.componentInstance;

    expect(pedidos.visibles().map(p => p.id)).toEqual([2, 3, 1]);
    pedidos.estado.set('CONFIRMADO');
    expect(pedidos.visibles().map(p => p.id)).toEqual([3, 1]);
    pedidos.busqueda.set('#1');
    expect(pedidos.visibles().map(p => p.id)).toEqual([1]);
  });

  it('Inventario ordena lo más urgente primero, cuenta cada estado y filtra lo que hay que reponer', () => {
    const fixture = TestBed.createComponent(GestionInventario);
    fixture.detectChanges();
    const fila = (id: number, nombre: string, stock: number) =>
      ({ id, producto: { nombre, stock }, stockMinimo: 5, fechaActualizacion: '', fechaStockBajo: null, fechaAgotado: null });
    TestBed.inject(HttpTestingController).expectOne('/api/inventarios').flush([
      fila(1, 'Cola Grande', 30), fila(2, 'Papas', 4), fila(3, 'Nuggets', 0), fila(4, 'Aros', 2),
    ]);
    const inventario = fixture.componentInstance;

    expect(inventario.visibles().map(i => i.producto.nombre)).toEqual(['Nuggets', 'Aros', 'Papas', 'Cola Grande']);
    expect([inventario.agotados(), inventario.stockBajo(), inventario.disponibles()]).toEqual([1, 2, 1]);
    inventario.soloPorReponer.set(true);
    expect(inventario.visibles().map(i => i.producto.nombre)).toEqual(['Nuggets', 'Aros', 'Papas']);

    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('tr.fila-agotado').length).toBe(1);
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('tr.fila-bajo').length).toBe(2);
  });

  it('Usuarios filtra por rol y por correo', () => {
    const fixture = TestBed.createComponent(GestionUsuarios);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/usuarios').flush([
      { id: 1, nombre: 'Ana', correo: 'ana@correo.com', telefono: '999', rol: 'CLIENTE', activo: true },
      { id: 2, nombre: 'Luis', correo: 'luis@fastfood.com', telefono: '988', rol: 'EMPLEADO', activo: true },
      { id: 3, nombre: 'Rosa', correo: 'rosa@fastfood.com', telefono: '977', rol: 'ADMINISTRADOR', activo: true },
    ]);
    const usuarios = fixture.componentInstance;

    usuarios.rol.set('EMPLEADO');
    expect(usuarios.visibles().map(u => u.nombre)).toEqual(['Luis']);
    usuarios.rol.set('TODOS');
    usuarios.busqueda.set('fastfood');
    expect(usuarios.visibles().map(u => u.nombre)).toEqual(['Luis', 'Rosa']);
  });
});
