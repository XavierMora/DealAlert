import { Component, effect, input, InputSignal, linkedSignal, model, output, WritableSignal } from '@angular/core';
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-pagination',
  imports: [FormsModule],
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  totalPages: InputSignal<number> = input.required<number>();
  actualPage = model<number>(1);
  readonly maxPages: number = 5;  

  pages: WritableSignal<number[] | undefined> = linkedSignal<number, number[]>({
    source: this.actualPage,
    computation: (page, prevPages) => {
      let totalPages = this.totalPages();
      if(totalPages == 0) return [];
      
      let pages: number[] | undefined = prevPages?.value;
      let maxPages = Math.min(this.maxPages, totalPages)
      // Inicialización del array si no existe o la nueva longitud es distinta a la anterior
      if(pages === undefined || pages.length != maxPages){
        pages = []
        let index = 0;
        for (let page = 1; page <= maxPages; page++) {
          pages[index] = page;
          index++;
        }
        return pages;
      }
      
      window.scroll(0,0);

      if(page == 1 && pages[0] > 1){ 
        // Salto a primera página
        // También resetea el array si cambia totalPages y se mantiene la longitud del anterior sino se establece en el if anterior
        return pages.map((_, i) => 1+i)
      }else if(page == totalPages && pages[0] <= (totalPages-this.maxPages)){
        // Salto a última página
        return pages.map((_, i) => (totalPages-this.maxPages)+i+1)
      }

      if(page == pages[pages.length-1]+1){ // Siguiente página
        pages.shift()
        pages.push(page)
      }else if(page == pages[0]-1){ // Anterior página
        pages.pop();
        pages.unshift(page);
      }

      return pages;
    }
  });

  previousPage(){
    this.actualPage.update(page => page > 1 ? page-1 : page);
  }

  nextPage(){
    this.actualPage.update(page => page < this.totalPages() ? page+1 : page);
  }
}