import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SteamStoreLink } from './steam-store-link';

describe('SteamStoreLink', () => {
  let component: SteamStoreLink;
  let fixture: ComponentFixture<SteamStoreLink>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SteamStoreLink]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SteamStoreLink);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
