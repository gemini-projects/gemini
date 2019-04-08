import {EntityRecord} from "../schema/EntityRecord";
import {ActivatedRoute, Router} from "@angular/router";
import {FieldSchema} from "../schema/field-schema";

export function navigateToEntityRecord(router: Router, route: ActivatedRoute, er: EntityRecord) {
    let entitySchema = er.entitySchema;
    let logicalKeyFields: FieldSchema[] = entitySchema.getLogicalKeyFields();
    if (logicalKeyFields.length == 1) {
        const lk = er.data[logicalKeyFields[0].name];
        return router.navigate(['../', lk], {relativeTo: route});
    }

    // no entity schema with a single logical key
    // let's try by UUID
   /*  if (er.meta.uuid != null) {
        return router.navigate(['../', er.meta.uuid], {relativeTo: route});
    } */
}
