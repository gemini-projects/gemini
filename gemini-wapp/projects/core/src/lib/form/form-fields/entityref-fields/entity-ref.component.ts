import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {SelectItem} from "primeng/api";
import {GeminiSchemaService} from "../../../schema/schema.service";
import {EntityRecord, EntityRecordList} from "../../../schema/EntityRecord";
import {Observable} from "rxjs";
import {FieldSchema} from "../../../schema/field-schema";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'gemini-entityRef',
    templateUrl: './entity-ref.component.html'
})
export class EntityRefComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    private elems: SelectItem[] = [];
    private emptyFilterMessage: string;
    private selectValueMessage: string;

    constructor(private schemaService: GeminiSchemaService, private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.translate.get("DATATYPE.ENTITY_REF").subscribe((message: string) => {
            this.emptyFilterMessage = message['NORESULT_MESSAGE'];
            this.selectValueMessage = message['SELECTVALUE_MESSAGE'];
        });

        let refEntityName = this.fieldStatus.fieldSchema.refEntity!;
        this.schemaService.getEntitySchema$(refEntityName).subscribe((entiySchema) => {
            let descFields = entiySchema.getFieldsForReferenceDescription();
            let av: Observable<any> = this.fieldStatus.availableData;
            av.subscribe((entityRecordList: EntityRecordList) => {
                for (let er of entityRecordList.data) {
                    this.elems.push({label: this.getReferenceDescriptionLabel(descFields, er), value: er})
                }
            });
        });
    }

    getReferenceDescriptionLabel(descFields: FieldSchema[], entityRecord: EntityRecord): string {
        return descFields.map(f => entityRecord.data[f.name]).join(" - ");
    }

}
