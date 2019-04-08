import {Component, Input, OnInit} from '@angular/core';
import {FieldSchema, FieldType} from "../../schema/field-schema";
import {EntityRecord} from "../../schema/EntityRecord";
import {GeminiMessagesService} from "../../common/gemini-messages.service";
import {GeminiEntityManagerService} from "../../api";

@Component({
    selector: 'view-field-container',
    templateUrl: './view-field-container.component.html',
    styleUrls: ['./view-field-container.component.scss']
})
export class ViewFieldContainerComponent implements OnInit {
    FieldType = FieldType;

    @Input() field: FieldSchema;
    @Input() entityRecord: EntityRecord;
    private trueLabel: string;
    private falseLabel: string;
    private noValue: boolean;

    value: any;
    type: FieldType;

    constructor(private messages: GeminiMessagesService,
                private entityManager: GeminiEntityManagerService) {
    }

    ngOnInit(): void {
        this.type = this.field.type;
        this.fromFieldToValue(this.field, this.entityRecord);
        this.trueLabel = this.messages.get('DATATYPE.BOOL.TRUE');
        this.falseLabel = this.messages.get('DATATYPE.BOOL.FALSE');
    }


    fromFieldToValue(field: FieldSchema, entityRecord: EntityRecord) {
        if (!(field.name in entityRecord.data) || entityRecord.data[field.name] == null) {
            this.noValue = true;
            return;
        }
        this.noValue = false;
        switch (field.type) {
            case FieldType.TEXT:
            case FieldType.NUMBER:
            case FieldType.LONG:
            case FieldType.DOUBLE:
            case FieldType.BOOL:
                this.value = entityRecord.data[field.name];
                break;
            case FieldType.TIME:
                const t = entityRecord.data[field.name] as Date;
                this.value = t.toLocaleTimeString();
                break;
            case FieldType.DATE:
                const d = entityRecord.data[field.name] as Date;
                this.value = d.toLocaleDateString();
                break;
            case FieldType.DATETIME:
                const dt = entityRecord.data[field.name] as Date;
                this.value = dt.toLocaleString();
                break;
            case FieldType.ENTITY_REF:
                const refValue = entityRecord.data[field.name];
                if (refValue instanceof EntityRecord) {
                    const er: EntityRecord = entityRecord.data[field.name];
                    this.value = er.getReferenceDescriptionLabel()
                } else {
                    // need to be async - we have the field key
                    if (isNotEmpty(refValue)) {
                        this.entityManager.getEntityRecord(field.refEntity, refValue)
                            .subscribe(er => {
                                this.value = er.getReferenceDescriptionLabel();
                            })
                    } else {
                        this.noValue = true;
                    }
                }
                break;
            case FieldType.RECORD:
                break;
            default:
                console.error(`Unable to convert field ${field.name} - ${field.type}`);
        }
    }
}


function isNotEmpty(refValue): boolean {
    if (refValue == null)
        return false;
    for (var key in refValue) {
        if (refValue.hasOwnProperty(key))
            return true;
    }
    return false;
}



