import { Component, inject, input, output, signal } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize, Subject } from 'rxjs';
import { ApiErrorCode } from '../../../shared/models/ApiErrorCode';
import { Router } from '@angular/router';

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
  router = inject(Router);

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
}
