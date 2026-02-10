import { Component, signal, ViewEncapsulation } from '@angular/core';
import { Login } from '../../components/login/login';
import { VerifyCode } from '../../components/verify-code/verify-code';

@Component({
  selector: 'app-login-page',
  imports: [Login, VerifyCode],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css'
})
export class LoginPage {
  email = signal<string | undefined>(undefined)
}
