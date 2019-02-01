import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityLayoutComponent } from './entity-layout.component';

describe('EntityLayoutComponent', () => {
  let component: EntityLayoutComponent;
  let fixture: ComponentFixture<EntityLayoutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityLayoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
