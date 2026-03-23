import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { catchError, EMPTY, finalize, firstValueFrom, Observable, of, tap, throwError } from 'rxjs';
import { ApiAuthErrorCode } from '../model/ApiAuthErrorCode';

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

  signInCode(email: string): Observable<ApiResponse<undefined | Record<string, string>> | null>{
    if(this.canSend(this.retryAfterSignInCode(), email)) return throwError(() => {
      return {error: ApiAuthErrorCode.CODE_SENT_RECENTLY}
    });

    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/sign-in-code`, 
      {email}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        if(err.status == 429) this.setRetryAfterResponse(this.retryAfterSignInCode, err, email);
        return throwError(() => err.error)
      })
    );
  }
  
  verifyCode(email: string, code: string): Observable<ApiResponse<undefined | Record<string, string>>>{
    if(this.canSend(this.retryAfterVerifyCode(), email)) return EMPTY;

    return this.http.post<ApiResponse<undefined | Record<string, string>>>(
      `${environment.apiUrl}/account/verify-code`, 
      {email, code}, 
      {credentials: 'include'}
    ).pipe(
      catchError((err:HttpErrorResponse) => {
        if(err.status == 429) this.setRetryAfterResponse(this.retryAfterVerifyCode, err, email);
        return throwError(() => err.error)
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

  private getAccount(){
    return this.http.get<ApiResponse<Account>>(
      `${environment.apiUrl}/account`,
      {credentials: 'include'}
    )
  }

  setAuthentication(){ 
    // Se llama desde el provideAppInitializer para que se establezca en el inicio
    // antes de los guards
    return firstValueFrom(this.getAccount().pipe(
        catchError((err) => of(err.error)),
        tap((res: ApiResponse<Account>) => {
          if(res.success){
            this._isAuthenticated.set(true);
            this._currentAccount.set(res.data!);
          }else{
            this._isAuthenticated.set(false);
            this._currentAccount.set(null);
          }
        })
      )
    )
  }

  logout(): Observable<ApiResponse<undefined>>{
    return this.http.post<ApiResponse<undefined>>(
      `${environment.apiUrl}/account/logout`,
      null,
      {credentials: 'include'}
    ).pipe(
      catchError((err: ApiResponse<undefined>) => throwError(() => err.error)),
      // Se actualiza el estado del auth
      // también el token csrf se borra asi que se usa el setAuthentication para establecerlo
      finalize(() => this.setAuthentication()), 
    )
  }
}
