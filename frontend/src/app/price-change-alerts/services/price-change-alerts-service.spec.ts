import { TestBed } from '@angular/core/testing';

import { PriceChangeAlertsService } from './price-change-alerts-service';

describe('PriceChangeAlertsService', () => {
  let service: PriceChangeAlertsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PriceChangeAlertsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
