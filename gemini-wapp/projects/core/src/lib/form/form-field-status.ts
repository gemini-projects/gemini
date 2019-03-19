import {FormControl} from "@angular/forms";
import {FieldSchema} from "../schema/field-schema";
import {Type} from "@angular/core";

export class FormFieldStatus {
    fieldSchema: FieldSchema;
    formControl: FormControl;
    formComponent: FormFieldComponentMeta;
}

export class FormFieldComponentMeta {
    componentType: Type<any>;
    componentData: FormFieldData
}

export interface FormFieldData {
    [key: string]: any
}

export class InputFieldData implements FormFieldData {
    inputType: string;
    step?: number
}

export class SpinnerFieldData implements FormFieldData {
    step: number;
}

export class DateTimeFieldData implements FormFieldData {
    dateTimeType: DateTimeType;
}

export enum DateTimeType {
    DATE, TIME, DATETIME
}
