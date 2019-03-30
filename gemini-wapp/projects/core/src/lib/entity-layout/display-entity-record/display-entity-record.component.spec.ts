import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayEntityRecordComponent } from './display-entity-record.component';

describe('DisplayEntityRecordComponent', () => {
  let component: DisplayEntityRecordComponent;
  let fixture: ComponentFixture<DisplayEntityRecordComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisplayEntityRecordComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayEntityRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
