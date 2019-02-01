import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';


import {AppComponent} from './app.component';

import {UiModule} from './ui/ui.module';
import {AppRoutingModule} from './app-routing.module';
import {CoreModule} from "@gemini/core";
import {environment} from '../environments/environment'


@NgModule({
    declarations: [
        AppComponent],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        UiModule,
        AppRoutingModule,
        HttpClientModule,
        CoreModule.forRoot(environment)
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
