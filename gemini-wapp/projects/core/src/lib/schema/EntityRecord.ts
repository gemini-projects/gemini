import {EntitySchema} from "./entity-schema";
import {FieldSchema, FieldType} from "./field-schema";
import * as moment from 'moment';

export const FIELD_META_UUID: string = "uuid";

/**
 * EntityRecordApi is used as the first entry point for Gemini API EntityRecord (JSON).
 * It maps the response to an Object
 */
export interface EntityRecordApi {
    data: any;
    meta: any
}

/**
 * EntityRecordApiList  is used as the first entry point for Gemini API results (as list
 */
export class EntityRecordApiList {
    meta: any;
    data: EntityRecordApi[];
}

/**
 * EntityRecord is used to manipulate data and its field types. It implements the [[EntityRecordApi]] interface
 * but stores and checks data converting field values to a suitable and omogeneous type accordingly to [[FieldType]]
 */
export class EntityRecord implements EntityRecordApi {
    entitySchema: EntitySchema;

    private _meta: object;
    private _data: any;

    constructor(entity: EntitySchema, entityRecord?: EntityRecordApi) {
        this.entitySchema = entity;
        this._data = {};
        this._meta = {};
        if (entityRecord) {
            for (const field of entity.fields) {
                this.set(field, entityRecord.data[field.name]);
            }
            if (entityRecord.meta[FIELD_META_UUID] != null) {
                this._meta[FIELD_META_UUID] = entityRecord.meta[FIELD_META_UUID]
            }
        }
    }

    get meta(): EntityRecordMeta {
        return this._meta;
    }

    get data(): any {
        return this._data;
    }

    set(field: FieldSchema, value: any) {
        let entityField = getAndCheckEntityField(this.entitySchema, field);
        if (entityField) {
            this._data[entityField.name] = convertToFieldValue.call(this, field, value);
        }
    }

    getLogicalKeyValue() {
        const logicalKeyFields = this.entitySchema.getLogicalKeyFields();
        if (logicalKeyFields.length == 1) {
            const key = logicalKeyFields[0];
            if (!(this._data[key.name] instanceof Object)) {
                return this._data[key.name];
            } else {
                console.warn("TODO entityRef Keys"); // TODO
            }
        } else {
            console.warn("TODO handle multiple logical keys value"); // TODO
        }
    }

    getReferenceDescriptionLabel(): string {
        const descFields = this.entitySchema.getFieldsForReferenceDescription();
        return descFields.map(f => this._data[f.name]).join(" - ");
    }

    is(logicalKey: any): boolean {
        const logicalKeyFields = this.entitySchema.getLogicalKeyFields();
        if (logicalKeyFields.length == 1) {
            const key = logicalKeyFields[0];
            if (!(this._data[key.name] instanceof Object)) {
                return this._data[key.name] == logicalKey;
            } else {
                console.warn("TODO equality"); // TODO

            }
        } else {
            console.warn("TODO equals for multiple logical keys value"); // TODO
        }
        return false;
    }

    toJsonObject(): Object {
        if (this.entitySchema) {
            return {
                "meta": this._meta,
                "data": convertToJsonData(this.entitySchema, this._data)
            }
        } else {
            return {
                "meta": this._meta,
                "data": this._data
            }
        }
    }
}


export class EntityRecordList {
    meta: any;
    entitySchema: EntitySchema;
    data: EntityRecord[];
}

export class EntityRecordMeta {
    uuid?: string;

    [key: string]: any
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

/**
 * Convert a field to a suitable json value
 */
function convertToJsonData(entitySchema: EntitySchema, data: any) {
    let ret = {};
    for (const field of entitySchema.fields) {
        let value = data[field.name];
        if (typeof value == "undefined")
            continue;
        if (value == null)
            ret[field.name] = null;
        if (value != null)
            switch (field.type) {
                case FieldType.TEXT:
                case FieldType.NUMBER:
                case FieldType.LONG:
                case FieldType.DOUBLE:
                case FieldType.BOOL:
                    ret[field.name] = data[field.name];
                    break;
                case FieldType.TIME:
                    let t = value as Date;
                    ret[field.name] = t.toISOString().slice(11);
                    break;
                case FieldType.DATE:
                    let d = value as Date;
                    ret[field.name] = moment(d).format("YYYY-MM-DD");
                    break;
                case FieldType.DATETIME:
                    let dt = value as Date;
                    ret[field.name] = dt.toISOString();
                    break;
                case FieldType.ENTITY_REF:
                    if (value instanceof EntityRecord) {
                        const er = value as EntityRecord;
                        ret[field.name] = er.getLogicalKeyValue();
                    } else {
                        console.error(`Json Conversion non implemented: \value is not an instance of EntityRecord`)
                    }
                    break;
                case FieldType.RECORD:
                    break;
                default:
                    console.error(`Json Conversion not implemented for type ${field.type}`)
                    ret[field.name] = data[field.name]
            }
    }
    return ret;
}

/**
 * Convert a value to a valid Gemini standard private storage value for field.
 *
 * @param field Schema field
 * @param value actual value
 */
function convertToFieldValue(field: FieldSchema, value: any) {
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
            if (isDate(value)) {
                return value
            }
            if (isString(value)) {
                return parseISOTime(value);
            }
            break;
        case FieldType.DATE:
            if (isDate(value)) {
                return value
            }
            if (isString(value)) {
                return parseISODate(value);
            }
        case FieldType.DATETIME:
            if (isDate(value)) {
                return value
            }
            if (isString(value)) {
                return parseISODateTime(value);
            }
        case FieldType.ENTITY_REF:
            // TODO any check ? EntityRecord Object instance check ??
            // the value may also be a simple value as a string.. need type checking when is used
            return value;
        case FieldType.RECORD:
            break;
    }
    console.error(`Entity ${this.entitySchema.name}: unable to convert to field ${field.name} - ${field.type} - return unconverted value`);
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

function parseISODate(value): Date {
    const date = moment.utc(value, moment.ISO_8601);
    if (date.isValid())
        return date.toDate();
    return null;
}

function parseISOTime(value): Date {
    let date = moment.utc(value, moment.ISO_8601);
    if (date.isValid()) {
        return date.toDate();
    } else {
        date = moment.utc(value, "HH:mm:SS.SSSz")
        if (date.isValid())
            return date.toDate();
    }
    return null;
}

function parseISODateTime(value): Date {
    const date = moment.utc(value, moment.ISO_8601);
    if (date.isValid())
        return date.toDate();
    return null;
}
