import { HttpEvent, HttpEventType, HttpHandlerFn, HttpRequest, HttpResponse } from "@angular/common/http";
import { catchError, EMPTY, map, Observable, tap, throwError } from "rxjs";
import { ApiErrorCode } from "../models/ApiErrorCode";
import { inject } from "@angular/core";
import { AlertService } from "../components/alert/alert-service";
import { ApiAuthErrorCode } from "../../auth/model/ApiAuthErrorCode";

export function TooManyRequestsInterceptor(
    req: HttpRequest<unknown>, 
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>>{
    const alertService = inject(AlertService);

    return next(req).pipe(
        catchError((err) => {
            if(err.error.error === ApiErrorCode.TOO_MANY_REQUESTS){
                alertService.newAlert({
                    type: 'error',
                    text: 'Muchas peticiones. Intentar más tarde.'
                })

                return EMPTY;
            }
            
            return throwError(() => err);
        })
    );
}