import { Component, inject, output, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { finalize } from 'rxjs';
import { ApiAuthErrorCode } from '../../model/ApiAuthErrorCode';
import { ApiErrorCode } from '../../../shared/models/ApiErrorCode';

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
    if(form.invalid || this.sending()) return;
    
    this.sending.set(true);
    this.authService.signInCode(this.email).pipe(
      finalize(() => this.sending.set(false))
    ).subscribe({
      next: () => {
        this.codeSent.emit(this.email);
      },
      error: (err: ApiResponse<undefined | Record<string, string>>) => {
        if(err === null || err.message === undefined){
          this.errorSendingForm.set('Error enviando el formulario.');
          return;
        };

        switch(err.error){
          case ApiAuthErrorCode.CODE_SENT_RECENTLY:
            this.errorSendingForm.set(undefined);
            this.codeSent.emit(this.email)
            break;
          case ApiErrorCode.INVALID_DATA:
            this.errorSendingForm.set(undefined);
            let errorEmail = form.form.controls['email'];
            errorEmail.setErrors({apiError: err.data!['email']});
            errorEmail.markAllAsTouched();
            break;
          default:
            this.errorSendingForm.set(err.message);
            break;
        }
      }
    }) 
  }
}
