import {Component, Input, OnInit} from '@angular/core';
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../../form/form.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormStatus} from "../../form/form-status";
import {EntityRecord} from "../../schema/EntityRecord";
import {FieldSchema} from "../../schema/field-schema";
import {TranslateService} from "@ngx-translate/core";
import {ApiError} from "../../api/api-error";
import {GeminiNotificationService} from "../../common";

@Component({
    selector: 'new-entity',
    templateUrl: './new-entity-record.component.html',
    styleUrls: ['./new-entity-record.component.css']
})
export class NewEntityRecordComponent implements OnInit {
    formStatus: FormStatus;

    private ERROR_NEW_ENTITYREC_MESSAGE: string;
    private CREATED_MEESSAGE: string;

    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute,
                private router: Router,
                private geminiNotification: GeminiNotificationService,
                private translate: TranslateService) {
    }

    @Input()
    set name(name: string) {
        let entityName = name.trim();
        this.formService.entityToForm(entityName)
            .subscribe(fs => {
                this.formStatus = fs;
            })
    }

    ngOnInit() {
        this.translate.get("NEW_ENTITY_REC").subscribe(message => {
            this.ERROR_NEW_ENTITYREC_MESSAGE = message['ERRORS'] && message['ERRORS']['NEW'] ? message['ERRORS']['NEW'] : "Error";
            this.CREATED_MEESSAGE = message['CREATED']
        });

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
