export class GeminiConfigService {

    readonly API_URL: string;
    readonly COMPONENT_URL: string;

    constructor(environment: any) {
        // TODO CHECK CONFIGURATION
        /* this.API_URL = environment.API_URL;
        this.COMPONENT_URL = environment.COMPONENT_URL; */
        GeminiConfigService.checkRequiredParameters(environment);
        Object.assign(this, environment);
    }

    private static checkRequiredParameters(environment: any): void {
        ['API_URL', 'COMPONENT_URL'].forEach(element => {
            if (!environment[element]) {
                throw `GEMINI CONFIGURATION SERVICE -> NOT FOUND REQUIRED PROPERTY:  ${element}`
            }
            if (typeof environment[element] != 'string' || environment[element] == "") {
                throw `GEMINI CONFIGURATION SERVICE -> INVALID REQUIRED PROPERTY: ${element} - must be a Non Empty string`
            }
        });
    }

    public getComponentEventURI(component: string, event: string): string {
        return `${this.COMPONENT_URL}/${component}/${event}`
    }
}
