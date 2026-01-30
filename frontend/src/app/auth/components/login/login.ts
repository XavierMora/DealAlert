import { Component, inject, model, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { catchError, map } from 'rxjs';
import { required } from '@angular/forms/signals';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authService = inject(AuthService);
  email: string = '';
  codeSended = signal<boolean>(false);
  emailAlreadySent = signal<string | undefined>(undefined);

  login(form:NgForm){
    const fields = form.form.controls;
    let error = fields['email'];

    if(error.errors === null){
      this.authService.login(this.email).subscribe({
        next: () => this.codeSended.set(true),
        error: (err: ApiResponse<undefined | Record<string, string>>) => {
          if(err.data === undefined){
            this.emailAlreadySent.set(err.message);
          }else{
            fields['email'].setErrors({apiError: err.data!['email']});
            fields['email'].markAllAsTouched();
          }
        }
      }) 
    }
  }
}
