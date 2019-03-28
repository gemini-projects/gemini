import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayEntityComponent } from './display-entity.component';

describe('DisplayEntityComponent', () => {
  let component: DisplayEntityComponent;
  let fixture: ComponentFixture<DisplayEntityComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisplayEntityComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayEntityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
