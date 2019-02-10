import {Injectable} from '@angular/core';
import {EntitySchema} from "./entity-schema";
import {FieldSchema, FieldType} from "./field-schema";
import {GeminiValueStrategy} from "./gemini-value-strategy";
import {Observable, of} from "rxjs";
import {GeminiApiService} from "../api";

@Injectable({
    providedIn: 'root',
})
export class GeminiSchemaService {

    private entityCache: { [key: string]: EntitySchema };

    constructor(private apiService: GeminiApiService) {
        this.entityCache = {}
    }

    getEntitySchema(entityName: string): Observable<EntitySchema> {
        return this.apiService.getEntityRecord("entity", entityName);
       // return of({name: "Entity", displayname: "ENNN"});
    }

    getEntityFields(entityName: string): FieldSchema[] {
        return [{
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
        }]
    }

}
