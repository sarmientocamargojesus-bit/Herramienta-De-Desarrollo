import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { Categoria } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,MensajeError],selector:'app-formulario-producto',templateUrl:'./formulario-producto.html',styleUrl:'./formulario-producto.scss' })
export class FormularioProducto implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(CatalogoService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')) || null; readonly categorias=signal<Categoria[]>([]); readonly error=signal(''); readonly enviando=signal(false);
 readonly form=this.fb.nonNullable.group({nombre:['',Validators.required],descripcion:[''],precio:[0,[Validators.required,Validators.min(0.01)]],stock:[0,[Validators.required,Validators.min(0),Validators.pattern(/^\d+$/)]],categoria:['',Validators.required]});
 ngOnInit():void{if(this.id){forkJoin({producto:this.servicio.producto(this.id),categorias:this.servicio.categorias()}).subscribe({next:r=>{this.categorias.set(r.categorias);this.form.patchValue(r.producto)},error:e=>this.error.set(mensajeError(e))})}else{this.servicio.categorias().subscribe({next:c=>this.categorias.set(c),error:e=>this.error.set(mensajeError(e))})}}
 guardar():void{if(this.form.invalid){this.form.markAllAsTouched();return}this.enviando.set(true); const datos=this.form.getRawValue();const peticion=this.id?this.servicio.actualizarProducto(this.id,datos):this.servicio.crearProducto(datos);peticion.subscribe({next:()=>void this.router.navigateByUrl('/gestion/productos'),error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
