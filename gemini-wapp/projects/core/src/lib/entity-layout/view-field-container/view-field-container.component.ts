import {Component, Input, OnInit} from '@angular/core';
import {FieldSchema} from "../../schema/field-schema";
import {EntityRecord} from "../../schema/EntityRecord";

@Component({
    selector: 'view-field-container',
    templateUrl: './view-field-container.component.html',
    styleUrls: ['./view-field-container.component.css']
})
export class ViewFieldContainerComponent implements OnInit {

    @Input() field: FieldSchema;
    @Input() entityRecord: EntityRecord;


    constructor() {
    }

    ngOnInit() {

    }

}
