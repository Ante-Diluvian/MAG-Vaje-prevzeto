import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { testniZeton } from '../../testing/testni-zeton';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  it('vlogo prebere iz shranjenega žetona', () => {
    localStorage.setItem('token', testniZeton('VODJA'));
    const auth = TestBed.inject(AuthService);

    expect(auth.vloga()).toBe('VODJA');
    expect(auth.jeVodja()).toBe(true);
    expect(auth.jeOsebje()).toBe(true);
  });

  it('mehanik je osebje, ni pa vodja; stranka ni osebje', () => {
    const auth = TestBed.inject(AuthService);

    auth.token.set(testniZeton('MEHANIK'));
    expect([auth.jeVodja(), auth.jeOsebje()]).toEqual([false, true]);

    auth.token.set(testniZeton('STRANKA'));
    expect([auth.jeVodja(), auth.jeOsebje()]).toEqual([false, false]);
  });

  it('prijava shrani žeton, odjava ga izbriše in preusmeri na prijavo', () => {
    const auth = TestBed.inject(AuthService);
    const streznik = TestBed.inject(HttpTestingController);
    const preusmeritev = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    auth.prijava('stranka@test.si', 'geslo').subscribe();
    const zahteva = streznik.expectOne('/api/prijava');
    expect(zahteva.request.body).toEqual({ email: 'stranka@test.si', geslo: 'geslo' });
    zahteva.flush({ token: testniZeton('STRANKA') });

    expect(localStorage.getItem('token')).toBe(testniZeton('STRANKA'));
    expect(auth.vloga()).toBe('STRANKA');

    auth.odjava();

    expect(localStorage.getItem('token')).toBeNull();
    expect(auth.vloga()).toBeNull();
    expect(preusmeritev).toHaveBeenCalledWith(['/prijava']);
  });
});
