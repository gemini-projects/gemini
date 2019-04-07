import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {GeminiSchemaService} from "../../schema/schema.service";
import {EntitySchema} from "../../schema/entity-schema";
import {GeminiEntityManagerService} from "../../api";
import {EntityRecord, FIELD_META_UUID} from "../../schema/EntityRecord";
import {FormService} from "../../form/form.service";
import {FormStatus} from "../../form/form-status";
import {ConfirmationService} from "primeng/api";
import {GeminiNotificationService, GeminiNotificationType} from "../../common";
import {ApiError} from "../../api/api-error";
import {GeminiMessagesService} from "../../common/gemini-messages.service";

@Component({
    selector: 'display-entity',
    templateUrl: './view-entity-record.component.html',
    styleUrls: ['./view-entity-record.component.scss']
})
export class ViewEntityRecordComponent implements OnInit {
    private entityName: string;
    private lkORUUID: string;

    private entitySchema: EntitySchema;
    private entityRecord: EntityRecord;
    private entityRecordTitle: string;
    private isModifing: boolean;
    private formStatus: FormStatus;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private schemaService: GeminiSchemaService,
                private entityManager: GeminiEntityManagerService,
                private formService: FormService,
                private confirmationService: ConfirmationService,
                private geminiNotification: GeminiNotificationService,
                private message: GeminiMessagesService) {
        this.isModifing = false;
    }

    ngOnInit() {
        const _entityName = this.route.parent.snapshot.paramMap.get("name");
        if (_entityName)
            this.entityName = _entityName.toUpperCase();
        const lkORUUID = this.route.snapshot.paramMap.get("lk");
        if (lkORUUID)
            this.lkORUUID = lkORUUID;

        this.schemaService.getEntitySchema$(this.entityName).subscribe(entitySchema => {
            this.entitySchema = entitySchema;
            this.entityManager.getEntityRecord(this.entitySchema, this.lkORUUID).subscribe(entityRecord => {
                this.entityRecord = entityRecord;
                const refDescFields = this.entitySchema.getFieldsForReferenceDescription();
                this.entityRecordTitle = refDescFields.map(f => entityRecord.data[f.name]).join(" - ");
            }, (entityRecError: ApiError) => {
                this.geminiNotification.error(this.message.get("ERROR"), this.message.get("ENTITY_RECORD.ERRORS.NOT_FOUND"), GeminiNotificationType.INSIDE);
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
        this.confirmationService.confirm({
            message: this.message.get('ENTITY_RECORD.DELETE.CONFIRMATION_MESSAGE'),
            header: this.message.get('ENTITY_RECORD.DELETE.CONFIRMATION_HEADER'),
            acceptLabel: this.message.get('BUTTON.DELETE'),
            rejectLabel: this.message.get('BUTTON.CANCEL'),
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.entityManager.deleteEntityRecord(this.entityRecord).subscribe(() => {
                    this.geminiNotification.warning(this.message.get("ENTITY_RECORD.DELETE.DELETED"));
                    return this.router.navigate(['../'], {relativeTo: this.route});
                })
            },
            reject: () => {
            }
        });
    }

    cancel() {
        this.isModifing = false;
    }

    save() {
        this.formStatus.submitFn().subscribe((er: EntityRecord) => {
            this.geminiNotification.success(this.message.get("ENTITY_RECORD.MODIFY.MODIFIED"));
            if (this.entityRecord.meta.uuid != er.meta.uuid) {
                console.warn("TODO need to refresh")
            }
            this.entityRecord = er;
            this.isModifing = false;
        }, (error: ApiError) => {
            this.geminiNotification.error(this.message.get("ENTITY_RECORD.ERRORS.UPDATE"), error.message);
        });
    }

}
