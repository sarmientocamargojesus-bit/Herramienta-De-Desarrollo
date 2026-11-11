import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AvisosService } from '../../core/avisos.service';
import { mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Rol, Usuario } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,MensajeError],selector:'app-cambio-rol',templateUrl:'./cambio-rol.html',styleUrl:'./cambio-rol.scss' })
export class CambioRol implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(GestionService); private readonly avisos=inject(AvisosService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')); readonly usuario=signal<Usuario|null>(null); readonly error=signal(''); readonly enviando=signal(false);
 readonly form=this.fb.nonNullable.group({rol:['CLIENTE' as Rol,Validators.required]});
 ngOnInit():void{this.servicio.usuarios().subscribe({next:us=>{const u=us.find(x=>x.id===this.id);if(u){this.usuario.set(u);this.form.patchValue({rol:u.rol})}else this.error.set('Usuario no encontrado.')},error:e=>this.error.set(mensajeError(e))})}
 guardar():void{if(!this.usuario()||this.form.invalid)return;this.enviando.set(true);this.servicio.cambiarRol(this.id,this.form.getRawValue().rol).subscribe({next:()=>{this.avisos.exito('Rol actualizado.');void this.router.navigateByUrl('/gestion/usuarios')},error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
