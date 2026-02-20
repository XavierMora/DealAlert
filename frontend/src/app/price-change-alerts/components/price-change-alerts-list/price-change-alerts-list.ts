import { Component, inject, signal } from '@angular/core';
import { PriceChangeAlertsService } from '../../services/price-change-alerts-service';
import { AsyncPipe } from '@angular/common';
import { SteamStoreLink } from '../../../games/components/steam-store-link/steam-store-link';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { toObservable } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-price-change-alerts-list',
  imports: [AsyncPipe, SteamStoreLink, Pagination],
  templateUrl: './price-change-alerts-list.html',
  styleUrl: './price-change-alerts-list.css',
})
export class PriceChangeAlertsList {
  private priceChangeAlertsService = inject(PriceChangeAlertsService);
  page = signal<number>(1);
  private page$ = toObservable(this.page);
  alerts$ = this.page$.pipe(
    switchMap((page) => {
      return this.priceChangeAlertsService.getAlerts(page-1);
    }) 
  )
}
