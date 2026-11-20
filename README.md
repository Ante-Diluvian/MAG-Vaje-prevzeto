# Mehanična delavnica

[![CI/CD](https://github.com/Ante-Diluvian/MAG-Vaje-prevzeto/actions/workflows/ci-cd.yml/badge.svg?branch=main)](https://github.com/Ante-Diluvian/MAG-Vaje-prevzeto/actions/workflows/ci-cd.yml)

Aplikacija za mehanične delavnice: delovni nalogi, rezervni deli, računi in evidenca strank. Vloge: mehanik, stranka, vodja delavnice.

## Nameščena aplikacija

- Aplikacija: https://prevzeto.pages.dev
- API in Swagger: https://delavnica-api-latest.onrender.com/swagger-ui/index.html

Backend teče na brezplačni instanci Render, ki po 15 minutah brez prometa zaspi. Prva zahteva po tem traja minuto ali dve.

## Sestava

| Mapa | Vsebina |
|---|---|
| `frontend/delavnica-web` | Angular odjemalec |
| `backend/delavnica-api` | Spring Boot REST API (JWT, Swagger) |
| `db` | Dockerfile za CockroachDB |
| `docker-compose.yml` | zažene bazo in backend |
| `docs` | zasnova (01–04), uporabniške zgodbe in wireframes (05), arhitekturne odločitve (`adr/`) |

## Zagon

1. Baza in backend:
   ```bash
   docker compose up --build
   ```
   - API in Swagger: http://localhost:8080/swagger-ui/index.html
   - Konzola baze: http://localhost:8081
   - SQL: vrata 26257
2. Frontend:
   ```bash
   cd frontend/delavnica-web
   npm install
   ng serve
   ```
   Aplikacija je na http://localhost:4200.

## CI/CD

Cevovod [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) (GitHub Actions) se zažene ob vsakem pull requestu v `main` in ob vsakem pushu na `main`.

| Faza | Kaj naredi |
|---|---|
| `lint` | Preveri slog kode: Checkstyle za backend in ESLint za frontend. Ob napaki cevovod pade. |
| `build` | Zgradi backend jar in frontend `dist` (odvisnosti Maven in npm so predpomnjene) ter ju shrani kot artefakta. |
| `test` | Zažene teste backenda (JUnit, baza H2) in frontenda (Vitest) ter shrani poročili o pokritosti kot artefakta. |
| `deploy` | Samo ob pushu na `main`, ko uspejo `lint`, `build` in `test`. Iz artefaktov namesti backend na Render (slika prek GHCR) in frontend na Cloudflare Pages, nato preveri, da sta dosegljiva. |

`build` in `test` tečeta vzporedno, ko uspe `lint`. Pull requesta v `main` ni mogoče združiti, dokler `lint`, `build` in `test` ne uspejo.

## Dokumentacija

- [01 Zasnova rešitve](docs/01-zasnova-resitve.md)
- [02 Funkcionalnosti in obseg](docs/02-funkcionalnosti-in-obseg.md)
- [03 Podatkovni model](docs/03-podatkovni-model.md)
- [04 Arhitektura in tehnološki sklad](docs/04-arhitektura-in-tehnoloski-sklad.md)
- [05 Uporabniške zgodbe](docs/05-uporabniske-zgodbe.md)
- [05 Wireframes](docs/05-wireframes.md)
- [07 Pregled projekta](docs/07-pregled-projekta.md)
- [Arhitekturne odločitve (ADR)](docs/adr/)
