import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Goal, GoalService } from '../../core/services/goal.service';
import { AppNotification, NotificationService } from '../../core/services/notification.service';
import { FriendsComponent } from './friends.component';
import { GroupsComponent } from './groups.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, FriendsComponent, GroupsComponent],
  template: `
    <header class="topbar">
      <span class="brand"><span class="flame">🔥</span>Rallo</span>
      <div class="topbar-actions">
        <button type="button" class="bell" (click)="toggleNotifications()">
          🔔
          @if (unreadCount() > 0) {
            <span class="badge">{{ unreadCount() }}</span>
          }
        </button>
        <button type="button" class="link" (click)="logout()">Sign out</button>
      </div>
    </header>

    @if (showNotifications()) {
      <section class="panel notifications-panel">
        <h2>Notifications</h2>
        <ul class="notifications">
          @for (notification of notifications(); track notification.id) {
            <li [class.unread]="!notification.read">
              <span>{{ notification.message }}</span>
              @if (!notification.read) {
                <button type="button" class="link" (click)="markRead(notification)">Mark read</button>
              }
            </li>
          } @empty {
            <li>No notifications yet — check in to start a streak!</li>
          }
        </ul>
      </section>
    }

    <main>
      <div class="stack">
        <section class="panel">
          <h2>New goal</h2>
          <form class="goal-form" [formGroup]="form" (ngSubmit)="createGoal()">
            <input formControlName="title" type="text" placeholder="e.g. Gym session" />
            <select formControlName="frequency">
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
            </select>
            @if (form.controls.frequency.value === 'WEEKLY') {
              <input formControlName="targetDaysPerWeek" type="number" min="1" max="7"
                     placeholder="Days/week" />
            }
            <button type="submit" [disabled]="form.invalid">Add goal</button>
          </form>
        </section>

        <section class="panel">
          <h2>Your goals</h2>
          @if (message()) {
            <p class="message">{{ message() }}</p>
          }
          <ul class="goals">
            @for (goal of goals(); track goal.id) {
              <li class="goal">
                <div>
                  <strong>{{ goal.title }}</strong>
                  <span class="muted"> — {{ goal.frequency === 'DAILY' ? 'daily' : 'weekly' }}</span>
                </div>
                <div class="goal-actions">
                  <span class="streak" title="Current streak">🔥 {{ streaks()[goal.id] ?? '…' }}</span>
                  <button type="button" (click)="checkin(goal)">Check in</button>
                </div>
              </li>
            } @empty {
              <li>No active goals yet. Add one above to start a streak.</li>
            }
          </ul>
        </section>
      </div>

      <div class="stack">
        <app-friends />
        <app-groups />
      </div>
    </main>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly goalService = inject(GoalService);
  private readonly notificationService = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  goals = signal<Goal[]>([]);
  streaks = signal<Record<string, number | undefined>>({});
  notifications = signal<AppNotification[]>([]);
  unreadCount = signal(0);
  showNotifications = signal(false);
  message = signal('');

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    frequency: ['DAILY' as 'DAILY' | 'WEEKLY'],
    targetDaysPerWeek: [3],
  });

  ngOnInit(): void {
    this.loadGoals();
    this.loadNotifications();
  }

  createGoal(): void {
    const { title, frequency, targetDaysPerWeek } = this.form.getRawValue();
    this.goalService
      .create({
        title,
        frequency,
        targetDaysPerWeek: frequency === 'WEEKLY' ? targetDaysPerWeek : undefined,
      })
      .subscribe({
        next: () => {
          this.form.reset({ title: '', frequency: 'DAILY', targetDaysPerWeek: 3 });
          this.message.set('');
          this.loadGoals();
        },
        error: () => this.message.set('Could not create the goal — check the fields and try again.'),
      });
  }

  checkin(goal: Goal): void {
    this.goalService.checkin(goal.id, this.today()).subscribe({
      next: () => {
        this.message.set(`Checked in for "${goal.title}" — keep it up!`);
        this.loadStreak(goal.id);
        this.loadNotifications();
      },
      error: err =>
        this.message.set(
          err.status === 409
            ? `Already checked in for "${goal.title}" today.`
            : 'Check-in failed — please try again.',
        ),
    });
  }

  markRead(notification: AppNotification): void {
    this.notificationService.markRead(notification.id).subscribe(() => this.loadNotifications());
  }

  toggleNotifications(): void {
    this.showNotifications.update(open => !open);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  private loadGoals(): void {
    this.goalService.list().subscribe(goals => {
      this.goals.set(goals);
      goals.forEach(goal => this.loadStreak(goal.id));
    });
  }

  private loadStreak(goalId: string): void {
    this.goalService
      .streak(goalId)
      .subscribe(count => this.streaks.update(all => ({ ...all, [goalId]: count })));
  }

  private loadNotifications(): void {
    this.notificationService.list().subscribe(list => this.notifications.set(list));
    this.notificationService.unreadCount().subscribe(count => this.unreadCount.set(count));
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
