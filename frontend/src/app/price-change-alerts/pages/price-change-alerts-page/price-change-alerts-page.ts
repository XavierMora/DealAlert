import { Component } from '@angular/core';
import { PriceChangeAlertsList } from '../../components/price-change-alerts-list/price-change-alerts-list';

@Component({
  selector: 'app-price-change-alerts-page',
  imports: [PriceChangeAlertsList],
  templateUrl: './price-change-alerts-page.html',
  styleUrl: './price-change-alerts-page.css',
})
export class PriceChangeAlertsPage {

}
