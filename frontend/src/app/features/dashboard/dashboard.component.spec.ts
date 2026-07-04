import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { environment } from '../../../environments/environment';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let httpMock: HttpTestingController;

  const gymGoal = {
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
      imports: [DashboardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(DashboardComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  function flushInitialLoad(goals: unknown[] = [gymGoal]): void {
    fixture.detectChanges(); // triggers ngOnInit
    httpMock.expectOne(`${environment.apiUrl}/api/goals`).flush(goals);
    httpMock.expectOne(`${environment.apiUrl}/api/notifications`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/notifications/unread-count`).flush(0);
    goals.forEach(goal =>
      httpMock
        .expectOne(`${environment.apiUrl}/api/checkins/goals/${(goal as { id: string }).id}/streak`)
        .flush(4),
    );
    fixture.detectChanges();
  }

  it('renders goals with their streaks after load', () => {
    flushInitialLoad();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Gym');
    expect(text).toContain('4');
  });

  it('shows an empty state when there are no goals', () => {
    flushInitialLoad([]);

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('No active goals yet');
  });

  it('shows a friendly message on duplicate check-in (409)', () => {
    flushInitialLoad();

    fixture.componentInstance.checkin(fixture.componentInstance.goals()[0]);
    httpMock
      .expectOne(`${environment.apiUrl}/api/checkins/goals/goal-1`)
      .flush(null, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Already checked in for "Gym" today.');
  });

  it('refreshes streak and notifications after a successful check-in', () => {
    flushInitialLoad();

    fixture.componentInstance.checkin(fixture.componentInstance.goals()[0]);
    httpMock
      .expectOne(`${environment.apiUrl}/api/checkins/goals/goal-1`)
      .flush(null, { status: 201, statusText: 'Created' });
    httpMock.expectOne(`${environment.apiUrl}/api/checkins/goals/goal-1/streak`).flush(5);
    httpMock.expectOne(`${environment.apiUrl}/api/notifications`).flush([]);
    httpMock.expectOne(`${environment.apiUrl}/api/notifications/unread-count`).flush(1);
    fixture.detectChanges();

    expect(fixture.componentInstance.streaks()['goal-1']).toBe(5);
    expect(fixture.componentInstance.unreadCount()).toBe(1);
  });
});
