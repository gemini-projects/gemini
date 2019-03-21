import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {SelectItem} from "primeng/api";

@Component({
    selector: 'gemini-entityRef',
    templateUrl: './entity-ref.component.html'
})
export class EntityRefComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    elems: SelectItem[];

    ngOnInit(): void {

    }

}
