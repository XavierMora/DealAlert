import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, Signal, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError, EMPTY, Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private retryAfterSignInCode = signal<any | undefined>(undefined);
  private retryAfterVerifyCode = signal<any | undefined>(undefined);

  public signInCode(email: string): Observable<ApiResponse<undefined | Record<string, string>> | null>{
    if(this.canSend(this.retryAfterSignInCode(), email)) return throwError(() => null);

    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/sign-in-code`, 
      {email}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        if(err.status == 429) this.setRetryAfterResponse(this.retryAfterSignInCode, err, email);

        throw err.error
      })
    );
  }

  public verifyCode(email: string, code: string): Observable<ApiResponse<undefined | Record<string, string>>>{
    if(this.canSend(this.retryAfterVerifyCode(), email)) return throwError(() => null);

    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/verify-code`, 
      {email, code}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        if(err.status == 429) this.setRetryAfterResponse(this.retryAfterVerifyCode, err, email);
        throw err.error
      })
    );
  }

  private canSend(retryAfterValue: any, email: string){
    return retryAfterValue !== undefined && Date.now() < retryAfterValue.sentAt && email === retryAfterValue.email;
  }

  private setRetryAfterResponse(retryAfterSignal: WritableSignal<any>, err: HttpErrorResponse, email: string){
    let retryAfter = err.headers.get('retry-after');

    if(retryAfter != null){
      retryAfterSignal.set({
        sentAt: Date.now()+Number(retryAfter)*1000,
        email
      });
    }
  }

  loadCsrfToken(){
    return this.http.get(`${environment.apiUrl}/csrf`, {credentials: 'include'});
  }
}
