import { HttpEvent, HttpHandlerFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, EMPTY, Observable } from "rxjs";

export function UnauthorizedInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
    const router = inject(Router);

    return next(req).pipe(
        catchError(err => {
            if(err.status === 401){
                router.navigateByUrl("/login")
                return EMPTY;
            }else{
                throw err;
            }
        })
    )
}