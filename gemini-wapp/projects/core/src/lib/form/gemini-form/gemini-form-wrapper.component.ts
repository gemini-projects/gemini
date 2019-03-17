import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EntitySchema} from "../../schema/entity-schema";
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../form.service";
import {FormStatus} from "../form-status";

@Component({
    selector: 'gemini-form-wrapper',
    templateUrl: './gemini-form-wrapper.component.html',
    styleUrls: ['./gemini-form-wrapper.component.scss']
})
export class GeminiFormWrapperComponent {
    @Input() formStatus: FormStatus;
}
