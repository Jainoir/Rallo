import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    imports: [ReactiveFormsModule, RouterLink],
    template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-brand">🔥 Rallo</div>
        <p class="auth-tagline">Build streaks. Stay accountable.</p>
        <div class="auth-container">
          <h2>Sign in to Rallo</h2>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <input formControlName="email" type="email" placeholder="Email" />
            <input formControlName="password" type="password" placeholder="Password" />
            <button type="submit" [disabled]="form.invalid">Sign in</button>
            @if (error) {
              <p>{{ error }}</p>
            }
          </form>
          <a routerLink="/auth/register">New here? Create an account</a>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  error = '';

  submit(): void {
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => (this.error = 'Invalid email or password'),
    });
  }
}
