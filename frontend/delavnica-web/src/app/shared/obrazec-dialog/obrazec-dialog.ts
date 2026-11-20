import { Component, inject } from '@angular/core';
import { FieldTree, FormField } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Observable, firstValueFrom } from 'rxjs';
import { NapakaPipe } from '../napaka.pipe';

export interface Moznost {
  vrednost: string;
  oznaka: string;
}

// Opis enega polja: okno ga izriše samo. `moznosti` naredi spustni seznam (lahko je signal, ki se naloži kasneje).
export interface PoljeObrazca {
  kljuc: string; // ime polja v obrazcu
  oznaka: string;
  tip?: 'number' | 'email' | 'password';
  moznosti?: () => Moznost[];
  placeholder?: string; // prazna izbira na vrhu spustnega seznama
}

export interface ObrazecDialogData<T extends object = object> {
  naslov: string;
  // Shrani je videti onemogočen, dokler je obrazec neveljaven
  obrazec: FieldTree<T>;
  polja: PoljeObrazca[];
  shrani: () => Observable<unknown>; // klic strežnika; okno se zapre šele po uspehu
}

// Okno z naslovom, polji obrazca ter gumbi Prekliči in Shrani. Zapre se z `true`, če je bilo shranjeno.
@Component({
  imports: [FormField, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, NapakaPipe],
  templateUrl: './obrazec-dialog.html',
  styleUrl: './obrazec-dialog.scss',
})
export class ObrazecDialog {
  protected readonly podatki = inject<ObrazecDialogData>(MAT_DIALOG_DATA);
  private okno = inject(MatDialogRef<ObrazecDialog>);

  // Polja se izberejo po imenu (obrazec[kljuc]). Vrsto vrednosti določa `tip` v opisu polja:
  // številska polja imajo vrednost number, vsa ostala (besedilo, email, geslo, izbira) string.
  protected besedilno(kljuc: string) {
    return (this.podatki.obrazec as unknown as Record<string, FieldTree<string>>)[kljuc];
  }

  protected stevilsko(kljuc: string) {
    return (this.podatki.obrazec as unknown as Record<string, FieldTree<number>>)[kljuc];
  }

  protected async shrani() {
    const obrazec = this.podatki.obrazec();
    if (obrazec.invalid()) {
      obrazec.markAsTouched(); // pokaže napake pod polji, strežnika ne kličemo
      return;
    }
    try {
      await firstValueFrom(this.podatki.shrani());
      this.okno.close(true);
    } catch {
      // napako je že prikazal interceptor, okno ostane odprto
    }
  }
}

export function odpriObrazec<T extends object>(dialog: MatDialog, podatki: ObrazecDialogData<T>) {
  return firstValueFrom(dialog.open(ObrazecDialog, { data: podatki, width: '480px' }).afterClosed());
}
