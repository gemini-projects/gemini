import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {MessagesModule} from 'primeng/messages';
import {MessageModule} from 'primeng/message';


import {GeminiUriService} from './common'
import {EntityLayoutComponent} from './entity-layout/entity-layout.component';
import {GeminiFormComponent} from './form/gemini-form/gemini-form.component';
import {GeminiFieldDirective} from "./form/form-fields/gemini-field.directive";
import {FormFieldContainerComponent} from './form/form-field-container/form-field-container.component';
import {InputComponent} from "./form/form-fields/input-field/input-field.component";


@NgModule({
    declarations: [
        EntityLayoutComponent,
        GeminiFormComponent,
        GeminiFieldDirective,
        InputComponent,
        FormFieldContainerComponent
    ],
    imports: [
        CommonModule,
        ButtonModule,
        ReactiveFormsModule,
        InputTextModule,
        MessagesModule,
        MessageModule
    ],
    exports: [EntityLayoutComponent],
    entryComponents: [
        InputComponent
    ]
})
export class CoreModule {

    public static forRoot(environment: any) {

        let config: GeminiUriService = new GeminiUriService(environment);

        return {
            ngModule: CoreModule,
            providers: [
                {
                    provide: GeminiUriService,
                    useValue: config
                }
            ]
        };
    }
}
