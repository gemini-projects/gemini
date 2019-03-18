import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'gemini-date-field',
    templateUrl: 'date.component.html'
})
export class DateComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    localeConfig: Object;

    constructor(private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.translate.get("DATATYPE.DATE_TIME").subscribe((loc: Object) => {
            this.localeConfig = loc;
        })
    }

}

