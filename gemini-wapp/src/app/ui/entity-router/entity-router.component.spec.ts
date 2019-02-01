import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityRouterComponent } from './entity-router.component';

describe('EntityRouterComponent', () => {
  let component: EntityRouterComponent;
  let fixture: ComponentFixture<EntityRouterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityRouterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityRouterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
