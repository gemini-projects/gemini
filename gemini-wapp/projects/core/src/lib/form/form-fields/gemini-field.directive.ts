import {ComponentFactoryResolver, Directive, Input, OnInit, ViewContainerRef} from "@angular/core";
import {FormFieldComponent} from "./form-field-component.interface";
import {FormFieldStatus} from "../form-field-status";
import {FormStatus} from "../form-status";


@Directive({
    selector: "[geminiFormField]"
})
export class GeminiFieldDirective implements OnInit, FormFieldComponent {
    @Input() fieldStatus: FormFieldStatus;
    @Input() formStatus: FormStatus;

    componentRef: any;
    constructor(
        private resolver: ComponentFactoryResolver,
        private container: ViewContainerRef
    ) {
    }

    ngOnInit(): void {
        const factory = this.resolver.resolveComponentFactory(
           this.fieldStatus.formComponent.componentType
        );
        this.componentRef = this.container.createComponent(factory);
        this.componentRef.instance.fieldStatus = this.fieldStatus;
        this.componentRef.instance.formStatus = this.formStatus;
    }

}
