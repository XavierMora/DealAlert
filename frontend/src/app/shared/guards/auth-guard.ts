import { inject } from '@angular/core';
import { CanActivateFn, RedirectCommand, Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth-service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if(!authService.isAuthenticated()){
    const loginUrl = router.parseUrl('/login')
    return new RedirectCommand(loginUrl)
  }

  return true;
};
