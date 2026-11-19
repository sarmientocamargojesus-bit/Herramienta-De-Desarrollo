import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AvisosService } from '../../services/avisos.service';
import { mensajeError } from '../../shared/errores';
import { GestionService } from '../../services/gestion.service';
import { Inventario } from '../../models/product';
import { estadoStock } from '../../shared/stock';
import { EtiquetaEstado } from '../../shared/etiqueta-estado/etiqueta-estado';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,MensajeError,EtiquetaEstado,IndicadorCarga],selector:'app-formulario-inventario',templateUrl:'./formulario-inventario.html',styleUrl:'./formulario-inventario.scss' })
export class FormularioInventario implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(GestionService); private readonly avisos=inject(AvisosService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')); readonly inventario=signal<Inventario|null>(null); readonly error=signal(''); readonly enviando=signal(false); readonly cargando=signal(true);
 readonly form=this.fb.nonNullable.group({stockMinimo:[0,[Validators.required,Validators.min(0),Validators.pattern(/^\d+$/)]]});
 readonly estado=estadoStock;
 // Muestra cómo quedará el producto con el mínimo escrito, antes de guardar.
 private readonly minimoEscrito=toSignal(this.form.controls.stockMinimo.valueChanges,{initialValue:0});
 readonly vistaPrevia=computed(()=>{const i=this.inventario();const minimo=Number(this.minimoEscrito());if(!i||!Number.isInteger(minimo)||minimo<0||minimo===i.stockMinimo)return null;return estadoStock(i.producto.stock,minimo)});
 ngOnInit():void{this.servicio.inventario(this.id).subscribe({next:i=>{this.inventario.set(i);this.form.patchValue({stockMinimo:i.stockMinimo});this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}})}
 guardar():void{if(this.form.invalid){this.form.markAllAsTouched();return}if(this.enviando())return;this.enviando.set(true);this.servicio.actualizarMinimo(this.id,this.form.getRawValue().stockMinimo).subscribe({next:()=>{this.avisos.exito('Stock mínimo actualizado.');void this.router.navigateByUrl('/gestion/inventario')},error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
