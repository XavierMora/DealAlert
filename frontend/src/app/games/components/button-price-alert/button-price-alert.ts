import { Component, inject, input, output, signal } from '@angular/core';
import { PriceChangeAlertsService } from '../../../price-change-alerts/services/price-change-alerts-service';
import { AlertService } from '../../../shared/components/alert/alert-service';
import { Router } from '@angular/router';
import { AuthService } from '../../../auth/services/auth-service';
import { toObservable } from '@angular/core/rxjs-interop';
import { TelegramLink } from '../../../auth/components/telegram-link/telegram-link';

@Component({
  selector: 'app-button-price-alert',
  imports: [TelegramLink],
  templateUrl: './button-price-alert.html'
})

export class ButtonPriceAlert {
  game = input.required<Game>();
  private gameId!: number
  isInPriceAlert = signal<boolean>(false);
  private priceChangeAlertsService = inject(PriceChangeAlertsService);
  private alertService = inject(AlertService);
  private router = inject(Router);
  private authService = inject(AuthService);
  alertDeleted = output<number>();
  showTelegramLink = signal<boolean>(false);

  constructor(){
    toObservable(this.authService.isAuthenticated).subscribe(auth => {
      if(!auth) this.isInPriceAlert.set(false);
    })
  }

  ngOnInit(){
    let { id, isInPriceAlert } = this.game();
    this.gameId = id
    this.isInPriceAlert.update(currentValue => isInPriceAlert !== undefined ? isInPriceAlert : currentValue);
  }

  modifyPriceAlert(){
    if(!this.authService.isAuthenticated()){
      this.router.navigateByUrl('/login')
      return;
    }

    if(!this.isInPriceAlert()){
      if(this.authService.currentAccount()?.telegramUserId == null){
        this.showTelegramLink.set(true);
        return;
      }

      this.createPriceAlert();
    }else{
      this.priceChangeAlertsService.deleteAlert(this.gameId).subscribe({
        next: () => {
          this.isInPriceAlert.set(false);
          this.alertDeleted.emit(this.gameId);
        }, error: (res) => {
          this.alertService.newAlert({
            type: 'error',
            text: res.message === undefined ? 'Error eliminando alerta.' : res.message
          })
        }
      });
    }
  }

  telegramLinked(){
    this.showTelegramLink.set(false);
    this.createPriceAlert();
  }

  private createPriceAlert(){
    this.priceChangeAlertsService.createAlert(this.gameId).subscribe({
      next: (res) => {
        this.alertService.newAlert({
          type: 'success',
          text: res.message!,
          actionText: 'Ver',
          action: () => this.router.navigateByUrl('/deal-alerts')
        })
        this.isInPriceAlert.set(true)
      },
      error: (res) => {
        this.alertService.newAlert({
          type: 'error',
          text: res.message === undefined ? 'Error creando alerta.' : res.message
        })
      }
    });
  }
}
