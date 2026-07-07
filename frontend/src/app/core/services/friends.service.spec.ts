import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { FriendsService, LeaderboardEntry } from './friends.service';
import { environment } from '../../../environments/environment';

describe('FriendsService', () => {
  let service: FriendsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FriendsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('fetches the friends streak leaderboard', () => {
    const entries: LeaderboardEntry[] = [
      { userId: 'u1', username: 'jane', bestStreak: 9 },
      { userId: 'me', username: 'sanjai', bestStreak: 3 },
    ];
    let result: LeaderboardEntry[] = [];
    service.leaderboard().subscribe(list => (result = list));

    const req = httpMock.expectOne(`${environment.apiUrl}/api/checkins/streaks/friends`);
    expect(req.request.method).toBe('GET');
    req.flush(entries);

    expect(result).toEqual(entries);
  });

  it('sends a friend request by username', () => {
    service.sendRequest('jane').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/friends/requests`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'jane' });
    req.flush({ id: 'r1', requesterId: 'me', requesterUsername: 'sanjai', createdAt: '' });
  });

  it('accepts a pending request', () => {
    service.accept('r1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/friends/requests/r1/accept`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});
