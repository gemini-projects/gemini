import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {GeminiSchemaService} from "../../schema/schema.service";
import {EntitySchema} from "../../schema/entity-schema";
import {GeminiEntityManagerService} from "../../api";
import {EntityRecord} from "../../schema/EntityRecord";
import {FormService} from "../../form/form.service";
import {FormStatus} from "../../form/form-status";

@Component({
    selector: 'lib-display-entity',
    templateUrl: './display-entity.component.html',
    styleUrls: ['./display-entity.component.scss']
})
export class DisplayEntityComponent implements OnInit {
    private entityName: string;
    private lkORUUID: string;

    private entitySchema: EntitySchema;
    private entityRecord: EntityRecord;
    private entityRecordTitle: string;
    private isModifing: boolean;
    private formStatus: FormStatus;

    constructor(private route: ActivatedRoute,
                private schemaService: GeminiSchemaService,
                private entityManager: GeminiEntityManagerService,
                private formService: FormService) {
        this.isModifing = false;
    }

    ngOnInit() {
        const entityName = this.route.parent.snapshot.paramMap.get("name");
        if (entityName)
            this.entityName = entityName;
        const lkORUUID = this.route.snapshot.paramMap.get("lk");
        if (lkORUUID)
            this.lkORUUID = lkORUUID;

        this.schemaService.getEntitySchema$(entityName).subscribe(entitySchema => {
            this.entitySchema = entitySchema;
            this.entityManager.getEntityRecord(this.entityName, this.lkORUUID).subscribe(entityRecord => {
                this.entityRecord = entityRecord;
                const refDescFields = this.entitySchema.getFieldsForReferenceDescription();
                this.entityRecordTitle = refDescFields.map(f => entityRecord.data[f.name]).join(" - ");

            });
        });


    }

    modify() {
        this.isModifing = true;
        this.formService.entityToForm(this.entitySchema, this.entityRecord)
            .subscribe(fs => {
                this.formStatus = fs;
            })
    }

    delete() {

    }

    cancel() {
        this.isModifing = false;
    }

    save() {

    }

}
