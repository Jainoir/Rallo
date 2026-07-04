import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Goal {
  id: string;
  title: string;
  description: string | null;
  frequency: 'DAILY' | 'WEEKLY';
  targetDaysPerWeek: number | null;
  active: boolean;
  createdAt: string;
}

export interface CreateGoalRequest {
  title: string;
  description?: string;
  frequency: 'DAILY' | 'WEEKLY';
  targetDaysPerWeek?: number;
}

@Injectable({ providedIn: 'root' })
export class GoalService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  list(): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/api/goals`);
  }

  create(request: CreateGoalRequest): Observable<Goal> {
    return this.http.post<Goal>(`${this.apiUrl}/api/goals`, request);
  }

  streak(goalId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/api/checkins/goals/${goalId}/streak`);
  }

  checkin(goalId: string, checkinDate: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/api/checkins/goals/${goalId}`, { checkinDate });
  }
}
