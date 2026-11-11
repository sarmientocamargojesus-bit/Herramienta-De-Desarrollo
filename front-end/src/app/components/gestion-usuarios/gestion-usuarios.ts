import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { AvisosService } from '../../core/avisos.service';
import { GestionService } from '../../core/gestion.service';
import { SesionService } from '../../core/sesion.service';
import { coincide } from '../../core/texto';
import { Usuario } from '../../models/product';
import { DialogoConfirmacion } from '../dialogo-confirmacion/dialogo-confirmacion';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[EtiquetaEstado, IndicadorCarga, RouterLink, DialogoConfirmacion, MensajeError],selector:'app-gestion-usuarios',templateUrl:'./gestion-usuarios.html',styleUrl:'./gestion-usuarios.scss' })
export class GestionUsuarios implements OnInit {
 private readonly servicio=inject(GestionService); readonly usuarios=signal<Usuario[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 private readonly avisos=inject(AvisosService); private readonly sesion=inject(SesionService);
 readonly elegido=signal<Usuario|null>(null); readonly guardando=signal(false);
 readonly mensajeCambio=computed(()=>{const u=this.elegido();if(!u)return '';return u.activo?`${u.nombre} no podrá iniciar sesión y su sesión abierta se cerrará en su próxima acción. Podrás volver a activarla después.`:`${u.nombre} podrá volver a iniciar sesión con su contraseña.`});
 // El administrador no puede desactivar su propia cuenta desde aquí: perdería el acceso al panel.
 esPropio(u:Usuario):boolean{return u.correo.toLowerCase()===(this.sesion.sesion()?.correo??'').toLowerCase()}
 readonly busqueda=signal(''); readonly rol=signal('TODOS');
 readonly visibles=computed(()=>this.usuarios().filter(u=>(this.rol()==='TODOS'||u.rol===this.rol())&&coincide(this.busqueda(),u.nombre,u.correo,u.telefono)));
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.usuarios().subscribe({next:u=>{this.usuarios.set(u);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
 cambiarActivo():void{const u=this.elegido();if(!u||this.guardando())return;this.guardando.set(true);this.servicio.cambiarActivo(u,!u.activo).subscribe({next:()=>{this.elegido.set(null);this.guardando.set(false);this.avisos.exito(`Cuenta de ${u.nombre} ${u.activo?'desactivada':'activada'}.`);this.cargar()},error:e=>{this.elegido.set(null);this.guardando.set(false);this.reintentable.set(false);this.error.set(mensajeError(e))}})}
}
