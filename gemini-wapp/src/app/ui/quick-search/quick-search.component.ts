import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {AppRoutingModule} from '../../app-routing.module'

import {GeminiComponent, GeminiUriService} from '@gemini/core';


@Component({
    selector: 'gemini-quick-search',
    templateUrl: './quick-search.component.html',
    styleUrls: ['./quick-search.component.scss']
})
export class QuickSearchComponent extends GeminiComponent {
    results: SearchElement[];
    items: SearchElement[];

    constructor(private router: Router, private httpClient: HttpClient, private geminiConfigService: GeminiUriService) {
        super('Common.QuickSearch', httpClient, geminiConfigService);
        this.items = [];
    }

    geminiOnInit(serverData: Observable<any>) {
        serverData.subscribe({
            next: (data: SearchElement[]) => this.items = data,
            error: err => console.error('Received an errror: ' + err)
        });
    }

    search(event) {
        let toSearch: string = event.query;
        // order by best match
        this.results = this.items.filter(element => {
            return element.searchRoute.toUpperCase().includes(toSearch.toUpperCase())
        }).sort((e1, e2) => {
            let n1 = e1.searchRoute.indexOf(toSearch);
            let n2 = e2.searchRoute.indexOf(toSearch);
            return n1 - n2;
        });
    }

    selected(value) {
        const route: string = AppRoutingModule.ENTITY_ROUTE;
        return this.router.navigate([route, value.searchRoute.toLowerCase()]);
    }
}


class SearchElement {
    searchType: string;
    searchRoute: string;
}


