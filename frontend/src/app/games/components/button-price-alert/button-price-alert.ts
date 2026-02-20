import { Component, inject, input, signal } from '@angular/core';
import { PriceChangeAlertsService } from '../../../price-change-alerts/services/price-change-alerts-service';
import { AlertService } from '../../../shared/components/alert/alert-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-button-price-alert',
  imports: [],
  template: `
    <button (click)="modifyPriceAlert()" class="text-right cursor-pointer" 
    [title]="(isInPriceAlert() ? 'Dejar de recibir': 'Recibir')+'notificaciones sobre cambios de precios'">
      @if(isInPriceAlert()){
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#e5e5e5"><path d="M160-200v-80h80v-280q0-33 8.5-65t25.5-61l60 60q-7 16-10.5 32.5T320-560v280h248L56-792l56-56 736 736-56 56-146-144H160Zm560-154-80-80v-126q0-66-47-113t-113-47q-26 0-50 8t-44 24l-58-58q20-16 43-28t49-18v-28q0-25 17.5-42.5T480-880q25 0 42.5 17.5T540-820v28q80 20 130 84.5T720-560v206Zm-276-50Zm36 324q-33 0-56.5-23.5T400-160h160q0 33-23.5 56.5T480-80Zm33-481Z"/></svg>
      }@else{
        <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 -960 960 960" width="24px" fill="#e5e5e5"><path d="M480-500Zm0 420q-33 0-56.5-23.5T400-160h160q0 33-23.5 56.5T480-80Zm240-360v-120H600v-80h120v-120h80v120h120v80H800v120h-80ZM160-200v-80h80v-280q0-83 50-147.5T420-792v-28q0-25 17.5-42.5T480-880q25 0 42.5 17.5T540-820v28q14 4 27.5 8.5T593-772q-15 14-27 30.5T545-706q-15-7-31.5-10.5T480-720q-66 0-113 47t-47 113v280h320v-112q18 11 38 18t42 11v83h80v80H160Z"/></svg>
      }
    </button>
  `
})

export class ButtonPriceAlert {
  game = input.required<Game>();
  private gameId!: number
  isInPriceAlert = signal<boolean>(false);
  private priceChangeAlertsService = inject(PriceChangeAlertsService);
  private alertService = inject(AlertService);
  private router = inject(Router);

  ngOnInit(){
    let { id, isInPriceAlert } = this.game();
    this.gameId = id
    this.isInPriceAlert.set(isInPriceAlert === undefined ? false : isInPriceAlert);
  }

  modifyPriceAlert(){
    if(!this.isInPriceAlert()){
      this.priceChangeAlertsService.createAlert(this.gameId).subscribe({
        next: (res) => {
          this.alertService.newAlert({
            type: 'success',
            text: res.message!,
            actionText: 'Ver',
            action: () => this.router.navigateByUrl('/price-alerts')
          })
          this.isInPriceAlert.set(true)
        },
        error: (res) => {
          this.alertService.newAlert({
            type: 'error',
            text: res.message === undefined ? 'Error creando alerta' : res.message
          })
        }
      });
    }else{
      this.priceChangeAlertsService.deleteAlert(this.gameId).subscribe({
        next: () => this.isInPriceAlert.set(false),
        error: (res) => {
          this.alertService.newAlert({
            type: 'error',
            text: res.message === undefined ? 'Error eliminando alerta' : res.message
          })
        }
      });
    }
  }
}