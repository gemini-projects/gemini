import {Component, Input} from '@angular/core';

import {GeminiSchemaService} from "../schema/schema.service";
import {EntitySchema} from "../schema/entity-schema";
import {FormService} from "../form/form.service";
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'entity-layout',
    templateUrl: './entity-layout.component.html',
    styleUrls: ['./entity-layout.component.scss']
})
export class EntityLayoutComponent {
    errorMsgs: [any];
    newEntityRecordEnabled: boolean;
    entitySchema: EntitySchema;

    _entityName;
    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute) {
        this.newEntityRecordEnabled = false;
        route.params.subscribe(val => {
            this.name = val.name
        });
    }

    @Input()
    set name(name: string) {
        this._entityName = name.trim();
        this.schemaService.getEntitySchema(this._entityName)
            .subscribe(es => {
                    this.entitySchema = es
                },
                error => {
                    this.errorMsgs = [{severity: 'error', summary: 'Error', detail: error}]
                }
            );
    }

    get name() {
        return this._entityName;
    }
}

