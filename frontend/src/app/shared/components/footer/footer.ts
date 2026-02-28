import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  imports: [],
  template: `
  <footer class="border-t border-(--surface-2) py-6 text-center text-(--text-muted) text-sm w-full">
    <p>PriceWatch</p>
    <p class="mt-1">Proyecto personal. No afiliado con Steam.</p>
  </footer>
  `
})
export class Footer {

}
