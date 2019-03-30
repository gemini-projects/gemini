import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {GeminiSchemaService} from "../../schema/schema.service";
import {EntitySchema} from "../../schema/entity-schema";
import {GeminiEntityManagerService} from "../../api";
import {EntityRecord} from "../../schema/EntityRecord";
import {FormService} from "../../form/form.service";
import {FormStatus} from "../../form/form-status";
import {ConfirmationService} from "primeng/api";
import {TranslateService} from "@ngx-translate/core";
import {GeminiNotificationService, GeminiNotificationType} from "../../common";
import {ApiError} from "../../api/api-error";

@Component({
    selector: 'lib-display-entity',
    templateUrl: './display-entity-record.component.html',
    styleUrls: ['./display-entity-record.component.scss']
})
export class DisplayEntityRecordComponent implements OnInit {
    private entityName: string;
    private lkORUUID: string;

    private entitySchema: EntitySchema;
    private entityRecord: EntityRecord;
    private entityRecordTitle: string;
    private isModifing: boolean;
    private formStatus: FormStatus;
    private MESSAGES: any;

    constructor(private route: ActivatedRoute,
                private router: Router,
                private schemaService: GeminiSchemaService,
                private entityManager: GeminiEntityManagerService,
                private formService: FormService,
                private confirmationService: ConfirmationService,
                private translate: TranslateService,
                private notification: GeminiNotificationService) {
        this.isModifing = false;
    }

    ngOnInit() {
        const entityName = this.route.parent.snapshot.paramMap.get("name");
        if (entityName)
            this.entityName = entityName;
        const lkORUUID = this.route.snapshot.paramMap.get("lk");
        if (lkORUUID)
            this.lkORUUID = lkORUUID;

        this.translate.get(["DISPLAY_ENTITY_REC", "BUTTON", "ERROR"]).subscribe(message => {
            this.MESSAGES = message;
        });

        this.schemaService.getEntitySchema$(entityName).subscribe(entitySchema => {
            this.entitySchema = entitySchema;
            this.entityManager.getEntityRecord(this.entitySchema, this.lkORUUID).subscribe(entityRecord => {
                this.entityRecord = entityRecord;
                const refDescFields = this.entitySchema.getFieldsForReferenceDescription();
                this.entityRecordTitle = refDescFields.map(f => entityRecord.data[f.name]).join(" - ");
            }, (entityRecError: ApiError) => {
                this.notification.error(this.MESSAGES["ERROR"], this.MESSAGES["DISPLAY_ENTITY_REC"]["ERRORS"]["NOT_FOUND"], GeminiNotificationType.INSIDE);
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
            message: this.MESSAGES['DISPLAY_ENTITY_REC']['DELETE']['CONFIRMATION_MESSAGE'],
            header: this.MESSAGES['DISPLAY_ENTITY_REC']['DELETE']['CONFIRMATION_HEADER'],
            acceptLabel: this.MESSAGES['BUTTON']['DELETE'],
            rejectLabel: this.MESSAGES['BUTTON']['CANCEL'],
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.entityManager.deleteEntityRecord(this.entityRecord).subscribe(() => {
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
        console.log("SAVING");
        this.formStatus.submitFn().subscribe((er: EntityRecord) => {
            this.entityRecord = er;
            this.isModifing = false;
        });
    }

}
