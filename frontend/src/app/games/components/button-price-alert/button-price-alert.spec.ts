import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ButtonPriceAlert } from './button-price-alert';

describe('ButtonPriceAlert', () => {
  let component: ButtonPriceAlert;
  let fixture: ComponentFixture<ButtonPriceAlert>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonPriceAlert]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ButtonPriceAlert);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
