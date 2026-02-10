import { Routes } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';
import { LoginPage } from './auth/pages/login-page/login-page';
export const routes: Routes = [
    {
        path: '', 
        component: GamesPage
    },
    {
        path: 'login', 
        component: LoginPage
    }
];
