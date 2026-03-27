import { Component, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-games-search-bar',
  imports: [FormsModule],
  templateUrl: './games-search-bar.html',
  styleUrl: './games-search-bar.css',
})
export class GamesSearchBar {
  newName = output<string>();
  lastEvent: number | undefined = undefined;

  emitNewName(value: string){
    if(this.lastEvent !== undefined) clearTimeout(this.lastEvent);
    
    // Se notifica sobre el nombre que se busca si pasaron 200ms sin un nuevo evento
    this.lastEvent = setTimeout(() => {
      this.newName.emit(value.trim());
    }, 200); 
  }
}
