import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {MessagesModule} from 'primeng/messages';
import {MessageModule} from 'primeng/message';
import {RouterModule} from '@angular/router';
import {DropdownModule, TooltipModule} from "primeng/primeng";
import {TranslateModule} from "@ngx-translate/core";


import {GeminiUriService} from './common'
import {EntityLayoutComponent} from './entity-layout/entity-layout.component';
import {GeminiFormWrapperComponent} from './form/gemini-form/gemini-form-wrapper.component';
import {GeminiFieldDirective} from "./form/form-fields/gemini-field.directive";
import {FormFieldContainerComponent} from './form/form-field-container/form-field-container.component';
import {InputComponent} from "./form/form-fields/input-fields/input-field.component";
import {NewEntityComponent} from './entity-layout/new-entity/new-entity.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BooleanComponent} from "./form/form-fields/boolean-field/boolean.component";
import { ListEntityComponent } from './entity-layout/list-entity/list-entity.component';


@NgModule({
    declarations: [
        EntityLayoutComponent,
        GeminiFormWrapperComponent,
        GeminiFieldDirective,
        InputComponent,
        BooleanComponent,
        FormFieldContainerComponent,
        NewEntityComponent,
        ListEntityComponent
    ],
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        RouterModule,
        ButtonModule,
        TooltipModule,
        DropdownModule,
        ReactiveFormsModule,
        InputTextModule,
        MessagesModule,
        MessageModule,
        TranslateModule.forChild({})
    ],
    exports: [EntityLayoutComponent],
    entryComponents: [
        InputComponent,
        BooleanComponent
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
