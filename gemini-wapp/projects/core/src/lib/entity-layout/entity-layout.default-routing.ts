import {Routes} from '@angular/router';
import {EntityLayoutComponent} from "./entity-layout.component";
import {NewEntityComponent} from "./new-entity/new-entity.component";
import {ListEntityComponent} from "./list-entity/list-entity.component";


export const entityRoutes: Routes = [
    {
        path: 'entity/:name', component: EntityLayoutComponent,
        children: [
            {
                path: '', component: ListEntityComponent
            },
            {
                path: 'new',
                component: NewEntityComponent
            },
            {
                path: ':lk',
                component: ListEntityComponent
            }
        ]
    }
];
