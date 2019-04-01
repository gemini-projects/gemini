import {Component, Input} from '@angular/core';
import {FormStatus} from "../form-status";

@Component({
    selector: 'gemini-form-wrapper',
    templateUrl: './gemini-form-wrapper.component.html',
    styleUrls: ['./gemini-form-wrapper.component.scss']
})
export class GeminiFormWrapperComponent {
    @Input() formStatus: FormStatus;
}
