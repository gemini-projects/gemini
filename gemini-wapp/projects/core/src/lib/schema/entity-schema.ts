import {FieldSchema} from "./field-schema";

export class EntitySchema {
    name: string;
    displayName: string;
    fields?: FieldSchema[];
}
