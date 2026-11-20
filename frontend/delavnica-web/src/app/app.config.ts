import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { apiUrlInterceptor } from './core/api-url.interceptor';
import { authInterceptor } from './core/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // apiUrlInterceptor je zadnji, da authInterceptor vidi še relativne poti (npr. /api/prijava)
    provideHttpClient(withInterceptors([authInterceptor, apiUrlInterceptor])),
  ]
};
