import { Routes } from '@angular/router';
import { GamesPage } from './games/pages/games-page/games-page';
import { Login } from './auth/components/login/login';
export const routes: Routes = [
    {
        path: '', 
        component: GamesPage
    },
    {
        path: 'login', 
        component: Login
    }
];
