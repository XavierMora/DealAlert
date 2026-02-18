import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { XsrfTokenInterceptor } from './shared/interceptors/request-interceptors';
import { UnauthorizedInterceptor } from './shared/interceptors/response-interceptors';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([XsrfTokenInterceptor, UnauthorizedInterceptor]))
  ]
};
