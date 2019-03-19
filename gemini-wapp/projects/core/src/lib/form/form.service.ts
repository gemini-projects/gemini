import {Injectable} from '@angular/core';
import {EventType, FieldEvents, FieldSchema, FieldType} from "../schema/field-schema";
import {FormBuilder} from "@angular/forms";
import {GeminiValueStrategy} from "../schema/gemini-value-strategy";
import {FormStatus} from "./form-status";
import {DateTimeType, FormFieldComponentMeta, FormFieldStatus} from "./form-field-status";
import {InputComponent} from "./form-fields/input-fields/input-field.component";
import {GeminiSchemaService} from "../schema/schema.service";
import {Observable} from "rxjs";
import {map} from 'rxjs/operators';
import {BooleanComponent} from "./form-fields/boolean-field/boolean.component";
import {EntityManagerService} from "../api";
import {DateTimeComponent} from "./form-fields/date-time-fields/date-time.component";
import {SpinnerComponent} from "./form-fields/input-fields/spinner.component";


@Injectable({
    providedIn: 'root'
})
export class FormService {

    constructor(private fb: FormBuilder,
                private schemaService: GeminiSchemaService,
                private entityManager: EntityManagerService) {
    }

    public entityToForm(entityName: string): Observable<FormStatus> {
        return this.schemaService.getEntityFields(entityName)
            .pipe(
                map((fieldSchemas: FieldSchema[]) => {

                    let formStatus = new FormStatus();
                    formStatus.entityName = entityName;
                    let formGroup = formStatus.formGroup = this.fb.group({});
                    let formFieldsStatus: FormFieldStatus[] = [];

                    this.registerFormValueChanges(formStatus);

                    for (let field of fieldSchemas) {
                        let formFieldStatus = this.createFormFieldStatus(field);
                        if (formFieldStatus) {
                            formGroup.addControl(field.name, formFieldStatus.formControl);
                            formFieldsStatus.push(formFieldStatus);
                        }
                    }
                    formStatus.fieldsStatus = formFieldsStatus;
                    formStatus.submitFn = this.submitFunction.bind(this, entityName, formStatus);
                    return formStatus
                }));
    }

    private submitFunction(entityName: string, formStatus: FormStatus) {
        console.warn(formStatus.formGroup.value);
        return this.entityManager.createOrUpdateEntityRecord(entityName);
    }

    private registerFormValueChanges(formStatus: FormStatus) {
        // formStatus.formGroup.valueChanges.subscribe(value => console.log(value));
    }

    private createFormFieldStatus(field: FieldSchema): FormFieldStatus {
        let formFielStatus = new FormFieldStatus();
        formFielStatus.fieldSchema = field;
        let events: FieldEvents = field.events;

        //==== FieldVisibility - preliminary check to avoid unnecessary logic ===
        if (events.visible.eventType == EventType.NO_EVENT && !events.visible.value) {
            return;
        }
        // ===================

        // get the component
        formFielStatus.formControl = this.fb.control(null);
        formFielStatus.formComponent = this.getFieldComponent(formFielStatus);

        if (formFielStatus.formComponent == null) {
            return null;
        }

        this.computeGeminiValueStrategy(formFielStatus, "visible");


        // this.computeSyncValidator(formFielStatus);

        /* if (field.modifiableStrategy == GeminiValueStrategy.SIMPLE) {
            formFielStatus.modifiable = field.modifiable;
        } */

        return formFielStatus;
    }

    private computeGeminiValueStrategy(formFielStatus: FormFieldStatus, fieldName: string) {
        const field = formFielStatus.fieldSchema;
        const strategyField: GeminiValueStrategy = field[fieldName + "Strategy"];
        if (strategyField == GeminiValueStrategy.SIMPLE) {
            formFielStatus['fieldName'] = field['fieldName'];
        }

    }

    private getFieldComponent(formFielStatus: FormFieldStatus): FormFieldComponentMeta {
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
                    componentType: SpinnerComponent,
                    componentData: {
                        step: 0.01 // TODO configurable decimals
                    }
                };
            case FieldType.LONG:
                return {
                    componentType: SpinnerComponent,
                    componentData: {
                        step: 1
                    }
                };
            case FieldType.DOUBLE:
                return {
                    componentType: SpinnerComponent,
                    componentData: {
                        step: 0.01 // TODO configurable decimals
                    }
                };
            case FieldType.BOOL:
                return {
                    componentType: BooleanComponent,
                    componentData: {} // todo spinnger vs other gui component ??
                };
            case FieldType.TIME:
                return {
                    componentType: DateTimeComponent,
                    componentData: {
                        dateTimeType: DateTimeType.TIME
                    }
                };
            case FieldType.DATE:
                return {
                    componentType: DateTimeComponent,
                    componentData: {
                        dateTimeType: DateTimeType.DATE
                    }
                };
            case FieldType.DATETIME:
                return {
                    componentType: DateTimeComponent,
                    componentData: {
                        dateTimeType: DateTimeType.DATETIME
                    }
                };
            case FieldType.ENTITY_REF:
                break;
            case FieldType.RECORD:
                break;
        }
        return null;
    }

    /* private computeSyncValidator(formFielStatus: FormFieldStatus) {
        let fieldSchema = formFielStatus.fieldSchema;
        let validators: ValidatorFn[] = [];
        if (fieldSchema.requiredStrategy === GeminiValueStrategy.SIMPLE && fieldSchema.required) {
            validators.push(Validators.required);
        }
        formFielStatus.formControl.setValidators(validators);
    }*/
}
