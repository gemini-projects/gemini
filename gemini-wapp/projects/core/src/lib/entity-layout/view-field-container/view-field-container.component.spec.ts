import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewFieldContainerComponent } from './view-field-container.component';

describe('ViewFieldContainerComponent', () => {
  let component: ViewFieldContainerComponent;
  let fixture: ComponentFixture<ViewFieldContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewFieldContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewFieldContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
