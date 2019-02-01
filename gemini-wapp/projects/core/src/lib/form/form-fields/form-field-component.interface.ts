import {FormFieldStatus} from "../form-field-status";
import {FormStatus} from "../form-status";

/**
 * Form Field Interface can be used to be sure that all the form fields correcly
 * implements properties matching the dynamic [geminiFormField] directive
 */
export interface FormFieldComponent {
    fieldStatus: FormFieldStatus;
    formStatus: FormStatus;
}
