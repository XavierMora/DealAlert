import { CanActivateFn, RedirectCommand, Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth-service';
import { inject } from '@angular/core';

export const noAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if(authService.isAuthenticated()){
    return new RedirectCommand(router.createUrlTree(['/games'], {relativeTo: null}))
  }

  return true;
};
