import { CurrencyPipe, NgOptimizedImage } from '@angular/common';
import { Component, input } from '@angular/core';
import { ButtonPriceAlert } from '../button-price-alert/button-price-alert';
import { SteamStoreLink } from '../steam-store-link/steam-store-link';

@Component({
  selector: 'app-game-card',
  imports: [CurrencyPipe, ButtonPriceAlert, SteamStoreLink, NgOptimizedImage],
  templateUrl: './game-card.html',
  styleUrl: './game-card.css',
})
export class GameCard {
  game = input.required<Game>();
}
