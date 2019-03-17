import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {SelectItem} from "primeng/api";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'gemini-boolean-field',
    templateUrl: './boolean.component.html',
    styleUrls: []
})
export class BooleanComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    booleans: SelectItem[];
    private trueLabel: string;
    private falseLabel: string;

    constructor(private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.trueLabel = 'YES';
        this.falseLabel = 'NO';

        this.translate.get('DATATYPE.BOOL.TRUE').subscribe((res: string) => {
            this.trueLabel = res;
        });
        this.translate.get('DATATYPE.BOOL.FALSE').subscribe((res: string) => {
            this.falseLabel = res;
        });
        this.booleans = [
            {label: this.trueLabel, value: true},
            {label: this.falseLabel, value: false}
        ]
    }


}

