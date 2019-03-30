import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, OperatorFunction, throwError} from "rxjs";
import {catchError, map, retry} from "rxjs/operators";

import {GeminiUriService} from "../common";
import {EntityRecord, entityRecordFromAPI, EntityRecordInterface} from "../schema/EntityRecord";
import {pipeFromArray} from "rxjs/internal/util/pipe";
import {ApiError} from "./api-error";
import {EntitySchema} from "../schema/entity-schema";
import {FieldSchema} from "../schema/field-schema";

@Injectable({
    providedIn: 'root'
})
export class GeminiEntityManagerService {

    private static DEFAULT_HTTP_HEADERS = {
        'Gemini': 'gemini.api'
    };

    constructor(private configService: GeminiUriService, private http: HttpClient) {
    }


    public getEntityRecordJson(entityName: string, entityKey: any): Observable<EntityRecordInterface> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        return pipeFromArray(GeminiEntityManagerService.commonHandler)(
            this.http.get<EntityRecordInterface>(this.configService.getApiEntityRecordByKeyUrl(entityName, entityKey), options)
        );
    }

    public getEntityRecord(entitySchema: EntitySchema, entityKey: any): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        return pipeFromArray(this.handlerToEntityRecord(entitySchema))(
            this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entitySchema.name, entityKey), options));
    }

    public getEntityRecords(entityName: string): Observable<EntityRecordInterface>;
    public getEntityRecords(entityName: string, searchFilter: any): Observable<EntityRecordInterface>;
    public getEntityRecords(entityName: string, searchFilter?: any): Observable<EntityRecordInterface> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        let httpParams = new HttpParams();
        httpParams = searchFilter ? httpParams.set(this.configService.SEARCH_PARAMETER, searchFilter) : httpParams;
        const options = {
            headers: httpHeaders,
            params: httpParams
        };
        return pipeFromArray(GeminiEntityManagerService.commonHandler)(
            this.http.get<EntityRecord>(this.configService.getApiEntityRootUrl(entityName), options));
    }

    public creteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.CREATE);
    }

    public updateEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.UPDATE);
    }

    public deleteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.DELETE);
    }

    private handleEntityRecordAPI(entityRecord: EntityRecord, opeType: OperationType): Observable<EntityRecord> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders,
        };
        let method: string;
        let url: string;
        switch (opeType) {
            case OperationType.CREATE:
                method = "post";
                url = this.configService.getApiEntityRootUrl(entityRecord.entitySchema.name);
                break;
            case OperationType.DELETE:
                method = "delete";
                url = this.configService.getApiEntityRecordByKeyUrl(entityRecord.entitySchema.name, this.extractEntityKey(entityRecord));
                break;
            case OperationType.UPDATE:
                method = "put";
                url = this.configService.getApiEntityRecordByKeyUrl(entityRecord.entitySchema.name, this.extractEntityKey(entityRecord));
                break;
        }
        return pipeFromArray(this.handlerToEntityRecord(entityRecord.entitySchema))(
            this.http[method]<EntityRecord>(url, entityRecord.toJsonObject(), options)
        );
    }

    private extractEntityKey(entityRecord: EntityRecord) {
        const entitySchema = entityRecord.entitySchema;
        let logicalKeyFields: FieldSchema[] = entitySchema.getLogicalKeyFields();
        // TODO uuid
        if (logicalKeyFields.length == 1) {
            return entityRecord.data[logicalKeyFields[0].name];
        }
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


    private static handleError(error: HttpErrorResponse): Observable<ApiError> {
        console.error(error);
        // return an observable with a user-facing error message
        return throwError(error.error as ApiError);
    };

    private static commonHandler: OperatorFunction<any, any>[] =
        [catchError(GeminiEntityManagerService.handleError)];

    private handlerToEntityRecord(entitySchema: EntitySchema) {
        return GeminiEntityManagerService.commonHandler.concat(map((e: EntityRecord) => entityRecordFromAPI(entitySchema, e)));
    }
}


enum OperationType {
    CREATE,
    DELETE,
    UPDATE
}
