import { Component, effect, inject, input, Signal, signal, WritableSignal } from '@angular/core';
import { GameService } from '../../services/game-service';
import { Observable } from 'rxjs';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { fromEvent, switchMap, interval } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-games-list',
  imports: [AsyncPipe, CurrencyPipe],
  templateUrl: './games-list.html',
  styleUrl: './games-list.css',
})
export class GamesList {
  private gameService = inject(GameService);
  name=input<string>();
  nameChanged: Observable<any> = toObservable(this.name)
  games$: Observable<ApiResponse<PagedContent<Game>>> = this.nameChanged.pipe(switchMap((name) => {
    return this.gameService.getGames(20, 0, name)
  })); 
}
