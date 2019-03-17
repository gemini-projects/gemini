import {FormGroup} from "@angular/forms";
import {FormFieldStatus} from "./form-field-status";
import {Observable} from "rxjs";

export class FormStatus {
    formGroup: FormGroup;
    fieldsStatus: FormFieldStatus[];
    submitFn: (...args: any[]) => Observable<any>;
    [key: string] : any // allowed any other string => value accordingly to the usage
}
