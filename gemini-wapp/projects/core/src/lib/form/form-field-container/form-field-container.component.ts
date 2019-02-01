import {Component, Input, OnInit} from '@angular/core';
import {FormFieldStatus} from "../form-field-status";
import {FormStatus} from "../form-status";

@Component({
    selector: 'form-field-container',
    templateUrl: './form-field-container.component.html',
    styleUrls: ['./form-field-container.component.css']
})
export class FormFieldContainerComponent implements OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;


    constructor() {
    }

    ngOnInit(): void {
    }
}
