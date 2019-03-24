import {FieldSchema} from "./field-schema";

export class EntitySchema {
    name: string;
    displayName: string;
    fields: FieldSchema[];

    constructor(rawSchema, fieldSchemas: FieldSchema[]) {
        Object.assign(this, rawSchema);
        this.fields = fieldSchemas;
    }

    getFieldsForReferenceDescription(): FieldSchema[] {
        let ret: FieldSchema[];
        ret = this.fields.filter(f => f.guiSettings.useAsDesc);
        if (ret.length == 0) {
            // need a default - use the logical
            ret = this.getLogicalKeyFields();
        }
        return ret.sort((a, b) => a.guiSettings.sortKey - b.guiSettings.sortKey);
    }

    getLogicalKeyFields(): FieldSchema[] {
        return this.fields.filter(f => f.isLogicalKey);
    }
}
