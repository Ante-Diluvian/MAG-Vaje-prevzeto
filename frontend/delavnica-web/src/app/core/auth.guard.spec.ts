import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { testniZeton } from '../../testing/testni-zeton';
import { authGuard, vlogaGuard } from './auth.guard';

describe('authGuard in vlogaGuard', () => {
  const zazeni = (guard: CanActivateFn) =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  const pot = (rezultat: unknown) => TestBed.inject(Router).serializeUrl(rezultat as UrlTree);

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideRouter([])] });
  });

  it('neprijavljenega uporabnika preusmeri na prijavo', () => {
    const rezultat = zazeni(authGuard);

    expect(rezultat).toBeInstanceOf(UrlTree);
    expect(pot(rezultat)).toBe('/prijava');
  });

  it('prijavljenega uporabnika spusti naprej', () => {
    localStorage.setItem('token', testniZeton('STRANKA'));

    expect(zazeni(authGuard)).toBe(true);
  });

  it('na strani vodje spusti samo vodjo, ostale preusmeri na začetno stran', () => {
    localStorage.setItem('token', testniZeton('STRANKA'));
    expect(pot(zazeni(vlogaGuard('VODJA')))).toBe('/');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideRouter([])] });
    localStorage.setItem('token', testniZeton('VODJA'));
    expect(zazeni(vlogaGuard('MEHANIK', 'VODJA'))).toBe(true);
  });
});
