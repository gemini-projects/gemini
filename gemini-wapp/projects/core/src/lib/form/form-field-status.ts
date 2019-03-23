import {FormControl} from "@angular/forms";
import {FieldSchema} from "../schema/field-schema";
import {Type} from "@angular/core";

export class FormFieldStatus {
    fieldSchema: FieldSchema;
    formControl: FormControl;
    formComponentConfig: FormFieldComponentConfig;
    availableData?: any;
}

export class FormFieldComponentConfig {
    componentType: Type<any>;
    componentConfigData: FormFieldData
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
