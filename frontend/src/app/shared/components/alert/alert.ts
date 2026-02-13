import { Component, inject, signal } from '@angular/core';
import { AlertService } from './alert-service';

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.html',
  styleUrl: './alert.css' 
})
export class Alert {
  private alertService = inject(AlertService);
  alertData!: AlertData;
  show = signal<boolean>(false);
  showTimeout: number | undefined = undefined;

  constructor(){
    this.alertService.showAlert.subscribe(data => {
      this.alertData = data;
      this.show.set(true);
      if(this.showTimeout != undefined) clearTimeout(this.showTimeout);
      this.showTimeout = setTimeout(() => this.show.set(false), 4000);
    })
  }
}
