import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriceChangeAlertsPage } from './price-change-alerts-page';

describe('PriceChangeAlertsPage', () => {
  let component: PriceChangeAlertsPage;
  let fixture: ComponentFixture<PriceChangeAlertsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PriceChangeAlertsPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PriceChangeAlertsPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
