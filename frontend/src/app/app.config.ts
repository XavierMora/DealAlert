import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { XsrfTokenInterceptor } from './shared/interceptors/request-interceptors';
import { AuthService } from './auth/services/auth-service';
import { firstValueFrom } from 'rxjs';
import { TooManyRequestsInterceptor } from './shared/interceptors/response-interceptors';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([XsrfTokenInterceptor, TooManyRequestsInterceptor])),
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return authService.setAuthentication();
    })
  ]
};
