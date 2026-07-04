import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AppNotification, NotificationService } from './notification.service';
import { environment } from '../../../environments/environment';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  const milestone: AppNotification = {
    id: 'notif-1',
    type: 'STREAK_MILESTONE',
    message: "Milestone! You're on a 7-day streak for 'Gym'. Keep it going!",
    read: false,
    createdAt: '2026-07-01T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists notifications', () => {
    let result: AppNotification[] = [];
    service.list().subscribe(list => (result = list));

    const req = httpMock.expectOne(`${environment.apiUrl}/api/notifications`);
    expect(req.request.method).toBe('GET');
    req.flush([milestone]);

    expect(result).toEqual([milestone]);
  });

  it('fetches the unread count', () => {
    let count = 0;
    service.unreadCount().subscribe(value => (count = value));

    const req = httpMock.expectOne(`${environment.apiUrl}/api/notifications/unread-count`);
    expect(req.request.method).toBe('GET');
    req.flush(3);

    expect(count).toBe(3);
  });

  it('marks a notification as read via PATCH', () => {
    service.markRead('notif-1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/notifications/notif-1/read`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...milestone, read: true });
  });
});
