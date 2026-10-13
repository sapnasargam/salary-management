import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatIconModule],
  template: `
    <header class="topbar">
      <div class="topbar-inner">
        <a routerLink="/" class="brand">
          <span class="mark" aria-hidden="true">a</span>
          <span>acme<span class="dot">.</span>salaries</span>
        </a>
        <nav aria-label="Main">
          <a routerLink="/employees" routerLinkActive="active" class="pill">
            <mat-icon>badge</mat-icon> Employees
          </a>
          <a routerLink="/insights" routerLinkActive="active" class="pill">
            <mat-icon>insights</mat-icon> Insights
          </a>
        </nav>
      </div>
    </header>
    <main class="content">
      <router-outlet />
    </main>
  `,
  styles: `
    .topbar { position: sticky; top: 0; z-index: 10; background: var(--brand-green); }
    .topbar-inner {
      max-width: 1280px; margin: 0 auto; padding: 14px 24px;
      display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
    }
    .brand {
      display: flex; align-items: center; gap: 10px; text-decoration: none;
      color: var(--brand-mint); font-family: 'Fraunces', serif; font-size: 24px;
    }
    .mark {
      display: grid; place-items: center; width: 34px; height: 34px; border-radius: 50%;
      background: var(--brand-lime); color: var(--brand-green); font-size: 20px; font-weight: 600;
    }
    .dot { color: var(--brand-lime); }
    nav { display: flex; gap: 10px; }
    .pill {
      display: inline-flex; align-items: center; gap: 8px; padding: 8px 20px;
      border: 1px solid var(--brand-mint); border-radius: 999px;
      color: var(--brand-mint); text-decoration: none; font-size: 16px;
      transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
    }
    .pill mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .pill:hover { background: var(--brand-mint); color: var(--brand-green); }
    .pill.active { background: var(--brand-lime); border-color: var(--brand-lime); color: var(--brand-green); }
    .content { max-width: 1280px; margin: 0 auto; padding: 40px 24px 64px; }
  `,
})
export class AppComponent {}
