import {Injectable} from '@angular/core';

import {GeminiConfigService} from "../common";
import {EntitySchema} from "./entity-schema";
import {FieldType} from "./field-schema";
import {GeminiValueStrategy} from "./gemini-value-strategy";

@Injectable({
    providedIn: 'root',
})
export class GeminiSchemaService {

    constructor(configService: GeminiConfigService) {
        // TODO query the application to get the entity schema
    }

    getEntitySchema(entityName: string): EntitySchema {
        return {
            name: entityName,
            displayName: entityName.toUpperCase(),
            fields: [{
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
            } ]
        };
    }

    /*
    , {
                name: "long",
                entity: {},
                type: FieldType.LONG
            }
     */


}
