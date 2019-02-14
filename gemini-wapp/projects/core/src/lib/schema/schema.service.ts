import {Injectable} from '@angular/core';
import {EntitySchema} from "./entity-schema";
import {FieldSchema, FieldType} from "./field-schema";
import {GeminiValueStrategy} from "./gemini-value-strategy";
import {Observable, of} from "rxjs";
import {GeminiApiService} from "../api";
import {map} from "rxjs/operators";
import {EntityRecord} from "./EntityRecord";

@Injectable({
    providedIn: 'root',
})
export class GeminiSchemaService {
    private static ENTITY_NAME_OF_ENTITIES: string = "entity";
    private static ENTITY_NAME_OF_FIELDS: string = "field";

    private entityCache: { [key: string]: EntitySchema };

    constructor(private apiService: GeminiApiService) {
        this.entityCache = {}
    }

    getEntitySchema(entityName: string): Observable<EntitySchema> {
        return this.apiService.getEntityRecord(GeminiSchemaService.ENTITY_NAME_OF_ENTITIES, entityName)
            .pipe(
                map((entityRecord: EntityRecord) => {

                    return entityRecord.data as EntitySchema;
                })
            );
        // return of({name: "Entity", displayname: "ENNN"});
    }

    getEntityFields(entityName: string): Observable<FieldSchema[]> {
        return this.apiService.getEntityList(GeminiSchemaService.ENTITY_NAME_OF_ENTITIES, entityName, GeminiSchemaService.ENTITY_NAME_OF_FIELDS)
            .pipe(
                map((entityRecord: EntityRecord[]) => {


                    return [] as FieldSchema[];
                }));

        /* return of([{
            name: "required text",
            type: FieldType.TEXT,
            requiredStrategy: GeminiValueStrategy.SIMPLE,
            visibleStrategy: GeminiValueStrategy.SIMPLE,
            modifiableStrategy: GeminiValueStrategy.SIMPLE,
            required: true,
            visible: true
        }, {
            name: "not required Long",
            type: FieldType.LONG,
            requiredStrategy: GeminiValueStrategy.SIMPLE,
            visibleStrategy: GeminiValueStrategy.SIMPLE,
            modifiableStrategy: GeminiValueStrategy.SIMPLE,
            visible: true
        }, {
            name: "not required Double",
            type: FieldType.DOUBLE,
            requiredStrategy: GeminiValueStrategy.SIMPLE,
            visibleStrategy: GeminiValueStrategy.SIMPLE,
            modifiableStrategy: GeminiValueStrategy.SIMPLE,
            visible: true
        }]); */
    }

}
