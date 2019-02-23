import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, OperatorFunction, throwError} from "rxjs";
import {catchError, retry} from "rxjs/operators";

import {GeminiUriService} from "../common";
import {EntityRecord} from "../schema/EntityRecord";

@Injectable({
    providedIn: 'root'
})
export class GeminiApiService {

    private static DEFAULT_HTTP_HEADERS = {
        'Gemini': 'gemini.api'
    };

    constructor(private configService: GeminiUriService, private http: HttpClient) {
    }

    public getEntityRecord(entityName: string, entityKey: any): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        return this.http.get<EntityRecord>(this.configService.getApiEntityUrl(entityName, entityKey), options)
            .pipe(retry(3),
                catchError(this.handleError));
    }

    public getEntityList(entityName: string, entityKey: any, entityCollection?: string): Observable<EntityRecord[]> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        // TODO entity key to string
        return this.http.get<EntityRecord[]>(this.configService.getApiEntityCollectionUrl(entityName, entityKey, entityCollection), options)
            .pipe(retry(3),
                catchError(this.handleError));
    }

    private httpHeadersFromDefault(): HttpHeaders {
        return new HttpHeaders(GeminiApiService.DEFAULT_HTTP_HEADERS);
    }

    public getEntitiesMatchingFilter(entityName: string, searchValue: any): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        let httpParams = new HttpParams()
            .set(this.configService.SEARCH_PARAMETER, searchValue);
        const options = {
            headers: httpHeaders,
            params: httpParams
        };
        return this.http.get(this.configService.getApiEntitiesUrl(entityName), options)
            .pipe(
                retry(3),
                catchError(this.handleError)
            );
    }

    private commonHandler(): OperatorFunction<any, any>[] {
        return [retry(3), catchError(this.handleError)];
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
