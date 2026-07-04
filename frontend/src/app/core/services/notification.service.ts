import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AppNotification {
  id: string;
  type: 'STREAK_MILESTONE' | 'STREAK_BROKEN' | 'STREAK_REMINDER' | 'CHECKIN_CONFIRMED';
  message: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/notifications`;

  list(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.apiUrl);
  }

  unreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count`);
  }

  markRead(id: string): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.apiUrl}/${id}/read`, {});
  }
}
