import { Component, inject, model, output, signal } from '@angular/core';
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
  timeoutErrorSendingForm!: number;

  signInCode(form:NgForm){   
    if(form.invalid || this.sending()) return;
    
    this.sending.set(true);
    this.authService.signInCode(this.email).pipe(
      finalize(() => this.sending.set(false))
    ).subscribe({
      next: () => {
        clearTimeout(this.timeoutErrorSendingForm);
        this.codeSent.emit(this.email);
      },
      error: (err: ApiResponse<undefined | Record<string, string>>) => {
        if(err === null || err.message === undefined){
          this.errorSendingForm.set('Error enviando el formulario.');
          return;
        };

        if(err.error === ApiAuthErrorCode.CODE_SENT_RECENTLY){
          this.codeSent.emit(this.email)
        }else if(err.error === ApiErrorCode.INVALID_DATA){
          let errorEmail = form.form.controls['email'];
          errorEmail.setErrors({apiError: err.data!['email']});
          errorEmail.markAllAsTouched();
        }else{
          this.errorSendingForm.set(err.message);
          this.timeoutErrorSendingForm = setTimeout(() => this.errorSendingForm.set(undefined), 3000);
        }
      }
    }) 
  }
}
