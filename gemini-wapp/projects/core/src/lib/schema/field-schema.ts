import {EntitySchema} from "./entity-schema";
import {GeminiValueStrategy} from "./gemini-value-strategy";

export class FieldSchema {
    name: string;
    entity?: any;
    type: FieldType;

    visibleStrategy: GeminiValueStrategy;
    visible?: boolean;
    modifiableStrategy: GeminiValueStrategy;
    modifiable?: boolean;
    requiredStrategy: GeminiValueStrategy;
    required?: boolean;
}

export class FieldSchemaStrict extends FieldSchema {
    entity: EntitySchema
}

export enum FieldType {
    TEXT,
    NUMBER,
    LONG,
    DOUBLE,
    BOOL,
    TIME,
    DATE,
    DATETIME,
    ENTITY_REF,
    RECORD
}

