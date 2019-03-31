import {Component, Input, OnInit} from '@angular/core';
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../../form/form.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormStatus} from "../../form/form-status";
import {EntityRecord} from "../../schema/EntityRecord";
import {FieldSchema} from "../../schema/field-schema";
import {ApiError} from "../../api/api-error";
import {GeminiNotificationService} from "../../common";
import {GeminiMessagesService} from "../../common/gemini-messages.service";

@Component({
    selector: 'new-entity',
    templateUrl: './new-entity-record.component.html',
    styleUrls: ['./new-entity-record.component.css']
})
export class NewEntityRecordComponent implements OnInit {
    formStatus: FormStatus;

    private ERROR_NEW_ENTITYREC_MESSAGE: string;
    private CREATED_MEESSAGE: string;
    private NEW_RECORD: string;

    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute,
                private router: Router,
                private geminiNotification: GeminiNotificationService,
                private mesage: GeminiMessagesService) {
    }

    @Input()
    set name(name: string) {
        let entityName = name.trim();
        this.formService.entityToForm(entityName)
            .subscribe(fs => {
                this.formStatus = fs;
                this.ERROR_NEW_ENTITYREC_MESSAGE = this.mesage.get('ENTITY_RECORD.ERRORS.NEW');
                this.NEW_RECORD = this.mesage.get('ENTITY_RECORD.NEW');
                this.CREATED_MEESSAGE = this.mesage.get('ENTITY_RECORD.CREATED');
            })
    }

    ngOnInit() {


        const entityName = this.route.parent.snapshot.paramMap.get("name");
        if (entityName)
            this.name = entityName
    }

    submitForm() {
        this.formStatus.submitFn().subscribe((er: EntityRecord) => {
            let entitySchema = er.entitySchema;
            this.geminiNotification.success(this.CREATED_MEESSAGE);

            let logicalKeyFields: FieldSchema[] = entitySchema.getLogicalKeyFields();
            if (logicalKeyFields.length == 1) {
                const lk = er.data[logicalKeyFields[0].name];
                return this.router.navigate(['../', lk], {relativeTo: this.route});
            }

            // no entity schema with a single logical key
            // TODO we can route by #unique-id
        }, (error: ApiError) => {
            this.geminiNotification.error(this.ERROR_NEW_ENTITYREC_MESSAGE, error.message);
        });
    }
}
