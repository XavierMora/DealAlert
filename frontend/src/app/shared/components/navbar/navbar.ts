import { Component, effect, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterEvent, RouterLink, RouterLinkActive } from "@angular/router";
import { AuthService } from '../../../auth/services/auth-service';
import { ApiErrorCode } from '../../models/ApiErrorCode';
import { AlertService } from '../alert/alert-service';
import { exhaustMap, filter, map, of, switchMap, takeLast } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';
import { TelegramLink } from '../../../auth/components/telegram-link/telegram-link';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, TelegramLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);
  activeMenu = signal<boolean>(false);
  activeAccountMenu = signal<boolean>(false);
  showTelegramLink = signal<boolean>(false);

  isAuthenticated = this.authService.isAuthenticated;
  currentAccount = this.authService.currentAccount;

  navigateHomePage = () => {
    this.router.navigateByUrl('');
    this.activeMenu.set(false)
    this.activeAccountMenu.set(false);
  }

  toggleMenu(){
    this.activeMenu.update(value => !value);
    this.activeAccountMenu.set(false);
  }

  toggleAccountMenu(){
    this.activeAccountMenu.update(value => !value);
  }

  authAction(){
    let auth = this.isAuthenticated()

    if(auth){
      this.authService.logout().subscribe({
        next: () => {
          this.router.navigateByUrl('/games');
          this.alertService.newAlert({
            type: 'success',
            text: 'Sesión cerrada.'
          })
        }, 
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

    this.activeMenu.set(false)
    this.activeAccountMenu.set(false);
  }

  telegramUnlinked(){
    this.authService.unlinkTelegram().subscribe({
      next: () => {
        this.alertService.newAlert({
          type: 'success',
          text: 'Se desvinculó telegram.'
        })
      }, error: () => {
        this.alertService.newAlert({
          type: 'error',
          text: 'Error inesperado.'
        })
      }
    });
  }
}
