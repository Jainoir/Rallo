import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  FriendRequest,
  FriendsService,
  LeaderboardEntry,
} from '../../core/services/friends.service';

@Component({
    selector: 'app-friends',
    imports: [ReactiveFormsModule],
    template: `
    <section class="panel">
      <h2>Friends</h2>

      <form class="goal-form" [formGroup]="form" (ngSubmit)="sendRequest()">
        <input formControlName="username" type="text" placeholder="Add friend by username" />
        <button type="submit" [disabled]="form.invalid">Send request</button>
      </form>
      @if (message()) {
        <p class="message">{{ message() }}</p>
      }

      @if (requests().length > 0) {
        <h3>Requests</h3>
        <ul class="notifications">
          @for (request of requests(); track request.id) {
            <li class="unread">
              <span><strong>{{ request.requesterUsername }}</strong> wants to be your friend</span>
              <button type="button" class="link" (click)="accept(request)">Accept</button>
            </li>
          }
        </ul>
      }

      <h3>Streak leaderboard</h3>
      <ol class="leaderboard">
        @for (entry of leaderboard(); track entry.userId) {
          <li>
            <span>{{ entry.username }}</span>
            <span class="streak">🔥 {{ entry.bestStreak }}</span>
          </li>
        } @empty {
          <li>Add friends to compare streaks.</li>
        }
      </ol>
    </section>
  `
})
export class FriendsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly friendsService = inject(FriendsService);

  leaderboard = signal<LeaderboardEntry[]>([]);
  requests = signal<FriendRequest[]>([]);
  message = signal('');

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
  });

  ngOnInit(): void {
    this.refresh();
  }

  sendRequest(): void {
    const username = this.form.getRawValue().username;
    this.friendsService.sendRequest(username).subscribe({
      next: () => {
        this.form.reset({ username: '' });
        this.message.set(`Request sent to ${username}.`);
      },
      error: err =>
        this.message.set(
          err.status === 404
            ? `No user named "${username}".`
            : err.error?.message ?? 'Could not send the request.',
        ),
    });
  }

  accept(request: FriendRequest): void {
    this.friendsService.accept(request.id).subscribe(() => {
      this.message.set(`You and ${request.requesterUsername} are now friends!`);
      this.refresh();
    });
  }

  private refresh(): void {
    this.friendsService.leaderboard().subscribe(entries => this.leaderboard.set(entries));
    this.friendsService.incomingRequests().subscribe(requests => this.requests.set(requests));
  }
}
