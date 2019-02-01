import {Component, Input, OnInit} from '@angular/core';
import {FormFieldComponent} from "../form-field-component.interface";
import {FormFieldStatus, InputFieldData} from "../../form-field-status";
import {FormStatus} from "../../form-status";

@Component({
    selector: 'gemini-number-input',
    templateUrl: './input-field.component.html',
    styleUrls: ['./input-field.component.scss']
})
export class InputComponent implements FormFieldComponent, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    inputData: InputFieldData;
    actualStep: number;

    constructor() {
    }

    ngOnInit() {
        this.inputData = this.fieldStatus.formComponent.componentData as InputFieldData;
        if (this.inputData.inputType == "number") {
            this.initNumberFields();
        }
    }

    private initNumberFields() {
        this.actualStep = this.inputData.step;
        this.fieldStatus.formControl.valueChanges.subscribe(val => {
            const decimals = this.retr_dec(val);
            let dec_st = "0.";
            if (decimals > 0) {
                for (let i = 0; i < decimals - 1; i++)
                    dec_st += "0";
                dec_st += 1;
            }
            this.actualStep = Number(dec_st);
        })
    }

    private retr_dec(num: string) {
        return (num.split('.')[1] || []).length;
    }
}
