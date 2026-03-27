import { Component, computed, inject, signal } from '@angular/core';
import { PriceChangeAlertsService } from '../../services/price-change-alerts-service';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { catchError, debounceTime, of, switchMap } from 'rxjs';
import { AlertService } from '../../../shared/components/alert/alert-service';
import { ApiErrorCode } from '../../../shared/models/ApiErrorCode';
import { GameCard } from '../../../games/components/game-card/game-card';
import { toObservable } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-price-change-alerts-list',
  imports: [GameCard, Pagination],
  templateUrl: './price-change-alerts-list.html',
  styleUrl: './price-change-alerts-list.css',
})
export class PriceChangeAlertsList {
  private priceChangeAlertsService = inject(PriceChangeAlertsService);
  private alertService = inject(AlertService);

  page = signal<number>(1);
  reloadPage = signal<boolean>(false);
  alerts = signal<ApiResponse<PagedContent<PriceChangeAlert>> | undefined>(undefined);

  constructor(){
    toObservable(computed(() => {
      return {page: this.page(), reloadPage: this.reloadPage()}
    })).pipe(
      debounceTime(150),
      switchMap(query => this.getAlerts(query.page))
    ).subscribe(data => this.alerts.set(data));    
  }

  getAlerts(page: number){
    return this.priceChangeAlertsService.getAlerts(page-1).pipe(
      catchError(err => {
        if(err.error === ApiErrorCode.TOO_MANY_REQUESTS){
          this.alertService.newAlert({
            type: 'error',
            text: 'Error obteniendo datos. Intentar más tarde.'
          })
        }
        return of(undefined);
      })
    );
  }

  deleteAlert(gameId: number){
    let goToPrevPage = false;
    let reloadPage = false;

    this.alerts.update(alerts => {
      if(alerts === undefined) return alerts;

      let data = alerts.data!;

      if(data.numberOfElements == 1){
        if(data.first && data.last) return undefined; // última alerta

        if(data.last) goToPrevPage = true;
        else reloadPage = true;
        
        return alerts;
      }
      
      // Listado sin alerta que se borró
      return {
        ...alerts,
        data: {
          ...data,
          content: data.content.filter(alert => alert.game.id != gameId),
          numberOfElements: data.numberOfElements-1
        }
      }
    })

    if(goToPrevPage) this.page.set(this.page()-1);
    else if(reloadPage) this.reloadPage.update(reload => !reload)
  }
}
