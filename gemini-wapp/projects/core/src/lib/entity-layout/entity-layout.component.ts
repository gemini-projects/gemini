import {Component, Input} from '@angular/core';

import {GeminiSchemaService} from "../schema/schema.service";
import {EntitySchema} from "../schema/entity-schema";
import {FormService} from "../form/form.service";

@Component({
    selector: 'entity-layout',
    templateUrl: './entity-layout.component.html',
    styleUrls: ['./entity-layout.component.scss']
})
export class EntityLayoutComponent {
    private schemaService: GeminiSchemaService;
    private formService: FormService;

    newEntityRecordEnabled: boolean;

    entityName: string;
    entitySchema: EntitySchema;

    constructor(schemaService: GeminiSchemaService, formService: FormService) {
        this.schemaService = schemaService;
        this.formService = formService;
        this.newEntityRecordEnabled = false;
    }

    @Input()
    set name(name: string) {
        let entityName = name.trim();
        this.entitySchema = this.schemaService.getEntitySchema(entityName);

        // todo conversion from field schema to form fields
    }

    newEntityRecord() {
        this.newEntityRecordEnabled = true;
    }

    closeNewEntityRecord() {
        this.newEntityRecordEnabled = false;
    }
}

