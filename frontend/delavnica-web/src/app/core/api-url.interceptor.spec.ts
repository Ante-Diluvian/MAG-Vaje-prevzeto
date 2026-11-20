import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { apiUrlInterceptor } from './api-url.interceptor';

describe('apiUrlInterceptor', () => {
  let http: HttpClient;
  let streznik: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiUrlInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    streznik = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    delete window.env;
    streznik.verify();
  });

  it('brez nastavljenega URL-ja backenda pusti relativno pot (razvojni proxy)', () => {
    http.get('/api/nalogi').subscribe();

    streznik.expectOne('/api/nalogi').flush([]);
  });

  it('zahtevam na /api doda URL nameščenega backenda', () => {
    window.env = { apiUrl: 'https://delavnica-api.example.net/' };
    http.get('/api/nalogi').subscribe();
    http.get('/favicon.ico').subscribe();

    streznik.expectOne('https://delavnica-api.example.net/api/nalogi').flush([]);
    streznik.expectOne('/favicon.ico').flush('');
  });
});
