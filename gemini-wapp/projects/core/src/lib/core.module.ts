import {APP_INITIALIZER, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {MessagesModule} from 'primeng/messages';
import {MessageModule} from 'primeng/message';
import {RouterModule} from '@angular/router';
import {
    CalendarModule,
    ConfirmationService,
    ConfirmDialogModule,
    DropdownModule,
    SpinnerModule,
    ToggleButtonModule,
    TooltipModule
} from "primeng/primeng";
import {TranslateModule} from "@ngx-translate/core";


import {GeminiUriService} from './common'
import {EntityLayoutComponent} from './entity-layout/entity-layout.component';
import {GeminiFormWrapperComponent} from './form/gemini-form/gemini-form-wrapper.component';
import {GeminiFieldDirective} from "./form/form-fields/gemini-field.directive";
import {FormFieldContainerComponent} from './form/form-field-container/form-field-container.component';
import {InputComponent} from "./form/form-fields/input-fields/input-field.component";
import {NewEntityRecordComponent} from './entity-layout/new-entity-record/new-entity-record.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BooleanComponent} from "./form/form-fields/boolean-field/boolean.component";
import {ListEntityComponent} from './entity-layout/list-entity/list-entity.component';
import {DateTimeComponent} from "./form/form-fields/date-time-fields/date-time.component";
import {SpinnerComponent} from "./form/form-fields/input-fields/spinner.component";
import {EntityRefComponent} from "./form/form-fields/entityref-fields/entity-ref.component";
import {ViewEntityRecordComponent} from './entity-layout/view-entity-record/view-entity-record.component';
import {GeminiMessagesService, initMessageService} from "./common/gemini-messages.service";
import { ViewFieldContainerComponent } from './entity-layout/view-field-container/view-field-container.component';


@NgModule({
    declarations: [
        EntityLayoutComponent,
        GeminiFormWrapperComponent,
        GeminiFieldDirective,
        InputComponent,
        BooleanComponent,
        DateTimeComponent,
        SpinnerComponent,
        EntityRefComponent,
        FormFieldContainerComponent,
        NewEntityRecordComponent,
        ListEntityComponent,
        ViewEntityRecordComponent,
        ViewFieldContainerComponent
    ],
    imports: [
        BrowserAnimationsModule,
        CommonModule,
        RouterModule,
        ButtonModule,
        TooltipModule,
        DropdownModule,
        SpinnerModule,
        CalendarModule,
        ToggleButtonModule,
        ReactiveFormsModule,
        FormsModule,
        InputTextModule,
        MessagesModule,
        MessageModule,
        ConfirmDialogModule,
        TranslateModule.forChild({})
    ],
    exports: [EntityLayoutComponent],
    providers: [ConfirmationService,
        GeminiMessagesService,
        {
            provide: APP_INITIALIZER,
            useFactory: initMessageService,
            deps: [GeminiMessagesService],
            multi: true
        }
    ],
    entryComponents: [
        InputComponent,
        BooleanComponent,
        DateTimeComponent,
        SpinnerComponent,
        EntityRefComponent
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
