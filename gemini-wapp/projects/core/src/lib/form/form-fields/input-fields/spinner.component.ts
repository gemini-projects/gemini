import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus, SpinnerFieldData} from "../../form-field-status";
import {FormStatus} from "../../form-status";

@Component({
    selector: 'gemini-spinner',
    templateUrl: './spinner.component.html'
})
export class SpinnerComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;
    private spinnerData: SpinnerFieldData;

    ngOnInit(): void {
        this.spinnerData = this.fieldStatus.formComponentConfig.componentConfigData as SpinnerFieldData;
        this.fieldStatus.formControl.setValue(this.fieldStatus.defaultValue);
    }

}
