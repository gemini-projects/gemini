import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {DateTimeFieldData, DateTimeType, FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'gemini-date-field',
    templateUrl: 'date-time.component.html'
})
export class DateTimeComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    localeConfig: Object;
    private dateTimeData: DateTimeFieldData;
    private showTime : boolean;
    private timeOnly : boolean;
    private hourFormat= 12;
    private showButtonBar: boolean;


    constructor(private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.translate.get("DATATYPE.DATE_TIME").subscribe((loc: Object) => {
            this.localeConfig = loc;
        });
        this.dateTimeData = this.fieldStatus.formComponent.componentData as DateTimeFieldData;
        this.showTime = this.dateTimeData.dateTimeType == DateTimeType.DATETIME;
        this.showButtonBar = this.dateTimeData.dateTimeType == DateTimeType.DATETIME;
        this.timeOnly = this.dateTimeData.dateTimeType == DateTimeType.TIME;
    }

}

