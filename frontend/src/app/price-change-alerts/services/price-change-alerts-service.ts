import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PriceChangeAlertsService {
  private http = inject(HttpClient);

  createAlert(gameId: number){
    return this.http.post<ApiResponse<PriceChangeAlert>>(
      `${environment.apiUrl}/price-change-alerts`, 
      {gameId},
      {withCredentials: true}
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        throw err.error
      })
    )
  }

  deleteAlert(gameId: number){
    return this.http.delete<ApiResponse<undefined>>(
      `${environment.apiUrl}/price-change-alerts/${gameId}`,
      {credentials: 'include'}
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        throw err.error
      })
    )
  }
}
