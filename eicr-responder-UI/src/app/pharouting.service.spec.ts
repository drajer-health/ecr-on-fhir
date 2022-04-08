import { TestBed } from '@angular/core/testing';

import { PharoutingService } from './pharouting.service';

describe('PharoutingService', () => {
  let service: PharoutingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PharoutingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
