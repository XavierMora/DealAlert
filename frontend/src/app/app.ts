import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';

@Component({
  selector: 'app-root',
  imports: [GamesPage],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
