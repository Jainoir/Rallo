import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Group, GroupService } from './group.service';
import { environment } from '../../../environments/environment';

describe('GroupService', () => {
  let service: GroupService;
  let httpMock: HttpTestingController;

  const gymCrew: Group = { id: 'g1', name: 'Gym crew', ownerId: 'me', createdAt: '' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GroupService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('creates a group', () => {
    service.create('Gym crew').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/groups`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Gym crew' });
    req.flush(gymCrew);
  });

  it('adds a member by username', () => {
    service.addMember('g1', 'jane').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/groups/g1/members`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'jane' });
    req.flush({ userId: 'u1', username: 'jane' });
  });

  it('fetches a group leaderboard', () => {
    service.leaderboard('g1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/checkins/streaks/groups/g1`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
