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

    value: any;
    type: FieldType;

    constructor(private messages: GeminiMessagesService) {
    }

    ngOnInit(): void {
        this.type = this.field.type;
        this.value = fromFieldToValue(this.field, this.entityRecord);
        this.trueLabel = this.messages.get('DATATYPE.BOOL.TRUE');
        this.falseLabel = this.messages.get('DATATYPE.BOOL.FALSE');
    }

}

function fromFieldToValue(field: FieldSchema, entityRecord: EntityRecord) {
    if (!(field.name in entityRecord.data)) {
        return handleUndefinedField(field.type);
    }
    switch (field.type) {
        case FieldType.TEXT:
        case FieldType.NUMBER:
        case FieldType.LONG:
        case FieldType.DOUBLE:
        case FieldType.BOOL:
            return entityRecord.data[field.name];
        case FieldType.TIME:
            break;
        case FieldType.DATE:
            break;
        case FieldType.DATETIME:
            break;
        case FieldType.ENTITY_REF:
            break;
        case FieldType.RECORD:
            break;
    }
}

function handleUndefinedField(type: FieldType) {
    switch (type) {
        case FieldType.TEXT:
            return "";
        case FieldType.NUMBER:
        case FieldType.LONG:
        case FieldType.DOUBLE:
           return false;
        case FieldType.BOOL:
           return false;
        case FieldType.TIME:
            break;
        case FieldType.DATE:
            break;
        case FieldType.DATETIME:
            break;
        case FieldType.ENTITY_REF:
            break;
        case FieldType.RECORD:
            break;
    }
}
