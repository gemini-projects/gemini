import {FieldSchema} from "./field-schema";

export class EntitySchema {
    name: string;
    displayName: string;
    fields: FieldSchema[];

    constructor(rawSchema, fieldSchemas: FieldSchema[]) {
        Object.assign(this, rawSchema);
        this.fields = fieldSchemas;
    }
}
