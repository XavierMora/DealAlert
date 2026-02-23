import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PriceChangeAlertsService {
  private http = inject(HttpClient);

  createAlert(gameId: number){
    return this.http.post<ApiResponse<PriceChangeAlert>>(
      `${environment.apiUrl}/price-change-alerts`, 
      {gameId},
      {credentials: 'include'}
    ).pipe(
      catchError((err: HttpErrorResponse) => throwError(() => err.error))
    )
  }

  deleteAlert(gameId: number){
    return this.http.delete<ApiResponse<undefined>>(
      `${environment.apiUrl}/price-change-alerts/${gameId}`,
      {credentials: 'include'}
    ).pipe(
      catchError((err: HttpErrorResponse) => throwError(() => err.error))
    )
  }

  getAlerts(page: number){
    return this.http.get<ApiResponse<PagedContent<PriceChangeAlert>>>(
      `${environment.apiUrl}/price-change-alerts?page=${page}`,
      {credentials: 'include'}
    ).pipe(
      catchError(err => throwError(() => err.error))
    )
  }
}
