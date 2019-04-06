import {Injectable} from '@angular/core';
import {EntitySchema} from "./entity-schema";
import {FieldSchema} from "./field-schema";
import {Observable} from "rxjs";
import {GeminiEntityManagerService} from "../api";
import {flatMap, map} from "rxjs/operators";
import {EntityRecord} from "./EntityRecord";

@Injectable({
    providedIn: 'root',
})
export class GeminiSchemaService {
    private static ENTITY_NAME_OF_ENTITIES: string = "entity";
    private static ENTITY_NAME_OF_FIELDS: string = "field";

    private entityCache: { [key: string]: EntitySchema };

    constructor(private apiService: GeminiEntityManagerService) {
        this.entityCache = {};
        this.apiService.schemaService = this; // to avoid circular dependency
    }

    getEntitySchema$(entityName: string): Observable<EntitySchema> {
        return this.getEntityFields(entityName)
            .pipe(
                flatMap((fsArray: FieldSchema[]) => {
                    return this.apiService.getEntityRecordJson(GeminiSchemaService.ENTITY_NAME_OF_ENTITIES, entityName)
                        .pipe(
                            map((entityRecord: EntityRecord) => {
                                return new EntitySchema(entityRecord.data, fsArray);
                            })
                        );
                }));
    }

    private getEntityFields(entityName: string): Observable<FieldSchema[]> {
        const search: string = `entity==${entityName.toUpperCase()}`;
        return this.apiService.getEntityRecordsInterface(GeminiSchemaService.ENTITY_NAME_OF_FIELDS, search)
            .pipe(
                map((entityRecord: EntityRecord) => {
                    let fieldsEntityRec = entityRecord.data as EntityRecord[];
                    return fieldsEntityRec.map<FieldSchema>(fsr => {
                        return fsr.data as FieldSchema
                    });
                }));
    }
}
