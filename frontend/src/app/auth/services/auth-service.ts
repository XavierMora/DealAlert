import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  public signInCode(email: string): Observable<ApiResponse<undefined | Record<string, string>>>{
    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/sign-in-code`, 
      {email}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        throw err.error
      })
    );
  }
}
