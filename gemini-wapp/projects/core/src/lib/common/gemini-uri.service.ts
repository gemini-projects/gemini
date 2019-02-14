export class GeminiUriService {
    readonly SEARCH_PARAMETER: string = "search";
    readonly API_URL: string;
    readonly COMPONENT_URL: string;

    // TODO encapsulate here all the client side environment data (such as query string for Language)

    constructor(environment: any) {
        // TODO CHECK CONFIGURATION
        /* this.API_URL = environment.API_URL;
        this.COMPONENT_URL = environment.COMPONENT_URL; */
        GeminiUriService.checkRequiredParameters(environment);
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

    public getComponentEventUrl(component: string, event: string): string {
        return `${this.COMPONENT_URL}/${component}/${event}`
    }

    public getApiEntitiesUrl(entityName: string){
        return `${this.API_URL}/${entityName.toLowerCase()}`
    }

    public getApiEntityUrl(entityName: string, entityKey: string): string {
        return `${this.API_URL}/${entityName.toLowerCase()}/${entityKey}`
    }

    public getApiEntityCollectionUrl(entityName: string, entityKey: string, entityCollection: string): string {
        return `${this.API_URL}/${entityName.toLowerCase()}/${entityKey}/${entityCollection}`
    }
}
