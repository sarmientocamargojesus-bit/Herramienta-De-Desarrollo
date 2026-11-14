import { InjectionToken } from '@angular/core';

// URL base de la API REST. En desarrollo el proxy envía /api al backend Spring Boot (puerto 8080).
// Para otro servidor basta con proveer un valor distinto, sin modificar los servicios.
export const API_URL = new InjectionToken<string>('API_URL', { providedIn: 'root', factory: () => '/api' });
