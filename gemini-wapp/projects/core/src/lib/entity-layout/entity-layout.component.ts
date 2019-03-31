import {Component, Input, OnInit} from '@angular/core';

import {GeminiSchemaService} from "../schema/schema.service";
import {EntitySchema} from "../schema/entity-schema";
import {FormService} from "../form/form.service";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {ApiError} from "../api/api-error";
import {GeminiNotificationService, GeminiNotificationType} from "../common";
import {Message} from "primeng/api";

@Component({
    selector: 'entity-layout',
    templateUrl: './entity-layout.component.html',
    styleUrls: ['./entity-layout.component.scss']
})
export class EntityLayoutComponent implements OnInit {
    errorMsgs: Message[] = [];
    newEntityRecordEnabled: boolean;
    entitySchema: EntitySchema;

    _entityName;

    constructor(private schemaService: GeminiSchemaService,
                private formService: FormService,
                private route: ActivatedRoute,
                private router: Router,
                private notificationService: GeminiNotificationService) {
        this.newEntityRecordEnabled = false;
        route.params.subscribe(val => {
            this.name = val.name
        });
    }

    ngOnInit(): void {
        this.router.events.subscribe(e => {
            if (e instanceof NavigationEnd) {
                this.errorMsgs = [];
            }
        });
        this.notificationService.notification$.subscribe(notification => {
            if (notification.type == GeminiNotificationType.INSIDE) {
                this.errorMsgs.push({
                    severity: notification.severity,
                    summary: notification.title,
                    detail: notification.description,
                    key: GeminiNotificationType.INSIDE
                })
            }
        })
    }

    @Input()
    set name(name: string) {
        this._entityName = name.trim();
        this.schemaService.getEntitySchema$(this._entityName)
            .subscribe(es => {
                    this.entitySchema = es
                },
                (error: ApiError) => {
                    this.errorMsgs = [{
                        severity: 'error',
                        summary: 'Error',
                        detail: error.message,
                        key: GeminiNotificationType.INSIDE
                    }]
                }
            );
    }

    get name() {
        return this._entityName;
    }
}

