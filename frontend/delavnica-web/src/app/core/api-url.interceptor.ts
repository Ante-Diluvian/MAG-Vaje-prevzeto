import { HttpInterceptorFn } from '@angular/common/http';

declare global {
  interface Window {
    // nastavi public/env.js, ki ga ob namestitvi prepiše job deploy
    env?: { apiUrl?: string };
  }
}

// Relativnim zahtevam na /api doda URL nameščenega backenda (če je nastavljen).
export const apiUrlInterceptor: HttpInterceptorFn = (zahteva, naprej) => {
  const osnova = window.env?.apiUrl?.replace(/\/+$/, '');
  return naprej(
    osnova && zahteva.url.startsWith('/api')
      ? zahteva.clone({ url: osnova + zahteva.url })
      : zahteva,
  );
};
