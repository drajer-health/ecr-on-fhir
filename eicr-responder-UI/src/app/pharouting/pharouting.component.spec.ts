import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PharoutingComponent } from './pharouting.component';

describe('PharoutingComponent', () => {
  let component: PharoutingComponent;
  let fixture: ComponentFixture<PharoutingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PharoutingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PharoutingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
