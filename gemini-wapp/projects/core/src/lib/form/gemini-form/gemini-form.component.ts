import {Component, Input, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";
import {FormFieldBase} from "../form-fields/form-field-base";
import {EntitySchema} from "../../schema/entity-schema";
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../form.service";
import {FormStatus} from "../form-status";

@Component({
    selector: 'gemini-form',
    templateUrl: './gemini-form.component.html',
    styleUrls: ['./gemini-form.component.scss']
})
export class GeminiFormComponent implements OnInit {
    @Input() entityName: string;

    entitySchema: EntitySchema;
    formStatus: FormStatus;

    private schemaService: GeminiSchemaService;
    private formService: FormService;

    constructor(schemaService: GeminiSchemaService, formService: FormService) {
        this.schemaService = schemaService;
        this.formService = formService;
    }

    ngOnInit() {
        this.entitySchema = this.schemaService.getEntitySchema(this.entityName);
        this.formStatus = this.formService.entitySchemaToForm(this.entitySchema);
    }

    onSubmit(event) {
        console.log(this.formStatus);
    }


    private computeSyncFormValidator(control: FormControl, field: FormFieldBase<any>) {

    }

    private computeAsynFormValidator(control: FormControl, field: FormFieldBase<any>) {

    }
}
