import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GeminiFormWrapperComponent } from './gemini-form-wrapper.component';

describe('GeminiFormWrapperComponent', () => {
  let component: GeminiFormWrapperComponent;
  let fixture: ComponentFixture<GeminiFormWrapperComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GeminiFormWrapperComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GeminiFormWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
