import { Component, computed, inject, signal } from '@angular/core';
import { PriceChangeAlertsService } from '../../services/price-change-alerts-service';
import { AsyncPipe } from '@angular/common';
import { SteamStoreLink } from '../../../games/components/steam-store-link/steam-store-link';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { toObservable } from '@angular/core/rxjs-interop';
import { catchError, debounceTime, EMPTY, switchMap } from 'rxjs';
import { AlertService } from '../../../shared/components/alert/alert-service';

@Component({
  selector: 'app-price-change-alerts-list',
  imports: [AsyncPipe, SteamStoreLink, Pagination],
  templateUrl: './price-change-alerts-list.html',
  styleUrl: './price-change-alerts-list.css',
})
export class PriceChangeAlertsList {
  private priceChangeAlertsService = inject(PriceChangeAlertsService);
  private alertService = inject(AlertService)

  page = signal<number>(1);
  delete = signal<number | undefined>(undefined)
  private query = toObservable(computed(() => {
    return {
      page: this.page(),
      delete: this.delete()
    }
  }));
 
  alerts$ = this.query.pipe(
    debounceTime(300),
    switchMap((query) => {
      return this.priceChangeAlertsService.getAlerts(query.page-1).pipe(
        catchError(err => {
          if(err.status === 429){
            this.alertService.newAlert({
              type: 'error',
              text: 'Error obteniendo datos. Intentar más tarde.'
            })
          }
          return EMPTY;
        })
      );
    }) 
  )
  
  deletePriceAlert(priceAlert: PriceChangeAlert){
    this.priceChangeAlertsService.deleteAlert(priceAlert.game.id).subscribe({
      next: () => {
        this.delete.set(priceAlert.id)

        this.alertService.newAlert({
          type: 'success',
          text: 'Alerta eliminada'
        })
      },
      error: () => {
        this.alertService.newAlert({
          type: 'error',
          text: 'Error al eliminar la alerta'
        })
      }
    })  
  }
}
