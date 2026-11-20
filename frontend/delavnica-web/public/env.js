// Nastavitve ob zagonu aplikacije. Job deploy to datoteko prepiše z URL-jem nameščenega backenda
// (spremenljivka API_URL okolja production). Lokalno je apiUrl prazen in zahteve na /api gredo
// prek razvojnega proxyja (proxy.conf.json).
window.env = { apiUrl: '' };
