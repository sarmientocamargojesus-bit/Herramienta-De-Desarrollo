import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Inventario } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,MensajeError],selector:'app-formulario-inventario',templateUrl:'./formulario-inventario.html',styleUrl:'./formulario-inventario.scss' })
export class FormularioInventario implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(GestionService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')); readonly inventario=signal<Inventario|null>(null); readonly error=signal(''); readonly enviando=signal(false);
 readonly form=this.fb.nonNullable.group({stockMinimo:[0,[Validators.required,Validators.min(0),Validators.pattern(/^\d+$/)]]});
 ngOnInit():void{this.servicio.inventario(this.id).subscribe({next:i=>{this.inventario.set(i);this.form.patchValue({stockMinimo:i.stockMinimo})},error:e=>this.error.set(mensajeError(e))})}
 guardar():void{if(this.form.invalid){this.form.markAllAsTouched();return}this.enviando.set(true);this.servicio.actualizarMinimo(this.id,this.form.getRawValue().stockMinimo).subscribe({next:()=>void this.router.navigateByUrl('/gestion/inventario'),error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
