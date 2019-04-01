import {Routes} from '@angular/router';
import {EntityLayoutComponent} from "./entity-layout.component";
import {NewEntityRecordComponent} from "./new-entity-record/new-entity-record.component";
import {ListEntityComponent} from "./list-entity/list-entity.component";
import {ViewEntityRecordComponent} from "./view-entity-record/view-entity-record.component";


export const entityRoutes: Routes = [
    {
        path: 'entity/:name', component: EntityLayoutComponent,
        children: [
            {
                path: '', component: ListEntityComponent
            },
            {
                path: 'new',
                component: NewEntityRecordComponent
            },
            {
                path: ':lk',
                component: ViewEntityRecordComponent
            }
        ]
    }
];
