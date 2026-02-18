import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Alert } from './shared/components/alert/alert';
import { AuthService } from './auth/services/auth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Alert],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  private authService = inject(AuthService)

  constructor(){
    this.authService.loadCsrfToken().subscribe();
  }
}
