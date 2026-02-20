import { Routes } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';
import { LoginPage } from './auth/pages/login-page/login-page';
import { PriceChangeAlertsList } from './price-change-alerts/components/price-change-alerts-list/price-change-alerts-list';
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
        component: PriceChangeAlertsList
    }
];
