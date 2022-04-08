import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PharoutinglistComponent } from './pharoutinglist.component';

describe('PharoutinglistComponent', () => {
  let component: PharoutinglistComponent;
  let fixture: ComponentFixture<PharoutinglistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PharoutinglistComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PharoutinglistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
