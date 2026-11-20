# 07 Pregled projekta

## 1. Implementirane funkcionalnosti za MVP

| # | Funkcionalnost | Ocena | Ugotovitve |
|---|---|---|---|
| F1 | Prijava z JWT in vloge | izpolnjeno | Prijava vrne JWT (velja 8 h) z ID-jem in vlogo, gesla so v bazi shranjena z BCrypt. Brez žetona API vrne 401, ob napačni vlogi 403. Meni in strani se prilagodijo vlogi, odjava izbriše žeton in preusmeri na prijavo. |
| F2 | Evidenca strank in vozil | delno | Stranke se dodajajo in urejajo na strani *Uporabniki*, vozila se dodajajo in brišejo. **Ni** iskanja vozil po registrski številki: v UI-ju ni polja za iskanje, backend pa parameter `?registrska=` ignorira. Urejanje vozila je možno samo prek API-ja. Backend vnosov ne preverja: sprejme uporabnika brez gesla, uporabnika s praznim imenom in letnik 3000. |
| F3 | Odpiranje naloga in dodelitev mehaniku | izpolnjeno | Nalog dobi vozilo, mehanika in opis. Dodelitev uporabniku, ki ni mehanik, vrne 400, prav tako prazen opis. Nov nalog je vedno v stanju `ODPRT`. |
| F4 | Stanje naloga in opis opravljenega dela | delno | Osnovno deluje. Mehanik pa lahko nalog nastavi na `OBRACUNAN` (preverjeno v UI-ju), obračunan nalog je prek API-ja mogoče vrniti v `ODPRT`, negativne ure pa se sprejmejo. |
| F5 | Dodajanje delov in odpis zaloge | izpolnjeno | Zaloga se zmanjša. Ob premajhni zalogi backend dodajanje zavrne, zaloga ostane enaka in postavka se ne ustvari. Vodja vidi sporočilo „Premalo zaloge“, mehanik pa samo „Napaka strežnika“ |
| F6 | Izdaja računa s popustom | izpolnjeno | Preverjeni zneski: 2,5 h + 2 × 10,50 € = 146,00 €. Za 5. račun iste stranke (2,55 h + 2 × 10,50 €) je obračunan 10 % popust: 133,65 €. Drugega računa za isti nalog ni mogoče izdati. |
| F7 | Pregled za stranko | delno | Stranka vidi samo svoja vozila, naloge in račune, vse samo za branje. Do tujih zapisov ne more dostopati. **Ni** obvestil o spremembi stanja naloga, novo stanje stranka vidi šele ob osvežitvi strani. |


## 2. Pregled 5 uporabniških zgodb

Izbral sem zgodbe, ki pokrivajo domensko specifične MVP funkcionalnosti (F3–F6) in pogled stranke (F7).

### UZ-06 Odpiranje delovnega naloga (F3): zadošča

| # | Kriterij | Ocena | Rezultat testa |
|---|---|---|---|
| 1 | Obvezna sta vozilo in opis | izpolnjeno | Prazen opis vrne 400. Obvezen je tudi mehanik; dodelitev stranki vrne 400. |
| 2 | Stanje `ODPRT`, današnji datum | izpolnjeno | Nalog dobi stanje `ODPRT` tudi, če zahteva pošlje drugo stanje. Datum odprtja je današnji. |
| 3 | Nalog takoj viden vsem mehanikom in vodji | delno | Nalog vidita vodja in **dodeljeni** mehanik, drugi mehanik ga ne vidi. Opis vloge in US se razlikujeta. |
| 4 | Lastnik vidi nalog | izpolnjeno | Lastnik vozila vidi nalog, druga stranka ga ne vidi. |

### UZ-07 Sprememba stanja naloga (F4): delno

| # | Kriterij | Ocena | Rezultat testa |
|---|---|---|---|
| 1 | Mehanik nastavi `ODPRT`/`V_DELU`/`ZAKLJUCEN` | izpolnjeno | Deluje na mehanikovih nalogih. Drug mehanik in stranka dobita 403. |
| 2 | Novo stanje je takoj vidno | izpolnjeno | Seznam se po shranjevanju osveži. |
| 3 | Mehanik ne more nastaviti `OBRACUNAN` | ni izpolnjeno | V UI-ju mehanik izbere `OBRACUNAN` in shrani. Nalog nato nima računa, izdaja računa vrne 409 („samo za zaključen nalog“), obrazec za urejanje pa izgine. |
| 4 | Obračunan nalog je samo za branje | delno | UI obrazec skrije. API pa mehaniku dovoli, da nalog vrne v `ODPRT` in nanj doda del. |

### UZ-09 Dodajanje dela na nalog (F5): zadošča

| # | Kriterij | Ocena | Rezultat testa |
|---|---|---|---|
| 1 | Samo deli iz kataloga, količina > 0 | izpolnjeno | Del se izbere iz kataloga. Količina 0 in neobstoječ del se zavrneta (mehanik zaradi napake z `/error` pri tem dobi 403). |
| 2 | Tabela „Uporabljeni deli“ | izpolnjeno | Prikaže naziv, količino in ceno. |
| 3 | Zaloga se zmanjša | izpolnjeno | Po dodajanju 2 kosov se zaloga zmanjša s 5 na 3. |
| 4 | Cena ob dodajanju se ohrani | izpolnjeno | Po spremembi cene dela na 99 € ostane v postavki 10,50 €. |

### UZ-11 Izdaja računa (F6): zadošča

| # | Kriterij | Ocena | Rezultat testa |
|---|---|---|---|
| 1 | Gumb samo pri `ZAKLJUCEN` | izpolnjeno | Gumb vidi samo vodja, mehanik pri klicu API-ja dobi 403. Za nalog v stanju `V_DELU` backend vrne 409. |
| 2 | Znesek = postavke + ure × cena ure | izpolnjeno | Znesek je 146,00 € (urna postavka 50 €). Popust za redne stranke je preverjen na 5. računu (133,65 €). |
| 3 | Nalog → `OBRACUNAN`, račun z današnjim datumom, neplačan | izpolnjeno | Nastavi se tudi datum zaprtja naloga. |
| 4 | Drugega računa ni mogoče izdati | izpolnjeno | Drugi klic vrne 409. Tudi če stanje naloga ročno vrnemo na `ZAKLJUCEN`, unikatni indeks na `racun.nalog_id` prepreči drugi račun. |

### UZ-12 Pregled svojih vozil in nalogov (F7): zadošča

| # | Kriterij | Ocena | Rezultat testa |
|---|---|---|---|
| 1 | Samo moja vozila | izpolnjeno | Stranka dobi samo vozila, katerih lastnica je. |
| 2 | Samo nalogi mojih vozil s stanjem, opisom in datumom | izpolnjeno | Tabela prikazuje datum, vozilo, mehanika, težavo in stanje. |
| 3 | Tuje vozilo se ne vrne | izpolnjeno | Podatki se ne vrnejo. Odgovor je 403 namesto 404 zaradi napake z `/error`. |
| 4 | Samo za branje | izpolnjeno | Stranka nima gumbov za urejanje in je s strani `/deli` preusmerjena. `POST`, `PUT` in `DELETE` vrnejo 403. |
