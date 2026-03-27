import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WolService {
  private http = inject(HttpClient);
  // Default URL de Quarkus
  // Apuntamos a la ruta relativa (el proxy de Angular lo redirigirá al 8080)
  private apiUrl = '/api/wol/wake';

  wakeUp(mac: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${mac}`, null, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  checkStatus(): Observable<{online: boolean, latencyMs: number}> {
    // La URL de status está en /api/wol/status y será enviada al 8080 por el proxy
    return this.http.get<{online: boolean, latencyMs: number}>('/api/wol/status');
  }
}
