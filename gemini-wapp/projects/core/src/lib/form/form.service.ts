import {Injectable} from '@angular/core';
import {FieldSchema, FieldType} from "../schema/field-schema";
import {FormBuilder} from "@angular/forms";
import {GeminiValueStrategy} from "../schema/gemini-value-strategy";
import {FormStatus} from "./form-status";
import {DateTimeType, FormFieldComponentConfig, FormFieldStatus} from "./form-field-status";
import {InputComponent} from "./form-fields/input-fields/input-field.component";
import {GeminiSchemaService} from "../schema/schema.service";
import {Observable, of} from "rxjs";
import {map} from 'rxjs/operators';
import {BooleanComponent} from "./form-fields/boolean-field/boolean.component";
import {GeminiEntityManagerService} from "../api";
import {DateTimeComponent} from "./form-fields/date-time-fields/date-time.component";
import {SpinnerComponent} from "./form-fields/input-fields/spinner.component";
import {EntityRefComponent} from "./form-fields/entityref-fields/entity-ref.component";
import {EntitySchema} from "../schema/entity-schema";
import {EntityRecord} from "../schema/EntityRecord";


@Injectable({
    providedIn: 'root'
})
export class FormService {

    constructor(private fb: FormBuilder,
                private schemaService: GeminiSchemaService,
                private entityManager: GeminiEntityManagerService) {
    }

    public entityToForm(entity: EntitySchema | string): Observable<FormStatus>;
    public entityToForm(entity: EntitySchema, entityRecord: EntityRecord): Observable<FormStatus>;
    public entityToForm(entity: string | EntitySchema, entityRecord?: EntityRecord): Observable<FormStatus> {
        if (typeof entity == "string") {
            return this.schemaService.getEntitySchema$(entity)
                .pipe(
                    map((entitySchema: EntitySchema) => {
                        return this.createFormStatus(entitySchema, entityRecord);
                    }));
        }
        if (entity instanceof EntitySchema) {
            return of(this.createFormStatus(entity, entityRecord))
        }
    }

    private createFormStatus(entitySchema: EntitySchema, entityRecord?: EntityRecord): FormStatus {
        let formStatus = new FormStatus();
        formStatus.entitySchema = entitySchema;
        let formGroup = formStatus.formGroup = this.fb.group({});
        //let formControls: Map<string, FormControl> = new Map<string, FormControl>();

        let formFieldsStatus: FormFieldStatus[] = [];

        this.registerFormValueChanges(formStatus);

        for (let field of entitySchema.fields) {
            let formFieldStatus = this.createFormFieldStatus(field, entityRecord);
            if (formFieldStatus) {
                formGroup.addControl(field.name, formFieldStatus.formControl);
                formFieldsStatus.push(formFieldStatus);
            }
        }
        formStatus.fieldsStatus = formFieldsStatus;
        formStatus.submitFn = this.submitFunction.bind(this, formStatus, entityRecord);
        return formStatus
    }

    private submitFunction(formStatus: FormStatus, oldEntityRec?: EntityRecord) {
        let entityRecord = this.convertFormValueToEntityRecord(formStatus.entitySchema, formStatus.formGroup.value);
        if (oldEntityRec)
            return this.entityManager.updateEntityRecord(entityRecord);
        else
            return this.entityManager.creteEntityRecord(entityRecord);
    }

    private registerFormValueChanges(formStatus: FormStatus) {
        // formStatus.formGroup.valueChanges.subscribe(value => console.log(value));
    }

    private createFormFieldStatus(field: FieldSchema, entityRecord?: EntityRecord): FormFieldStatus {
        let formFielStatus = new FormFieldStatus();
        formFielStatus.formControl = this.fb.control(null); // angular control
        formFielStatus.fieldSchema = field;

        /*
        let events: FieldEvents = field.events;
        //==== FieldVisibility - preliminary check to avoid unnecessary logic ===
        if (events.visible.eventType == EventType.NO_EVENT && !events.visible.value) {
            return;
        }
        // =================== */

        // Todo get form field filter data -- and ins to form field status
        formFielStatus.formComponentConfig = this.getComponentConfigByType(formFielStatus.fieldSchema.type);
        if (formFielStatus.formComponentConfig == null)
            return null;

        // TODO compute default DATA -- with def value
        this.geDefaultValueForField(formFielStatus, entityRecord);
        this.getAvailableValuesForField(formFielStatus);


        // this.computeGeminiValueStrategy(formFielStatus, "visible");
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

    private getComponentConfigByType(fieldType: FieldType): FormFieldComponentConfig {
        switch (fieldType) {
            case FieldType.TEXT:
                return {
                    componentType: InputComponent,
                    componentConfigData: {
                        inputType: "text"
                    }
                };
            case FieldType.NUMBER:
                return {
                    componentType: SpinnerComponent,
                    componentConfigData: {
                        step: 0.01 // TODO configurable decimals
                    }
                };
            case FieldType.LONG:
                return {
                    componentType: SpinnerComponent,
                    componentConfigData: {
                        step: 1
                    }
                };
            case FieldType.DOUBLE:
                return {
                    componentType: SpinnerComponent,
                    componentConfigData: {
                        step: 0.01 // TODO configurable decimals
                    }
                };
            case FieldType.BOOL:
                return {
                    componentType: BooleanComponent,
                    componentConfigData: {} // todo spinnger vs other gui component ??
                };
            case FieldType.TIME:
                return {
                    componentType: DateTimeComponent,
                    componentConfigData: {
                        dateTimeType: DateTimeType.TIME
                    }
                };
            case FieldType.DATE:
                return {
                    componentType: DateTimeComponent,
                    componentConfigData: {
                        dateTimeType: DateTimeType.DATE
                    }
                };
            case FieldType.DATETIME:
                return {
                    componentType: DateTimeComponent,
                    componentConfigData: {
                        dateTimeType: DateTimeType.DATETIME
                    }
                };
            case FieldType.ENTITY_REF:
                return {
                    componentType: EntityRefComponent,
                    componentConfigData: {}
                };
            case FieldType.RECORD:
                break;
        }
        return {
            componentType: InputComponent,
            componentConfigData: {
                inputType: "text"
            }
        };
    }

    /* private computeSyncValidator(formFielStatus: FormFieldStatus) {
        let fieldSchema = formFielStatus.fieldSchema;
        let validators: ValidatorFn[] = [];
        if (fieldSchema.requiredStrategy === GeminiValueStrategy.SIMPLE && fieldSchema.required) {
            validators.push(Validators.required);
        }
        formFielStatus.formControl.setValidators(validators);
    }*/
    private getAvailableValuesForField(formFielStatus: FormFieldStatus) {

        // todo SERVER SIDE available data call
        // todo or CLIENT SIDE available data callback

        // lets implement the default available value strategy
        switch (formFielStatus.fieldSchema.type) {
            case FieldType.TEXT:
            case FieldType.NUMBER:
            case FieldType.LONG:
            case FieldType.DOUBLE:
            case FieldType.BOOL:
            case FieldType.TIME:
            case FieldType.DATE:
            case FieldType.DATETIME:
                break;
            case FieldType.ENTITY_REF:
                this.getDefaultAvailableValuesForEntityRef(formFielStatus);
            case FieldType.RECORD:
                break;

        }

    }

    private getDefaultAvailableValuesForEntityRef(formFielStatus: FormFieldStatus) {
        let refEntityName: string = formFielStatus.fieldSchema.refEntity!;
        formFielStatus.availableData = this.entityManager.getEntityRecords(refEntityName)
    }

    private convertFormValueToEntityRecord(entitySchema: EntitySchema, objectWithFields: Object): EntityRecord {
        console.log(objectWithFields);
        let newRecord = new EntityRecord(entitySchema);
        for (const field of entitySchema.fields) {
            let value = objectWithFields[field.name];
            if (value != null) {
                newRecord.set(field, value);
            }
        }
        return newRecord;
    }

    private geDefaultValueForField(fieldStatus: FormFieldStatus, entityRecord?: EntityRecord) {
        if (entityRecord) {
            const data = entityRecord.data[fieldStatus.fieldSchema.name];

            fieldStatus.defaultValue = data;
            // TODO conversion
        }
    }
}
