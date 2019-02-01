import {GeminiValueStrategy} from "../../schema/gemini-value-strategy";

export class FormFieldBase<T> {
    name: string;
    displayName: string;

    defaultValue: T;

    requiredStrategy: GeminiValueStrategy;
    required: boolean;

    order: number;
    fieldType: string;

    constructor(options: {
        name: string,
        displayName: string,
        fieldType: string
        defaultValue?: T,
        requiredStrategy?: GeminiValueStrategy
        required?: boolean,
        order?: number,
    }) {
        // TODO check form required fields
        this.name = options.name;
        this.displayName = options.displayName;
        this.fieldType = options.fieldType;

        this.defaultValue = options.defaultValue || null;

        this.requiredStrategy = options.requiredStrategy || GeminiValueStrategy.SIMPLE;
        this.required = options.required || false;

        this.order = options.order === undefined ? 1 : options.order;
    }



}
