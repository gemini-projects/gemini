import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpParams} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {catchError, retry} from "rxjs/operators";

import {GeminiUriService} from "../common";

@Injectable({
    providedIn: 'root'
})
export class GeminiApiService {

    constructor(private configService: GeminiUriService, private http: HttpClient) {
    }

    public getEntityRecord(entityName: string, entityKey: any): Observable<any> {
        return this.http.get(this.configService.getApiEntityUrl(entityName, entityKey))
            .pipe(
                retry(3),
                catchError(this.handleError)
            );
    }

    public getEntitiesMatchingFilter(entityName: string, searchValue: any): Observable<any> {
        let httpParams = new HttpParams();
        httpParams.set(this.configService.SEARCH_PARAMETER, searchValue);
        return this.http.get(this.configService.getApiEntitiesUrl(entityName))
            .pipe(
                retry(3),
                catchError(this.handleError)
            );
    }

    private handleError(error: HttpErrorResponse) {
        if (error.error instanceof ErrorEvent) {
            // A client-side or network error occurred. Handle it accordingly.
            console.error('An error occurred:', error.error.message);
        } else {
            // The backend returned an unsuccessful response code.
            // The response body may contain clues as to what went wrong,
            console.error(
                `Backend returned code ${error.status}, ` +
                `body was: ${error.error}`);
        }
        // return an observable with a user-facing error message
        return throwError(
            'Something bad happened; please try again later.');
    };
}
