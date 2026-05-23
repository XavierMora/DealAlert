import { Component, computed, inject, input, output, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../services/auth-service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-telegram-link',
  templateUrl: './telegram-link.html',
  styleUrl: './telegram-link.css',
})
export class TelegramLink {
  linked = output<void>();
  closed = output<void>();

  private authService = inject(AuthService);
  token = signal<string | undefined>(undefined);
  loadingToken = signal<boolean>(false);
  checking = signal<boolean>(false);
  error = signal<string | undefined>(undefined);

  telegramUrl = computed(() => {
    const token = this.token();
    if(token === undefined) return undefined;

    return `https://t.me/${environment.telegramBotUsername}?start=${token}`;
  });

  linkTelegram(){
    if(this.loadingToken()) return;

    this.loadingToken.set(true);
    this.error.set(undefined);

    this.authService.telegramToken().pipe(
      finalize(() => this.loadingToken.set(false))
    ).subscribe({
      next: (res) => {
        this.token.set(res.data!.token);
        this.openTelegram();
      },
      error: (err: ApiResponse<undefined>) => {
        this.error.set(err.message ?? 'Error inesperado.');
      }
    });
  }

  openTelegram(){
    const url = this.telegramUrl();
    if(url === undefined) return;

    window.open(url, '_blank', 'noopener,noreferrer');
  }

  async checkLink(){
    if(this.checking()) return;

    this.checking.set(true);
    this.error.set(undefined);

    await this.authService.refreshAccount();
    this.checking.set(false);

    if(this.authService.currentAccount()?.telegramUserId != null){
      this.linked.emit();
      return;
    }

    this.error.set('Todavía no se detectó la vinculación.');
  }

  close(){
    this.closed.emit();
  }
}
