import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LeaderboardEntry } from './friends.service';

export interface Group {
  id: string;
  name: string;
  ownerId: string;
  createdAt: string;
}

export interface GroupMember {
  userId: string;
  username: string;
}

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  myGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(`${this.apiUrl}/api/groups`);
  }

  create(name: string): Observable<Group> {
    return this.http.post<Group>(`${this.apiUrl}/api/groups`, { name });
  }

  addMember(groupId: string, username: string): Observable<GroupMember> {
    return this.http.post<GroupMember>(`${this.apiUrl}/api/groups/${groupId}/members`, { username });
  }

  leaderboard(groupId: string): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.apiUrl}/api/checkins/streaks/groups/${groupId}`);
  }
}
