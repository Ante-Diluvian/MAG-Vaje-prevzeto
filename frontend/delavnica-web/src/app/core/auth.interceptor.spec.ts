import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { testniZeton } from '../../testing/testni-zeton';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let streznik: HttpTestingController;
  const snackBar = { open: vi.fn() };

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', testniZeton('MEHANIK'));
    snackBar.open.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    });
    http = TestBed.inject(HttpClient);
    streznik = TestBed.inject(HttpTestingController);
  });

  afterEach(() => streznik.verify());

  it('vsaki zahtevi doda žeton v glavo Authorization', () => {
    http.get('/api/nalogi').subscribe();

    const zahteva = streznik.expectOne('/api/nalogi');
    expect(zahteva.request.headers.get('Authorization')).toBe(`Bearer ${testniZeton('MEHANIK')}`);
    zahteva.flush([]);
  });

  it('sporočilo napake strežnika prikaže uporabniku', () => {
    http.post('/api/nalogi/1/postavke', {}).subscribe({ error: () => undefined });

    streznik
      .expectOne('/api/nalogi/1/postavke')
      .flush({ message: 'Premalo zaloge' }, { status: 409, statusText: 'Conflict' });

    expect(snackBar.open).toHaveBeenCalledWith('Premalo zaloge', 'V redu', { duration: 4000 });
  });

  it('ob poteklem žetonu (401) uporabnika odjavi brez sporočila', () => {
    const odjava = vi
      .spyOn(TestBed.inject(AuthService), 'odjava')
      .mockImplementation(() => undefined);
    http.get('/api/nalogi').subscribe({ error: () => undefined });

    streznik.expectOne('/api/nalogi').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(odjava).toHaveBeenCalled();
    expect(snackBar.open).not.toHaveBeenCalled();
  });
});
