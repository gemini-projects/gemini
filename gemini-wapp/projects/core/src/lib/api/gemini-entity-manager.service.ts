import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, OperatorFunction, throwError} from "rxjs";
import {catchError, map, retry} from "rxjs/operators";

import {GeminiUriService} from "../common";
import {EntityRecord, entityRecordFromAPI} from "../schema/EntityRecord";
import {pipeFromArray} from "rxjs/internal/util/pipe";
import {ApiError} from "./api-error";

@Injectable({
    providedIn: 'root'
})
export class GeminiEntityManagerService {

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
        return pipeFromArray(GeminiEntityManagerService.commonHandler)(
            this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entityName, entityKey), options)
        );
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
                catchError(GeminiEntityManagerService.handleError)
            );
    }

    public creteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.CREATE);
    }

    public updateEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.DELETE);
    }

    public deleteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.DELETE);
    }

    private handleEntityRecordAPI(entityRecord: EntityRecord, opeType: OperationType): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders,
        };
        let entitySchema = entityRecord.entitySchema()!;

        let method: string;
        switch (opeType) {
            case OperationType.CREATE:
                method = "post";
                break;
            case OperationType.DELETE:
                method = "delete";
                break;
            case OperationType.UPDATE:
                method = "put";
                break;

        }

        return pipeFromArray(GeminiEntityManagerService.commonHandler.concat(map((e: EntityRecord) => entityRecordFromAPI(entitySchema, e))))(
            this.http[method]<EntityRecord>(this.configService.getApiEntityRootUrl(entitySchema.name), entityRecord.toJsonObject(), options)
        );
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
        return new HttpHeaders(GeminiEntityManagerService.DEFAULT_HTTP_HEADERS);
    }


    private static handleError(error: HttpErrorResponse) {
        console.error(error);
        // return an observable with a user-facing error message
        return throwError(error.error as ApiError);
    };

    private static commonHandler: OperatorFunction<any, any>[] =
        [catchError(GeminiEntityManagerService.handleError)];
}


enum OperationType {
    CREATE,
    DELETE,
    UPDATE
}
