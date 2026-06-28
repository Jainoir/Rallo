import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';

interface Goal {
  id: string;
  title: string;
  frequency: string;
  active: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header>
      <h1>Rallo</h1>
    </header>
    <main>
      <h2>Your goals</h2>
      <ul>
        @for (goal of goals(); track goal.id) {
          <li>{{ goal.title }} — {{ goal.frequency }}</li>
        }
        @empty {
          <li>No active goals yet.</li>
        }
      </ul>
    </main>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);
  goals = signal<Goal[]>([]);

  ngOnInit(): void {
    this.http
      .get<Goal[]>(`${environment.apiUrl}/api/goals`)
      .subscribe({ next: data => this.goals.set(data) });
  }
}
