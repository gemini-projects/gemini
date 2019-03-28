import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'lib-display-entity',
    templateUrl: './display-entity.component.html',
    styleUrls: ['./display-entity.component.css']
})
export class DisplayEntityComponent implements OnInit {
    private entityName: string;
    private lkORUUID: string;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit() {
        const entityName = this.route.parent.snapshot.paramMap.get("name");
        if (entityName)
            this.entityName = entityName;
        const lkORUUID = this.route.snapshot.paramMap.get("lk");
        if (lkORUUID)
            this.lkORUUID = lkORUUID


    }

}
