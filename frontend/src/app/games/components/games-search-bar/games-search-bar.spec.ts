import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GamesSearchBar } from './games-search-bar';

describe('GamesSearchBar', () => {
  let component: GamesSearchBar;
  let fixture: ComponentFixture<GamesSearchBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GamesSearchBar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GamesSearchBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
