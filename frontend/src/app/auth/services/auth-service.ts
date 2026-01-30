import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError, from, map, Observable, of, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  public login(email: string): Observable<ApiResponse<undefined | Record<string, string>>>{
    localStorage.setItem("Device-ID", "3f2a1c5e-9b7a-4f6e-8c0b-6b2a7e9d1c4f");
    const headers = new HttpHeaders().set('Device-ID', localStorage.getItem('Device-ID')!);
  
    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/sign-in-code`, 
      {email}, 
      {
        mode: 'cors',
        headers
      },
    ).pipe(
      catchError((resError, _) => {throw resError.error})
    );
  }
}
