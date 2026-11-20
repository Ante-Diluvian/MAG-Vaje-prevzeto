import { TestBed } from '@angular/core/testing';
import { StatusBadge } from './status-badge';

describe('StatusBadge', () => {
  it('prikaže slovensko oznako stanja in barvni razred', () => {
    const fixture = TestBed.createComponent(StatusBadge);
    fixture.componentRef.setInput('stanje', 'V_DELU');
    fixture.detectChanges();

    const oznaka: HTMLElement = fixture.nativeElement.querySelector('.status');
    expect(oznaka.textContent).toBe('V delu');
    expect(oznaka.classList).toContain('status--v_delu');
  });
});
