import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {LoginComponent} from './ui/login/login.component';
import {LayoutComponent} from './ui/layout/layout.component';
import {HomeComponent} from "./ui/home/home.component";
import {entityRoutes} from "@gemini/core";


let layoutRoutes: Routes = [{path: '', component: HomeComponent}];
layoutRoutes = layoutRoutes.concat(entityRoutes);

const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {
        path: '', component: LayoutComponent,
        children: layoutRoutes
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
    public static readonly ENTITY_ROUTE = '/entity';
}
