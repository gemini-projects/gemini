import {FormControl} from "@angular/forms";
import {FieldSchema} from "../schema/field-schema";
import {Type} from "@angular/core";

export class FormFieldStatus {
    fieldSchema: FieldSchema;

    visible: boolean;
    modifiable: boolean;
    formControl: FormControl;
    formComponent: FormFieldComponent;


}

export class FormFieldComponent {
    componentType: Type<any>;
    componentData: FormFieldData
}

export interface FormFieldData {
    [key: string]: any
}

export class InputFieldData implements FormFieldData{
    inputType: string;
    step?: number
}

