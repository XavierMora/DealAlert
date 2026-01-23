import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private http = inject(HttpClient);

  getGames(): Observable<ApiResponse<PagedContent<Game>>>{
    return this.http.get(environment.apiUrl+"/games");
  }
}
