import { Component, inject } from '@angular/core';
import { PriceChangeAlertsList } from '../../components/price-change-alerts-list/price-change-alerts-list';
import { AuthService } from '../../../auth/services/auth-service';

@Component({
  selector: 'app-price-change-alerts-page',
  imports: [PriceChangeAlertsList],
  templateUrl: './price-change-alerts-page.html',
  styleUrl: './price-change-alerts-page.css',
})
export class PriceChangeAlertsPage {
  private authService = inject(AuthService);
  currentAccount = this.authService.currentAccount
}
