import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AvisosService } from '../../services/avisos.service';
import { mensajeError } from '../../shared/errores';
import { GestionService } from '../../services/gestion.service';
import { SesionService } from '../../services/sesion.service';
import { Rol, Usuario } from '../../models/product';
import { DialogoConfirmacion } from '../../shared/dialogo-confirmacion/dialogo-confirmacion';
import { EtiquetaEstado } from '../../shared/etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports:[ReactiveFormsModule,RouterLink,DialogoConfirmacion,EtiquetaEstado,MensajeError],selector:'app-cambio-rol',templateUrl:'./cambio-rol.html',styleUrl:'./cambio-rol.scss' })
export class CambioRol implements OnInit {
 private readonly fb=inject(FormBuilder); private readonly ruta=inject(ActivatedRoute); private readonly router=inject(Router); private readonly servicio=inject(GestionService); private readonly avisos=inject(AvisosService); private readonly sesion=inject(SesionService);
 readonly id=Number(this.ruta.snapshot.paramMap.get('id')); readonly usuario=signal<Usuario|null>(null); readonly error=signal(''); readonly enviando=signal(false);
 readonly form=this.fb.nonNullable.group({rol:['CLIENTE' as Rol,Validators.required]});
 readonly confirmando=signal(false);
 // Cambiar el propio rol quita el acceso al panel: el backend invalida la sesión al no coincidir el rol.
 readonly esPropio=computed(()=>this.usuario()?.correo.toLowerCase()===(this.sesion.sesion()?.correo??'').toLowerCase());
 readonly mensajeConfirmacion=computed(()=>{const u=this.usuario();if(!u)return '';const rol=this.form.controls.rol.value;const base=`${u.nombre} pasará de ${u.rol} a ${rol} y deberá volver a iniciar sesión.`;return this.esPropio()?`${base} Es tu propia cuenta: perderás el acceso a Gestión si dejas de ser administrador.`:base});
 ngOnInit():void{this.servicio.usuarios().subscribe({next:us=>{const u=us.find(x=>x.id===this.id);if(u){this.usuario.set(u);this.form.patchValue({rol:u.rol})}else this.error.set('Usuario no encontrado.')},error:e=>this.error.set(mensajeError(e))})}
 guardar():void{const u=this.usuario();if(!u||this.form.invalid)return;if(this.form.getRawValue().rol===u.rol){this.error.set('El usuario ya tiene ese rol.');return}this.error.set('');this.confirmando.set(true)}
 confirmar():void{this.confirmando.set(false);this.enviando.set(true);this.servicio.cambiarRol(this.id,this.form.getRawValue().rol).subscribe({next:()=>{this.avisos.exito('Rol actualizado.');if(this.esPropio()){this.sesion.salir();void this.router.navigateByUrl('/iniciar-sesion')}else void this.router.navigateByUrl('/gestion/usuarios')},error:e=>{this.error.set(mensajeError(e));this.enviando.set(false)}})}
}
