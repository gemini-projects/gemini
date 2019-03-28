import {Component, OnInit} from '@angular/core';
import {MessageService} from "primeng/api";
import {GeminiNotification, GeminiNotificationService} from "@gemini/core";

@Component({
    selector: 'gemini-layout',
    templateUrl: './layout.component.html',
    styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {
    private static HIDE_DELAY = 1500;

    constructor(private messageService: MessageService,
                private geminiNotificationService: GeminiNotificationService) {
    }

    ngOnInit() {
        this.geminiNotificationService.notification$().subscribe((notification: GeminiNotification) => {
            this.messageService.add({
                severity: notification.severity,
                summary: notification.title,
                detail: notification.description,
                life: LayoutComponent.HIDE_DELAY, sticky: false
            })
        })
    }
}
