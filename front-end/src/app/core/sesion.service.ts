import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { Rol, Usuario } from '../models/product';

interface RespuestaLogin { token: string; tipo: string; expiraEnSegundos: number; correo: string; rol: Rol; }
interface SesionGuardada extends RespuestaLogin { venceEn: number; }
const CLAVE = 'fast-food-sesion';

function leerSesion(): SesionGuardada | null {
  try {
    const valor = sessionStorage.getItem(CLAVE);
    const sesion = valor ? JSON.parse(valor) as SesionGuardada : null;
    if (sesion?.token && sesion.venceEn > Date.now()) return sesion;
    sessionStorage.removeItem(CLAVE);
  } catch { return null; }
  return null;
}

@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly http = inject(HttpClient);
  private readonly estado = signal<SesionGuardada | null>(leerSesion());
  readonly sesion = this.estado.asReadonly();
  readonly autenticado = computed(() => !!this.token());
  readonly rol = computed(() => this.token() ? this.estado()?.rol ?? null : null);
  private temporizador?: ReturnType<typeof setTimeout>;

  constructor() { this.programarVencimiento(); }

  // No modifica el estado: se usa dentro de computed. El cierre al vencer lo hace el temporizador.
  token(): string | null {
    const sesion = this.estado();
    return sesion && sesion.venceEn > Date.now() ? sesion.token : null;
  }

  iniciar(correo: string, contrasena: string) {
    return this.http.post<RespuestaLogin>('/api/auth/login', { correo, contrasena }).pipe(tap(respuesta => {
      const sesion: SesionGuardada = { ...respuesta, venceEn: Date.now() + respuesta.expiraEnSegundos * 1000 };
      sessionStorage.setItem(CLAVE, JSON.stringify(sesion));
      this.estado.set(sesion);
      this.programarVencimiento();
    }));
  }

  registrar(datos: Pick<Usuario, 'nombre' | 'correo' | 'telefono'> & { contrasena: string; direccion: string }) {
    return this.http.post<Usuario>('/api/usuarios', datos);
  }

  salir(): void {
    clearTimeout(this.temporizador);
    sessionStorage.removeItem(CLAVE);
    this.estado.set(null);
  }

  // Cierra la sesión en la página cuando vence el token, aunque no se haga ninguna petición.
  private programarVencimiento(): void {
    clearTimeout(this.temporizador);
    const sesion = this.estado();
    if (sesion) this.temporizador = setTimeout(() => this.salir(), Math.max(0, sesion.venceEn - Date.now()));
  }
}
