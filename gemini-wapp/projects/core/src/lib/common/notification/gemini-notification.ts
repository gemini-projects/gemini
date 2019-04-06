export class GeminiNotification {
    severity: string;
    title: string;
    description: string;
    type?: GeminiNotificationType

}

export enum GeminiNotificationType {
    TOAST = "TOAST",
    INSIDE = "INSIDE",
}
