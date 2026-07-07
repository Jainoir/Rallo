import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Group, GroupService } from '../../core/services/group.service';
import { LeaderboardEntry } from '../../core/services/friends.service';

@Component({
  selector: 'app-groups',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <section class="panel">
      <h2>Groups</h2>

      <form class="goal-form" [formGroup]="createForm" (ngSubmit)="create()">
        <input formControlName="name" type="text" placeholder="New group name" />
        <button type="submit" [disabled]="createForm.invalid">Create group</button>
      </form>
      @if (message()) {
        <p class="message">{{ message() }}</p>
      }

      <ul class="goals">
        @for (group of groups(); track group.id) {
          <li class="goal">
            <strong>{{ group.name }}</strong>
            <button type="button" (click)="select(group)">Leaderboard</button>
          </li>
        } @empty {
          <li>No groups yet — create one and invite your friends.</li>
        }
      </ul>

      @if (selected(); as group) {
        <h3>{{ group.name }} — leaderboard</h3>
        <ol class="leaderboard">
          @for (entry of leaderboard(); track entry.userId) {
            <li>
              <span>{{ entry.username }}</span>
              <span class="streak">🔥 {{ entry.bestStreak }}</span>
            </li>
          }
        </ol>
        <form class="goal-form" [formGroup]="inviteForm" (ngSubmit)="addMember(group)">
          <input formControlName="username" type="text" placeholder="Add member by username" />
          <button type="submit" [disabled]="inviteForm.invalid">Add</button>
        </form>
      }
    </section>
  `,
})
export class GroupsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly groupService = inject(GroupService);

  groups = signal<Group[]>([]);
  selected = signal<Group | null>(null);
  leaderboard = signal<LeaderboardEntry[]>([]);
  message = signal('');

  createForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
  });

  inviteForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
  });

  ngOnInit(): void {
    this.groupService.myGroups().subscribe(groups => this.groups.set(groups));
  }

  create(): void {
    this.groupService.create(this.createForm.getRawValue().name).subscribe({
      next: group => {
        this.createForm.reset({ name: '' });
        this.message.set('');
        this.groups.update(all => [...all, group]);
        this.select(group);
      },
      error: () => this.message.set('Could not create the group.'),
    });
  }

  select(group: Group): void {
    this.selected.set(group);
    this.groupService.leaderboard(group.id)
        .subscribe(entries => this.leaderboard.set(entries));
  }

  addMember(group: Group): void {
    const username = this.inviteForm.getRawValue().username;
    this.groupService.addMember(group.id, username).subscribe({
      next: () => {
        this.inviteForm.reset({ username: '' });
        this.message.set(`${username} added to ${group.name}.`);
        this.select(group);
      },
      error: err =>
        this.message.set(
          err.status === 404
            ? 'Only the group owner can add members (or no such user).'
            : err.error?.message ?? 'Could not add the member.',
        ),
    });
  }
}
