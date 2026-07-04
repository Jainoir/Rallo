import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Goal, GoalService } from './goal.service';
import { environment } from '../../../environments/environment';

describe('GoalService', () => {
  let service: GoalService;
  let httpMock: HttpTestingController;

  const gymGoal: Goal = {
    id: 'goal-1',
    title: 'Gym',
    description: null,
    frequency: 'DAILY',
    targetDaysPerWeek: null,
    active: true,
    createdAt: '2026-07-01T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GoalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists goals', () => {
    let result: Goal[] = [];
    service.list().subscribe(goals => (result = goals));

    const req = httpMock.expectOne(`${environment.apiUrl}/api/goals`);
    expect(req.request.method).toBe('GET');
    req.flush([gymGoal]);

    expect(result).toEqual([gymGoal]);
  });

  it('creates a goal', () => {
    service.create({ title: 'Gym', frequency: 'DAILY' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/goals`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Gym', frequency: 'DAILY' });
    req.flush(gymGoal);
  });

  it('fetches the streak for a goal', () => {
    let streak = 0;
    service.streak('goal-1').subscribe(count => (streak = count));

    const req = httpMock.expectOne(`${environment.apiUrl}/api/checkins/goals/goal-1/streak`);
    expect(req.request.method).toBe('GET');
    req.flush(5);

    expect(streak).toBe(5);
  });

  it('records a check-in with the given date', () => {
    service.checkin('goal-1', '2026-07-01').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/checkins/goals/goal-1`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ checkinDate: '2026-07-01' });
    req.flush(null, { status: 201, statusText: 'Created' });
  });
});
