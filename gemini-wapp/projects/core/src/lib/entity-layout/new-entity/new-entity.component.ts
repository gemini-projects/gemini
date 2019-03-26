import {Component, Input, OnInit} from '@angular/core';
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../../form/form.service";
import {ActivatedRoute} from "@angular/router";
import {FormStatus} from "../../form/form-status";

@Component({
    selector: 'new-entity',
    templateUrl: './new-entity.component.html',
    styleUrls: ['./new-entity.component.css']
})
export class NewEntityComponent implements OnInit {
    formStatus: FormStatus;

    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute) {
    }

    @Input()
    set name(name: string) {
        let entityName = name.trim();
        this.formService.entityToForm(entityName)
            .subscribe(fs => {
                this.formStatus = fs;
            })
    }

    ngOnInit() {
        this.route.parent.params.subscribe(val => {
            this.name = val.name;
        });
    }

    submitForm() {
        this.formStatus.submitFn().subscribe(v => {
            console.log(v)
            // TODO here gotosaved
        }, error => {
            // TODO notification service
            console.error(error);
        }, );
    }
}
