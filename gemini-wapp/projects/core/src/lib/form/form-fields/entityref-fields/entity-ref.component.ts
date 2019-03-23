import {Component, Input, OnInit} from "@angular/core";
import {FormFieldComponentDef} from "../form-field-component.interface";
import {FormFieldStatus} from "../../form-field-status";
import {FormStatus} from "../../form-status";
import {SelectItem} from "primeng/api";
import {EntitySchema} from "../../../schema/entity-schema";
import {GeminiSchemaService} from "../../../schema/schema.service";
import {EntityRecord} from "../../../schema/EntityRecord";
import {Observable} from "rxjs";

@Component({
    selector: 'gemini-entityRef',
    templateUrl: './entity-ref.component.html'
})
export class EntityRefComponent implements FormFieldComponentDef, OnInit {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    elems: SelectItem[] = [];

    constructor(entitySchema: GeminiSchemaService) {
    }

    ngOnInit(): void {
        let av: Observable<any> = this.fieldStatus.availableData;
        av.subscribe((entityRecord: EntityRecord) => {

            for (let er of entityRecord.data) {
                console.log(er);
                let actualFields = er.data;
                this.elems.push({label: actualFields.name as string, value: er})
            }
        });
    }

}
