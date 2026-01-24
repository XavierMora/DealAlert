import { Component, inject } from '@angular/core';
import { GameService } from '../../services/game-service';
import { Observable } from 'rxjs';
import { AsyncPipe, CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-games-list',
  imports: [AsyncPipe, CurrencyPipe],
  templateUrl: './games-list.html',
  styleUrl: './games-list.css',
})
export class GamesList {
  private gameService = inject(GameService);
  games$!: Observable<ApiResponse<PagedContent<Game>>>;

  constructor(){
    this.games$ = this.gameService.getGames();
  }
}
