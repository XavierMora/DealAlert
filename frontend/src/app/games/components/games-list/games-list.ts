import { Component, computed, inject, input, signal } from '@angular/core';
import { GameService } from '../../services/game-service';
import { distinctUntilChanged, finalize, Observable } from 'rxjs';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { switchMap } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';
import { Pagination } from '../pagination/pagination';

@Component({
  selector: 'app-games-list',
  imports: [AsyncPipe, CurrencyPipe, Pagination],
  templateUrl: './games-list.html',
  styleUrl: './games-list.css',
})
export class GamesList {
  private gameService = inject(GameService);
  name = input.required<string | undefined>();
  page = signal<number>(1);
  
  // Crea un signal que derivado de name y page
  query = computed(() => {
    return {
      name: this.name(),
      page: this.page()
    }
  });
  
  queryChanged$: Observable<any> = toObservable(this.query); // Emite el valor de query cuando los valores de los que depende cambian
  
  private firstNameRequestOnProgress = false;

  games$: Observable<ApiResponse<PagedContent<Game>>> = this.queryChanged$.pipe(
    distinctUntilChanged((prev, current) => { // Acepta los valores si la función devuelve falso
      if(prev.name!==current.name){
        current.page = 1;
        this.firstNameRequestOnProgress = true;
        return false
      }
      return this.firstNameRequestOnProgress || prev.page === current.page
    }),
    switchMap((query) => {
      return this.gameService.getGames(5, query.page-1, query.name).pipe(
        finalize(() => {
          this.firstNameRequestOnProgress = false;
        })
      )
    })
  );
}
