import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'gemini-entity-router',
  templateUrl: './entity-router.component.html',
  styleUrls: ['./entity-router.component.scss']
})
export class EntityRouterComponent implements OnInit {

  entityName: String;

  constructor(
    private route: ActivatedRoute,
    private location: Location
  ) {
    route.params.subscribe(val => {
      this.entityName = val.name;      
    });

      /* this.token = this.route
          .fragment
          .pipe((fragment => fragment || 'None')); */
  }

  ngOnInit(): void {
   
  }

}
