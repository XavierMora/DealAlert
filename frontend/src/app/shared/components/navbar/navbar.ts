import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from "@angular/router";
import { AuthService } from '../../../auth/services/auth-service';
import { ApiErrorCode } from '../../models/ApiErrorCode';
import { AlertService } from '../alert/alert-service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);

  isAuthenticated = this.authService.isAuthenticated;

  authAction(){
    let auth = this.isAuthenticated()

    if(auth){
      this.authService.logout().subscribe({
        error: (err: ApiResponse<undefined>) => {
          if(err.error === ApiErrorCode.INTERNAL_SERVER_ERROR){
            this.alertService.newAlert({
              type: 'error',
              text: 'Error cerrando sesión.'
            })
          }
        }
      })
    }else{
      this.router.navigateByUrl('/login')
    }
  }
}
