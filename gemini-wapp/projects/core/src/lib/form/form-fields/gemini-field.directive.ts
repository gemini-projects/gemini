import {ComponentFactoryResolver, Directive, Input, OnInit, ViewContainerRef} from "@angular/core";
import {FormFieldComponentDef} from "./form-field-component.interface";
import {FormFieldStatus} from "../form-field-status";
import {FormStatus} from "../form-status";


@Directive({
    selector: "[geminiFormField]"
})
export class GeminiFieldDirective implements OnInit, FormFieldComponentDef {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    componentRef: any;

    constructor(
        private resolver: ComponentFactoryResolver,
        private container: ViewContainerRef) {
    }

    ngOnInit(): void {
        const factory = this.resolver.resolveComponentFactory(
            this.fieldStatus.formComponentConfig.componentType
        );
        this.componentRef = this.container.createComponent(factory);
        this.componentRef.instance.fieldStatus = this.fieldStatus;
        this.componentRef.instance.formStatus = this.formStatus;
    }

}
