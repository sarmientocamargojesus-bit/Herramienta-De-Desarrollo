import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AvisosService } from '../../core/avisos.service';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { noSoloEspacios } from '../../core/validadores';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,MensajeError],selector:'app-formulario-categoria',templateUrl:'./formulario-categoria.html',styleUrl:'./formulario-categoria.scss' })
export class FormularioCategoria implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(CatalogoService); private readonly avisos=inject(AvisosService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')) || null; readonly error=signal(''); readonly enviando=signal(false);
 readonly form=this.fb.nonNullable.group({nombre:['',[Validators.required,noSoloEspacios]],descripcion:[''],activo:[true]});
 ngOnInit():void{if(this.id)this.servicio.todasCategorias().subscribe({next:cs=>{const c=cs.find(x=>x.id===this.id);if(c)this.form.patchValue(c);else this.error.set('Categoría no encontrada.')},error:e=>this.error.set(mensajeError(e))})}
 guardar():void{if(this.form.invalid){this.form.markAllAsTouched();return}this.enviando.set(true);const datos=this.form.getRawValue();const peticion=this.id?this.servicio.actualizarCategoria(this.id,datos):this.servicio.crearCategoria(datos);peticion.subscribe({next:()=>{this.avisos.exito('Categoría guardada.');void this.router.navigateByUrl('/gestion/categorias')},error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
