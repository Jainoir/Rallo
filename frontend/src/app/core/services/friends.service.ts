import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FriendRequest {
  id: string;
  requesterId: string;
  requesterUsername: string;
  createdAt: string;
}

export interface LeaderboardEntry {
  userId: string;
  username: string;
  bestStreak: number;
}

@Injectable({ providedIn: 'root' })
export class FriendsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /** Self + friends ranked by best current streak. */
  leaderboard(): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.apiUrl}/api/checkins/streaks/friends`);
  }

  incomingRequests(): Observable<FriendRequest[]> {
    return this.http.get<FriendRequest[]>(`${this.apiUrl}/api/friends/requests`);
  }

  sendRequest(username: string): Observable<FriendRequest> {
    return this.http.post<FriendRequest>(`${this.apiUrl}/api/friends/requests`, { username });
  }

  accept(requestId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/api/friends/requests/${requestId}/accept`, {});
  }
}
