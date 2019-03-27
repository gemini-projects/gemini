import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AutoCompleteModule} from 'primeng/autocomplete';

import {LayoutComponent} from './layout/layout.component';
import {HeaderComponent} from './header/header.component';
import {FooterComponent} from './footer/footer.component';
import {CoreModule} from "@gemini/core";
import {LeftSidebarComponent} from './left-sidebar/left-sidebar.component';
import {QuickSearchComponent} from './quick-search/quick-search.component';
import {LoginComponent} from './login/login.component'
import {RouterModule} from '@angular/router';
import {HomeComponent} from './home/home.component';


@NgModule({
    declarations: [LayoutComponent, HeaderComponent, FooterComponent, LeftSidebarComponent, QuickSearchComponent, LoginComponent, HomeComponent],
    imports: [
        CommonModule, AutoCompleteModule, CoreModule, RouterModule
    ],
    exports: [LayoutComponent, LoginComponent]
})
export class UiModule {
}
