import {EventEmitter, Injectable} from "@angular/core";
import {GeminiNotification, GeminiNotificationType} from './gemini-notification';
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class GeminiNotificationService {
    private notificationEmitter: EventEmitter<GeminiNotification>;
    notification$: Observable<GeminiNotification>;

    constructor() {
        this.notificationEmitter = new EventEmitter();
        this.notification$ = this.notificationEmitter;
    }

    error(errorTitle: string): void;
    error(errorTitle: string, errorDescription: string): void ;
    error(errorTitle: string, errorDescription: string, notificationType: GeminiNotificationType): void ;
    error(errorTitle: string, errorDescription?: string, notificationType?: GeminiNotificationType): void {
        this.prepareAndEmitNotification("error", errorTitle, errorDescription, notificationType);
    }

    success(successTitle: string): void;
    success(successTitle: string, successDescription: string): void ;
    success(successTitle: string, successDescription: string, notificationType: GeminiNotificationType): void ;
    success(successTitle: string, successDescription?: string, notificationType?: GeminiNotificationType): void {
        this.prepareAndEmitNotification("success", successTitle, successDescription, notificationType);
    }

    private prepareAndEmitNotification(severity: string, title: string, desc: string, type?: GeminiNotificationType) {
        let notification: GeminiNotification = {
            severity: severity,
            title: title,
            description: desc
        };
        if (type) {
            notification.type = type;
        }
        this.notificationEmitter.emit(notification);
    }
}
