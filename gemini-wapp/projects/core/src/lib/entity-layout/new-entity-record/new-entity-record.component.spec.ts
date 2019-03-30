import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewEntityRecordComponent } from './new-entity-record.component';

describe('NewEntityRecordComponent', () => {
  let component: NewEntityRecordComponent;
  let fixture: ComponentFixture<NewEntityRecordComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewEntityRecordComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewEntityRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
