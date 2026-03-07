import { Routes } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';
import { LoginPage } from './auth/pages/login-page/login-page';
import { PriceChangeAlertsPage } from './price-change-alerts/pages/price-change-alerts-page/price-change-alerts-page';
import { authGuard } from './shared/guards/auth-guard';
import { noAuthGuard } from './shared/guards/no-auth-guard';
import { HomePage } from './home/pages/home-page/home-page';
import { NotFoundPage } from './not-found/not-found-page';
export const routes: Routes = [
    {
        path: '', 
        component: HomePage
    },
    {
        path: 'games',
        component: GamesPage
    },
    {
        path: 'login', 
        component: LoginPage,
        canActivate: [noAuthGuard]
    },
    {
        path: 'deal-alerts',
        component: PriceChangeAlertsPage,
        canActivate: [authGuard]
    },
    {
        path: '**',
        component: NotFoundPage
    }
];
