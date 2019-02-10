import {OnInit} from '@angular/core'
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {GeminiUriService} from './gemini-uri.service'

export abstract class GeminiComponent implements OnInit {
    public static readonly ON_INIT_EVENT = 'oninit';


    constructor(protected componentName: string,
                private http?: HttpClient,
                protected GeminiConfig?: GeminiUriService) {
    }

    /**
     *  Init parameter and init callback
     *
     *  ngOnInit Executes a server call for init data (using init parameter if necessari)
     *  You can use the geminiOnInit callback to initialize the component accordingly to
     *  the received data from server (if needed)
     */
    geminiInitParam?(): {any: string};
    geminiOnInit?(serverData: Observable<any>): void

    ngOnInit() {
        if (this.geminiOnInit) {
            let url: string = this.GeminiConfig.getComponentEventUrl(this.componentName.toLowerCase(), GeminiComponent.ON_INIT_EVENT);
            let options: any = {};
            if (this.geminiInitParam) {
                let params = new HttpParams();
                for (let key in this.geminiInitParam) {
                    params = params.set(key, this.geminiInitParam[key])
                }
                options.params = params;
            }
            this.geminiOnInit(this.http.get(url, options));
        }
    }



}
