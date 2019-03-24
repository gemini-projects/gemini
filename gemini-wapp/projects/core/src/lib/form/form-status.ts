import {FormGroup} from "@angular/forms";
import {FormFieldStatus} from "./form-field-status";
import {Observable} from "rxjs";
import {EntitySchema} from "../schema/entity-schema";

export class FormStatus {
    entitySchema: EntitySchema;
    formGroup: FormGroup;
    fieldsStatus: FormFieldStatus[];
    submitFn: (...args: any[]) => Observable<any>;

    [key: string]: any // allowed any other string => value accordingly to the usage
}
