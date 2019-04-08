import {Injectable} from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, of, OperatorFunction, throwError} from "rxjs";
import {catchError, flatMap, map} from "rxjs/operators";

import {GeminiUriService} from "../common";
import {
    EntityRecord,
    entityRecordFromAPI,
    EntityRecordApi,
    EntityRecordApiList,
    EntityRecordList
} from "../schema/EntityRecord";
import {pipeFromArray} from "rxjs/internal/util/pipe";
import {ApiError} from "./api-error";
import {EntitySchema} from "../schema/entity-schema";
import {FieldSchema} from "../schema/field-schema";
import {GeminiSchemaService} from "../schema/schema.service";

@Injectable({
    providedIn: 'root'
})
export class GeminiEntityManagerService {

    private static DEFAULT_HTTP_HEADERS = {
        'Gemini': 'gemini.api'
    };

    schemaService: GeminiSchemaService;

    constructor(private configService: GeminiUriService,
                private http: HttpClient) {
    }


    public getEntityRecordJson(entityName: string, entityKey: any): Observable<EntityRecordApi> {
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        return pipeFromArray(GeminiEntityManagerService.commonHandler)(
            this.http.get<EntityRecordApi>(this.configService.getApiEntityRecordByKeyUrl(entityName, entityKey), options)
        );
    }

    public getEntityRecord(entity: EntitySchema | string, entityKey: any): Observable<EntityRecord> {
        // TODO add entitySchema also as entityName ?
        const httpHeaders: HttpHeaders = this.httpHeadersFromDefault();
        const options = {
            headers: httpHeaders
        };
        if (typeof entity == "string") {
            // we have entity name

            return pipeFromArray(GeminiEntityManagerService.commonHandler)(
                this.schemaService.getEntitySchema$(entity)
                    .pipe(flatMap(
                        entitySchema => {
                            return this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entitySchema.name, entityKey), options)
                                .pipe(map(e => entityRecordFromAPI(entitySchema, e)))
                        })));


            /* return this.schemaService.getEntitySchema$(entity)
                .pipe(flatMap(entitySchema => {
                    return pipeFromArray(this.handlerToEntityRecord(entitySchema))(
                        this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entitySchema.name, entityKey), options));
                })) */
        }
        if (entity instanceof EntitySchema) {
            const entitySchema = entity as EntitySchema;
            return pipeFromArray(this.handlerToEntityRecord(entitySchema))(
                this.http.get<EntityRecord>(this.configService.getApiEntityRecordByKeyUrl(entitySchema.name, entityKey), options));
        }
    }

    public getEntityRecordsInterface(entityName: string): Observable<EntityRecordApiList>;
    public getEntityRecordsInterface(entityName: string, searchFilter: any): Observable<EntityRecordApiList>;
    public getEntityRecordsInterface(entityName: string, searchFilter?: any): Observable<EntityRecordApiList> {
        // TODO add entityName also as schema ?
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

    public getEntityRecords(entityName: string): Observable<EntityRecordList> {
        return this.schemaService.getEntitySchema$(entityName)
            .pipe(flatMap(schema => {
                return this.getEntityRecordsInterface(entityName).pipe(
                    map(lists => {
                        let ret = {} as EntityRecordList;
                        ret.entitySchema = schema;
                        ret.data = [];
                        ret.meta = lists.meta;
                        for (const er of lists.data) {
                            ret.data.push(new EntityRecord(schema, er));
                        }
                        return ret;
                    })
                )
            }));
    }


    public creteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.CREATE);
    }

    public updateEntityRecord(entityRecord: EntityRecord, uuid?: string): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.UPDATE, uuid);
    }

    public deleteEntityRecord(entityRecord: EntityRecord): Observable<EntityRecord> {
        return this.handleEntityRecordAPI(entityRecord, OperationType.DELETE);
    }

    private handleEntityRecordAPI(entityRecord: EntityRecord, opeType: OperationType, uuid?: string): Observable<EntityRecord> {
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
                url = this.configService.getApiEntityRecordByKeyUrl(entityRecord.entitySchema.name, this.extractEntityKeyOrUUID(entityRecord, uuid));
                break;
            case OperationType.UPDATE:
                method = "put";
                url = this.configService.getApiEntityRecordByKeyUrl(entityRecord.entitySchema.name, this.extractEntityKeyOrUUID(entityRecord, uuid));
                break;
        }
        return pipeFromArray(this.handlerToEntityRecord(entityRecord.entitySchema))(
            this.http[method]<EntityRecord>(url, entityRecord.toJsonObject(), options)
        );
    }

    private extractEntityKeyOrUUID(entityRecord: EntityRecord, uuid?: string) {
        const entitySchema = entityRecord.entitySchema;
        console.log(entityRecord);
        if (uuid != null) {
            return uuid;
        }
        if (entityRecord.meta['uuid'] != null) {
            return entityRecord.meta.uuid;
        }
        let logicalKeyFields: FieldSchema[] = entitySchema.getLogicalKeyFields();
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


    private static handleError(error: HttpErrorResponse): Observable<never> {
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
