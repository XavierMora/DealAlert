import { HttpEvent, HttpHandlerFn, HttpRequest } from "@angular/common/http";
import { Observable } from "rxjs";

export function XsrfTokenInterceptor(
    req: HttpRequest<unknown>, 
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>>{
    if(['GET', 'OPTIONS'].includes(req.method)) return next(req);

    let xsrfToken = getCookie('XSRF-TOKEN');
    if(xsrfToken === null) return next(req)

    return next(req.clone({
        headers: req.headers.set('X-XSRF-TOKEN', xsrfToken)
    }));
}

function getCookie(name:string): string | null{
    if(document.cookie.length==0) return null;

    let cookies = document.cookie.split(";");    

    let value = null;
    let index = 0;
    while (value === null && index < cookies.length) {
        let cookieKeyValue = cookies[index].split("=");

        if(cookieKeyValue[0] == name) value = cookieKeyValue[1];

        index++;
    }

    return value;
}