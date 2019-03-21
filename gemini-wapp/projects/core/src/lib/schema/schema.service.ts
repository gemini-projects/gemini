import {Injectable} from '@angular/core';
import {EntitySchema} from "./entity-schema";
import {FieldSchema} from "./field-schema";
import {Observable} from "rxjs";
import {EntityManagerService} from "../api";
import {map} from "rxjs/operators";
import {EntityRecord} from "./EntityRecord";

@Injectable({
    providedIn: 'root',
})
export class GeminiSchemaService {
    private static ENTITY_NAME_OF_ENTITIES: string = "entity";
    private static ENTITY_NAME_OF_FIELDS: string = "field";

    private entityCache: { [key: string]: EntitySchema };

    constructor(private apiService: EntityManagerService) {
        this.entityCache = {}
    }

    getEntitySchema(entityName: string): Observable<EntitySchema> {
        return this.apiService.getEntityRecord(GeminiSchemaService.ENTITY_NAME_OF_ENTITIES, entityName)
            .pipe(
                map((entityRecord: EntityRecord) => {
                    // entity schema is one to one with api data
                    return entityRecord.data as EntitySchema;
                })
            );
    }

    getEntityFields(entityName: string): Observable<FieldSchema[]> {
        const search: string = `entity==${entityName.toUpperCase()}`;
        return this.apiService.getEntityRecords(GeminiSchemaService.ENTITY_NAME_OF_FIELDS, search)
            .pipe(
                map((entityRecord: EntityRecord) => {
                    let fieldsEntityRec = entityRecord.data as EntityRecord[];
                    let fielsSchemas: FieldSchema[] = fieldsEntityRec.map<FieldSchema>(fsr => {
                        return fsr.data as FieldSchema
                    });
                    return fielsSchemas;
                }));
    }

}
