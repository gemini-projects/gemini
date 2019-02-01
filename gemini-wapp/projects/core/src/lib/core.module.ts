import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';


import {GeminiConfigService} from './common'
import {EntityLayoutComponent} from './entity-layout/entity-layout.component';
import {GeminiFormComponent} from './form/gemini-form/gemini-form.component';
import {GeminiFieldDirective} from "./form/form-fields/gemini-field.directive";
import {FormFieldContainerComponent} from './form/form-field-container/form-field-container.component';
import {InputComponent} from "./form/form-fields/input-field/input-field.component";

import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';


@NgModule({
    declarations: [
        EntityLayoutComponent,
        GeminiFormComponent,
        GeminiFieldDirective,
        InputComponent,
        FormFieldContainerComponent
    ],
    imports: [CommonModule, ButtonModule, ReactiveFormsModule, InputTextModule],
    exports: [EntityLayoutComponent],
    entryComponents: [
        InputComponent
    ]
})
export class CoreModule {

    public static forRoot(environment: any) {

        let config: GeminiConfigService = new GeminiConfigService(environment);

        return {
            ngModule: CoreModule,
            providers: [
                {
                    provide: GeminiConfigService,
                    useValue: config
                }
            ]
        };
    }
}
