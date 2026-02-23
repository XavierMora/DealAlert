import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { catchError, finalize, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private retryAfterSignInCode = signal<any | undefined>(undefined);
  private retryAfterVerifyCode = signal<any | undefined>(undefined);
  private _isAuthenticated = signal<boolean>(false);
  private _currentAccount = signal<Account | null>(null);
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  readonly currentAccount = this._currentAccount.asReadonly();
  
  constructor(){
    this.setAuthentication()
  }

  signInCode(email: string): Observable<ApiResponse<undefined | Record<string, string>> | null>{
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

  verifyCode(email: string, code: string): Observable<ApiResponse<undefined | Record<string, string>>>{
    if(this.canSend(this.retryAfterVerifyCode(), email)) return throwError(() => null);

    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/verify-code`, 
      {email, code}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        if(err.status == 429) this.setRetryAfterResponse(this.retryAfterVerifyCode, err, email);
        throw err.error
      }),
      tap(() => this.setAuthentication()) // Se ejecuta si no hay error
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

  private setAuthentication(){
    this.http.get<ApiResponse<Account>>(
      `${environment.apiUrl}/account`,
      {credentials: 'include'}
    ).pipe(
      catchError(err => {throw err.error})
    ).subscribe({
      next: (res: ApiResponse<Account>) => {
        this._isAuthenticated.set(true);
        this._currentAccount.set(res.data!);
      },
      error: () => {
        this._isAuthenticated.set(false);
        this._currentAccount.set(null);
      }
    })
  }

  logout(): Observable<ApiResponse<undefined>>{
    return this.http.post<ApiResponse<undefined>>(
      `${environment.apiUrl}/account/logout`,
      null,
      {credentials: 'include'}
    ).pipe(
      finalize(() => this.setAuthentication()),
      catchError((err: ApiResponse<undefined>) => {throw err.error})
    )
  }
}
