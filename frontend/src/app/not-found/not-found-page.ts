import { Component } from '@angular/core';

@Component({
  selector: 'app-not-found-page',
  imports: [],
  template: `
  <div class="min-h-[70vh] flex items-center justify-center px-6">
    <div class="text-center max-w-xl">

      <h1 class="text-7xl font-extrabold text-(--main) mb-4">
        404
      </h1>

      <h2 class="text-2xl md:text-3xl font-semibold text-(--text-main) mb-4">
        Página no encontrada
      </h2>

      <button (click)="_history.back()" class="main-button">
          Volver
      </button>
    </div>  
  </div>
  `
})
export class NotFoundPage {
  _history = history
}
