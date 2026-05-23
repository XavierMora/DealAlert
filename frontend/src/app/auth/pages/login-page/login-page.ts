import { Component, signal } from '@angular/core';
import { Login } from '../../components/login/login';
import { VerifyCode } from '../../components/verify-code/verify-code';
import { TelegramLink } from '../../components/telegram-link/telegram-link';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-page',
  imports: [Login, VerifyCode, TelegramLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css'
})
export class LoginPage {
  email = signal<string | undefined>(undefined);
  showTelegramLink = signal<boolean>(false);

  constructor(private router: Router){}

  goToGames(){
    this.router.navigateByUrl('/games');
  }
}
