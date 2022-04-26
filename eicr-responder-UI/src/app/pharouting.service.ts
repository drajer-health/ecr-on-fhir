import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subject, BehaviorSubject } from 'rxjs';
import { Pharouting } from './pharouting';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { AppConfigService } from './AppConfigService';

interface Config {
    url: string;
  }

  export interface IAppConfig {
      baseUrl: string;
      baseDMUrl: string;
      baseStandardUrl: string;
      load: () => Promise<void>;
    }

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
    // 'Access-Control-Allow-Origin': '*',
   })
};

@Injectable({
  providedIn: 'root'
})

@Injectable()
export class PharoutingService
 {
  private baseUrl = 'http://localhost:8080/eicrresponder';

  constructor (private environment: AppConfigService, private http: HttpClient) {
    this.baseUrl = environment.config.url;
    console.log(" this.baseUrl environment 5555 =::::::"+this.baseUrl);
}

  private _pharouting: Subject<Array<Pharouting>> = new BehaviorSubject<Array<Pharouting>>([]);
  public readonly pharouting: Observable<Array<Pharouting>> = this._pharouting.asObservable();

  createPhaRouting(pharouting: object): Observable<object> {
     console.log("PHA Routing before sending::::" + JSON.stringify(pharouting))
    return this.http.post(`${this.baseUrl}` + "/api/pha", JSON.stringify(pharouting), httpOptions);
  }

  getPharoutingList(): Observable<any> {
    return this.http.get(`${this.baseUrl}` + "/api/phalists");
  }

}