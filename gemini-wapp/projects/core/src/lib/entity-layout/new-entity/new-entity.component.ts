import {Component, Input, OnInit} from '@angular/core';
import {GeminiSchemaService} from "../../schema/schema.service";
import {FormService} from "../../form/form.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormStatus} from "../../form/form-status";
import {EntityRecord} from "../../schema/EntityRecord";
import {FieldSchema} from "../../schema/field-schema";

@Component({
    selector: 'new-entity',
    templateUrl: './new-entity.component.html',
    styleUrls: ['./new-entity.component.css']
})
export class NewEntityComponent implements OnInit {
    formStatus: FormStatus;

    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute,
                private router: Router) {
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
        this.formStatus.submitFn().subscribe((er: EntityRecord) => {
            let entitySchema = er.entitySchema();
            if (entitySchema) {
                let logicalKeyFields: FieldSchema[] = entitySchema.getLogicalKeyFields();
                if (logicalKeyFields.length == 1) {
                    const lk = er.data[logicalKeyFields[0].name];
                    return this.router.navigate(['../', lk], {relativeTo: this.route});
                }
            }
            // no entity schema with a single logical key
            // TODO we can route by #unique-id
        }, error => {
            // TODO notification service
            console.error(error);
        },);
    }
}
