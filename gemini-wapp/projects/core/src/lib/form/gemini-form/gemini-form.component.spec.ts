import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GeminiFormComponent } from './gemini-form.component';

describe('GeminiFormComponent', () => {
  let component: GeminiFormComponent;
  let fixture: ComponentFixture<GeminiFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GeminiFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GeminiFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
