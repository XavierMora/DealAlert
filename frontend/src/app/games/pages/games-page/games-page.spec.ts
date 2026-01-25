import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GamesPage } from './games-page';

describe('GamesPage', () => {
  let component: GamesPage;
  let fixture: ComponentFixture<GamesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GamesPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GamesPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
