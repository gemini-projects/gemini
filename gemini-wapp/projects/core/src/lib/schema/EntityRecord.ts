import {EntitySchema} from "./entity-schema";
import {FieldSchema, FieldType} from "./field-schema";

export interface EntityRecordInterface {
    data: any;
    meta: any
}

export class EntityRecord implements EntityRecordInterface {
    entitySchema: EntitySchema;

    private _meta: object;
    private _data: any;

    constructor(entity: EntitySchema, entityRecord?: EntityRecordInterface) {
        this.entitySchema = entity;
        if (entityRecord) {
            this._data = entityRecord.data;
            this._meta = entityRecord.meta;
        } else {
            this._data = {};
            this._meta = {};
        }
    }

    get meta(): object {
        return this._meta;
    }

    get data(): any {
        return this._data;
    }

    set(field: FieldSchema, value: any) {
        let entityField = getAndCheckEntityField(this.entitySchema, field);
        if (entityField) {
            this._data[entityField.name] = convertFieldValue.call(this, field, value);
        }
    }

    toJsonObject(): Object {
        return {
            "meta": this._meta,
            "data": this._data
        }
    }
}

export function entityRecordFromAPI(entity: EntitySchema, er: EntityRecord): EntityRecord {
    return new EntityRecord(entity, er);
}

function getAndCheckEntityField(entity: EntitySchema, field: FieldSchema): FieldSchema {
    let matched = entity.fields.filter(f => f.name == field.name);
    if (matched.length == 1) {
        return matched[0];
    }
    return null;
}

function convertFieldValue(field: FieldSchema, value: any) {
    switch (field.type) {
        case FieldType.TEXT:
            if (isString(value))
                return value;
            if (isNumber(value))
                return value.toString();
            break;
        case FieldType.NUMBER:
            if (isString(value)) {
                const n = Number(value);
                if (!isNaN(n))
                    return n;
            }
            if (isNumber(value)) {
                return value;
            }
            break;
        case FieldType.LONG:
            if (isString(value)) {
                value = Number(value);
            }
            if (isNumber(value) && Number.isInteger(value)) {
                return value;
            }
            break;
        case FieldType.DOUBLE:
            if (isString(value)) {
                value = Number(value);
            }
            if (isNumber(value)) {
                return value;
            }
            break;
        case FieldType.BOOL:
            if (isString(value)) {
                return (value as string).toLowerCase() == 'true'
            }
            if (isBool(value))
                return value;
            break;
        case FieldType.TIME:
        case FieldType.DATE:
        case FieldType.DATETIME:
            // TODO any conversion here ?
            break;
        case FieldType.ENTITY_REF:
            // TODO any check ? EntityRecord Object instance check ??
            return value;
        case FieldType.RECORD:
            break;
    }
    console.error(`Entity ${this.entitySchema.name}: unable to convert field ${field.name} - ${field.type} - return uncovverted value`);
    return value;
}

function isString(value): boolean {
    return typeof value === 'string' || value instanceof String;
}

function isNumber(value): boolean {
    return typeof value == 'number'
}

function isBool(value): boolean {
    return typeof value === "boolean";
}

function isDate(value): boolean {
    return value instanceof Date;
}

export class EntityRecordList {
    meta: object;
    data: EntityRecord[];
}
