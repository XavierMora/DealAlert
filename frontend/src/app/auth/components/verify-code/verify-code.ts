import { Component, inject, input, output, signal } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize, Subject } from 'rxjs';
import { ApiErrorCode } from '../../../shared/models/ApiErrorCode';
import { Router } from '@angular/router';
import { AlertService } from '../../../shared/components/alert/alert-service';
import { ApiAuthErrorCode } from '../../model/ApiAuthErrorCode';

@Component({
  selector: 'app-verify-code',
  imports: [FormsModule],
  templateUrl: './verify-code.html',
  styleUrl: './verify-code.css',
})
export class VerifyCode {
  private authService = inject(AuthService);
  email = input.required<string>();
  code: string = '';
  sending = signal<boolean>(false);
  loginSuccess = output<boolean>();
  errorSendingForm = signal<string | undefined>(undefined);
  private router = inject(Router);
  private alertService = inject(AlertService);

  verifyCode(form: NgForm){
    if(form.invalid || this.sending()) return;
      
    this.sending.set(true);
    this.authService.verifyCode(this.email(), this.code).pipe(
      finalize(() => this.sending.set(false))
    ).subscribe({
      next: () => {
        this.router.navigateByUrl('/games');
      },
      error: (err: ApiResponse<undefined | Record<string, string>>) => {
        if(err.error === ApiErrorCode.INVALID_DATA){
          this.errorSendingForm.set(undefined);
          let errorCode = form.form.controls['code'];
          errorCode.setErrors({apiError: err.data !== undefined ? err.data['code'] : err.message});
          errorCode.markAsTouched();
        }else{
          this.errorSendingForm.set(err.message);
        }
      }
    })
  }

  replaceNotNumbers(e:any){
    e.target.value = e.target.value.replace(/[^0-9]/g, '')
  }

  resendCode(){
    this.authService.signInCode(this.email()).subscribe({
      next: () => this.alertService.newAlert({
        type: 'success',
        text: 'Código reenviado.'
      }),
      error: (err) => {
        if(err.error === ApiAuthErrorCode.CODE_SENT_RECENTLY){
          this.alertService.newAlert({
            type: 'error',
            text: 'Un código fue solicitado recientemente. Intentar más tarde.'
          })
        }else{
          this.alertService.newAlert({
            type: 'error',
            text: 'Error reenviando código.'
          })
        }
      }
    })
  }
}
