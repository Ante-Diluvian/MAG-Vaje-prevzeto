import { NapakaPipe } from './napaka.pipe';

type Napake = Parameters<NapakaPipe['transform']>[0];
const napake = (...seznam: { kind: string; message?: string }[]) => seznam as unknown as Napake;

describe('NapakaPipe', () => {
  const pipe = new NapakaPipe();

  it('vrne prazen niz, ko polje nima napak', () => {
    expect(pipe.transform(napake())).toBe('');
  });

  it('znano vrsto napake pretvori v slovensko besedilo', () => {
    expect(pipe.transform(napake({ kind: 'required' }))).toBe('Obvezno polje');
    expect(pipe.transform(napake({ kind: 'email' }))).toBe('Neveljaven email');
  });

  it('prednost ima sporočilo validatorja, prikaže se samo prva napaka', () => {
    expect(
      pipe.transform(
        napake({ kind: 'min', message: 'Letnik mora biti vsaj 1900' }, { kind: 'required' }),
      ),
    ).toBe('Letnik mora biti vsaj 1900');
  });

  it('za neznano vrsto napake vrne splošno besedilo', () => {
    expect(pipe.transform(napake({ kind: 'neznana' }))).toBe('Neveljaven vnos');
  });
});
