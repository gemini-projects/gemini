import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {HttpClient, HttpClientModule} from '@angular/common/http';

import {CoreModule} from "@gemini/core";
import {AppComponent} from './app.component';
import {UiModule} from './ui/ui.module';
import {AppRoutingModule} from './app-routing.module';
import {environment} from '../environments/environment'
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {MultiTranslateHttpLoader} from "ngx-translate-multi-http-loader";

export function createTranslateLoader(http: HttpClient) {
    return new MultiTranslateHttpLoader(http, environment.TRANSLATIONS.PATHS);
}

@NgModule({
    declarations: [
        AppComponent],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        UiModule,
        AppRoutingModule,
        HttpClientModule,
        CoreModule.forRoot(environment),
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (createTranslateLoader),
                deps: [HttpClient]
            },
            isolate: true
        })
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
    constructor(translate: TranslateService) {
        translate.use(environment.TRANSLATIONS.DEFAULT_LANGUAGE);
    }

}
