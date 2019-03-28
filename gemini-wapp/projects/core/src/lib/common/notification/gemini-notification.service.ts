import {EventEmitter, Injectable} from "@angular/core";
import {GeminiNotification} from "@gemini/core";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class GeminiNotificationService {
    private notificationEmitter: EventEmitter<GeminiNotification> = new EventEmitter();

    error(errorTitle: string): void;
    error(errorTitle: string, errorDescription: string): void ;
    error(errorTitle: string, errorDescription?: string): void {
        this.notificationEmitter.emit({
            severity: "error",
            title: errorTitle,
            description: errorDescription
        })
    }

    notification$(): Observable<GeminiNotification>{
        return this.notificationEmitter;
    }
}
