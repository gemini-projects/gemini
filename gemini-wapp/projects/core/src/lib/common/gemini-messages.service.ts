import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {tap} from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class GeminiMessagesService {

    private _messages;

    constructor(private translate: TranslateService) {
    }

    load(): Promise<Object> {
        return this.translate.getTranslation(this.translate.currentLang)
            .pipe(tap(m => this._messages = m)).toPromise()
    }

    get(key: string): string {
        if (this._messages) {
            return this._get(key);
        }
        console.error("Messages not loaded");
        return "";
    }

    private _get(key: string): string {
        if (key) {
            const splits = key.split(".");
            let m = this._messages;
            for (const split of splits) {
                if (m[split]) {
                    m = m[split];
                } else {
                    console.error(`No tralsation found for ${split} -> key: ${key}`);
                    return "";
                }
            }
            return m;
        }
    }
}


export function initMessageService(messagesService: GeminiMessagesService) {
    return () => messagesService.load();
}
