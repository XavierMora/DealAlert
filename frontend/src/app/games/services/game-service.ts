import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private http = inject(HttpClient);

  getGames(size: number, page: number, name?: string): Observable<ApiResponse<PagedContent<Game>>>{
    let params = new HttpParams().set('size', size).set('page', page)

    if(name !== undefined) params = params.append('name', name);

    return this.http.get<ApiResponse<PagedContent<Game>>>(`${environment.apiUrl}/games`, {
      params,
      credentials: 'include'
    });
  }
}
