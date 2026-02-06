import { Component, inject, model, output, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authService = inject(AuthService);
  email: string = '';
  codeSent = output<string>();
  sending = signal<boolean>(false);
  errorSendingForm = signal<string | undefined>(undefined);

  signInCode(form:NgForm){    
    if(form.invalid && this.sending()) return;
    
    this.sending.set(true);
    this.authService.signInCode(this.email).pipe(
      finalize(() => this.sending.set(false))
    ).subscribe({
      next: () => this.codeSent.emit(this.email),
      error: (err: ApiResponse<undefined | Record<string, string>>) => {
        if(err === null){ // Error por rate limit
          this.errorSendingForm.set("Error enviando el formulario. Intentar nuevamente.")
          setTimeout(() => this.errorSendingForm.set(undefined), 3000)
        }else if(err.data === undefined){ // error, ya se envió un código
          this.codeSent.emit(this.email)
        }else{
          let errorEmail = form.form.controls['email'];
          errorEmail.setErrors({apiError: err.data!['email']});
          errorEmail.markAllAsTouched();
        }
      }
    }) 
  }
}
