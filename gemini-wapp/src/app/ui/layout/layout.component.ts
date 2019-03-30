import {Component, OnInit} from '@angular/core';
import {MessageService} from "primeng/api";
import {GeminiNotification, GeminiNotificationService, GeminiNotificationType} from "@gemini/core";

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
        this.geminiNotificationService.notification$.subscribe((notification: GeminiNotification) => {
            if (!notification.type || notification.type == GeminiNotificationType.TOAST)
                this.messageService.add({
                    severity: notification.severity,
                    summary: notification.title,
                    detail: notification.description,
                    life: LayoutComponent.HIDE_DELAY, sticky: false
                })
        })
    }
}
