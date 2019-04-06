import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewEntityRecordComponent } from './view-entity-record.component';

describe('ViewEntityRecordComponent', () => {
  let component: ViewEntityRecordComponent;
  let fixture: ComponentFixture<ViewEntityRecordComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewEntityRecordComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewEntityRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
