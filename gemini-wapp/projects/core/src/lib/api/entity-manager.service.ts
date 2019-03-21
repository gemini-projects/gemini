import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, of, OperatorFunction, throwError} from "rxjs";
import {catchError, retry} from "rxjs/operators";

import {GeminiUriService} from "../common";
import {EntityRecord} from "../schema/EntityRecord";
import {pipeFromArray} from "rxjs/internal/util/pipe";

@Injectable({
    providedIn: 'root'
})
export class EntityManagerService {

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
        /* return this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entityName, entityKey), options)
            .pipe(retry(3),
                catchError(EntityManagerService.handleError)); */

        return pipeFromArray(EntityManagerService.commonHandler)(this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entityName, entityKey), options));
    }

    public getEntityRecords(entityName: string): Observable<EntityRecord>;
    public getEntityRecords(entityName: string, searchFilter: any): Observable<EntityRecord>;
    public getEntityRecords(entityName: string, searchFilter?: any): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        let httpParams = new HttpParams();
        httpParams = searchFilter ? httpParams.set(this.configService.SEARCH_PARAMETER, searchFilter) : httpParams;
        const options = {
            headers: httpHeaders,
            params: httpParams
        };
        return this.http.get<EntityRecord>(this.configService.getApiEntityRootUrl(entityName), options)
            .pipe(
                retry(3),
                catchError(EntityManagerService.handleError)
            );
    }

    public createOrUpdateEntityRecord(entityName: string): Observable<EntityRecord> {
        // TODO push value
        return of({
            meta: {},
            data: {code: entityName}
        } as EntityRecord);
    }

    /* public getEntityList(entityName: string, entityKey: any, entityCollection?: string): Observable<EntityRecord[]> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        // TODO entity key to string
        return this.http.get<EntityRecord[]>(this.configService.getApiEntityCollectionUrl(entityName, entityKey, entityCollection), options)
            .pipe(retry(3),
                catchError(this.handleError));
    } */

    private httpHeadersFromDefault(): HttpHeaders {
        return new HttpHeaders(EntityManagerService.DEFAULT_HTTP_HEADERS);
    }


    private static handleError(error: HttpErrorResponse) {
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

    private static commonHandler: OperatorFunction<any, any>[] =
        [retry(3), catchError(EntityManagerService.handleError)];
}
