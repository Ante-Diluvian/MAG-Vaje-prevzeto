// JWT za teste: frontend prebere samo podatke (vlogo), podpisa ne preverja.
export function testniZeton(vloga: string): string {
  const podatki = btoa(JSON.stringify({ sub: 'testni-uporabnik', vloga }))
    .replace(/=+$/, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `glava.${podatki}.podpis`;
}
