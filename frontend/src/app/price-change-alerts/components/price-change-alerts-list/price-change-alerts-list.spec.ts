import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriceChangeAlertsList } from './price-change-alerts-list';

describe('PriceChangeAlertsList', () => {
  let component: PriceChangeAlertsList;
  let fixture: ComponentFixture<PriceChangeAlertsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PriceChangeAlertsList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PriceChangeAlertsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
