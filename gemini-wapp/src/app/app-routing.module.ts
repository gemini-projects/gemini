import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {EntityRouterComponent} from "./ui/entity-router/entity-router.component";
import {LoginComponent} from './ui/login/login.component';
import {LayoutComponent} from './ui/layout/layout.component';
import {HomeComponent} from "./ui/home/home.component";

const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {
        path: '', component: LayoutComponent,
        children: [
            {path: '', component: HomeComponent},
            {path: 'entity/:name', component: EntityRouterComponent},
        ]
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
    public static readonly ENTITY_ROUTE = '/entity';
}
