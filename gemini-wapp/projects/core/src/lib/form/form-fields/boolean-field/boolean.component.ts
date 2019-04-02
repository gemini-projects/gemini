import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {TranslateService} from "@ngx-translate/core";
import {GeminiMessagesService} from "../../../common/gemini-messages.service";

@Component({
    selector: 'gemini-boolean-field',
    templateUrl: './boolean.component.html',
    styleUrls: []
})
export class BooleanComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    private trueLabel: string;
    private falseLabel: string;

    constructor(private messages: GeminiMessagesService) {
    }

    ngOnInit(): void {
        this.trueLabel = this.messages.get('DATATYPE.BOOL.TRUE');
        this.falseLabel = this.messages.get('DATATYPE.BOOL.FALSE')
        this.fieldStatus.formControl.setValue(this.fieldStatus.defaultValue);
    }

}

