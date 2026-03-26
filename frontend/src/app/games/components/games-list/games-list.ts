import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { GameService } from '../../services/game-service';
import { debounceTime, distinctUntilChanged, finalize, Observable, tap } from 'rxjs';
import { AsyncPipe, CurrencyPipe, NgOptimizedImage } from '@angular/common';
import { switchMap } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { ButtonPriceAlert } from '../button-price-alert/button-price-alert';
import { SteamStoreLink } from '../steam-store-link/steam-store-link';
import { GameCard } from '../game-card/game-card';

@Component({
  selector: 'app-games-list',
  imports: [AsyncPipe, CurrencyPipe, Pagination, ButtonPriceAlert, SteamStoreLink, GameCard, NgOptimizedImage],
  templateUrl: './games-list.html',
  styleUrl: './games-list.css',
})
export class GamesList {
  private gameService = inject(GameService);
  name = input.required<string | undefined>();
  page = signal<number>(1);
  
  // Crea un signal derivado de name y page
  query = computed(() => {
    return {
      name: this.name(),
      page: this.page()
    }
  });

  games$: Observable<ApiResponse<PagedContent<Game>>> = toObservable(this.query).pipe(
    debounceTime(200),
    switchMap((query) => this.gameService.getGames(20, query.page-1, query.name))
  );

  constructor(){
    effect(() => {
      this.name();
      this.page.set(1);
    })
  }
}
