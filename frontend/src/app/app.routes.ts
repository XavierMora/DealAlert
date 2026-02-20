import { Routes } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';
import { LoginPage } from './auth/pages/login-page/login-page';
import { PriceChangeAlertsPage } from './price-change-alerts/pages/price-change-alerts-page/price-change-alerts-page';
export const routes: Routes = [
    {
        path: '', 
        component: GamesPage
    },
    {
        path: 'login', 
        component: LoginPage
    },
    {
        path: 'price-alerts',
        component: PriceChangeAlertsPage
    }
];
