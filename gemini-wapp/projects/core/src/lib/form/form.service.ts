import {Injectable} from '@angular/core';
import {EntitySchema} from "../schema/entity-schema";
import {FieldSchema, FieldType} from "../schema/field-schema";
import {FormBuilder, ValidatorFn, Validators} from "@angular/forms";
import {GeminiValueStrategy} from "../schema/gemini-value-strategy";
import {FormStatus} from "./form-status";
import {FormFieldComponent, FormFieldStatus} from "./form-field-status";
import {InputComponent} from "./form-fields/input-field/input-field.component";

@Injectable({
    providedIn: 'root'
})
export class FormService {

    constructor(private fb: FormBuilder) {
    }

    public entitySchemaToForm(entitySchema: EntitySchema): FormStatus {
        let formStatus = new FormStatus();
        let formGroup = formStatus.formGroup = this.fb.group({});
        let formFieldsStatus: FormFieldStatus[] = [];

        this.registerValueChange(formStatus);

        for (let field of entitySchema.fields) {
            let formFieldStatus = this.createFormFieldStatus(field);
            if (formFieldStatus) {
                formGroup.addControl(field.name, formFieldStatus.formControl);
                formFieldsStatus.push(formFieldStatus);
            }
        }
        formStatus.fieldsStatus = formFieldsStatus;
        return formStatus;
    }

    private registerValueChange(formStatus: FormStatus) {
        // formStatus.formGroup.valueChanges.subscribe(value => console.log(value));
    }

    private createFormFieldStatus(field: FieldSchema): FormFieldStatus {
        let formFielStatus = new FormFieldStatus();
        formFielStatus.fieldSchema = field;

        //==== FieldVisibility - preliminary check to avoid unnecessary logic ===
        if (field.visibleStrategy == GeminiValueStrategy.SIMPLE && !field.visible) {
            return;
        }
        // ===================

        formFielStatus.formControl = this.fb.control(null);
        formFielStatus.formComponent = this.getFieldComponent(formFielStatus);

        this.computeGeminiValueStrategy(formFielStatus, "visible");


        this.computeSyncValidator(formFielStatus);

        if (field.modifiableStrategy == GeminiValueStrategy.SIMPLE) {
            formFielStatus.modifiable = field.modifiable;
        }

        return formFielStatus;
    }

    private computeGeminiValueStrategy(formFielStatus: FormFieldStatus, fieldName: string) {
        const field = formFielStatus.fieldSchema;
        const strategyField: GeminiValueStrategy = field[fieldName + "Strategy"];
        if (strategyField == GeminiValueStrategy.SIMPLE) {
            formFielStatus['fieldName'] = field['fieldName'];
        }

    }

    private getFieldComponent(formFielStatus: FormFieldStatus): FormFieldComponent {
        switch (formFielStatus.fieldSchema.type) {
            case FieldType.TEXT:
                return {
                    componentType: InputComponent,
                    componentData: {
                        inputType: "text"
                    }
                };
            case FieldType.NUMBER:
                return {
                    componentType: InputComponent,
                    componentData: {
                        inputType: "number"
                    }
                };
            case FieldType.LONG:
                return {
                    componentType: InputComponent,
                    componentData: {
                        inputType: "number",
                        step: 1
                    }
                };
            case FieldType.DOUBLE:
                return {
                    componentType: InputComponent,
                    componentData: {
                        inputType: "number",
                        step: 0.01 // TODO decimals
                    }
                };
            case FieldType.BOOL:
                break;
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
        return null;
    }

    private computeSyncValidator(formFielStatus: FormFieldStatus) {
        let fieldSchema = formFielStatus.fieldSchema;
        let validators: ValidatorFn[] = [];
        if (fieldSchema.requiredStrategy === GeminiValueStrategy.SIMPLE && fieldSchema.required) {
            validators.push(Validators.required);
        }
        formFielStatus.formControl.setValidators(validators);
    }
}
