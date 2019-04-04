import {Component, Input, OnInit} from '@angular/core';
import {FieldSchema, FieldType} from "../../schema/field-schema";
import {EntityRecord} from "../../schema/EntityRecord";
import {GeminiMessagesService} from "../../common/gemini-messages.service";

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

    constructor(private messages: GeminiMessagesService) {
    }

    ngOnInit(): void {
        this.type = this.field.type;
        this.value = this.fromFieldToValue(this.field, this.entityRecord);
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
                return entityRecord.data[field.name];
            case FieldType.TIME:
                const t = entityRecord.data[field.name] as Date;
                return t.toLocaleTimeString();
            case FieldType.DATE:
                const d = entityRecord.data[field.name] as Date;
                return d.toLocaleDateString();
            case FieldType.DATETIME:
                const dt = entityRecord.data[field.name] as Date;
                return dt.toLocaleString();
            case FieldType.ENTITY_REF:
                break;
            case FieldType.RECORD:
                break;
        }
        console.error(`Unable to convert field ${field.name} - ${field.type}`);
    }
}



