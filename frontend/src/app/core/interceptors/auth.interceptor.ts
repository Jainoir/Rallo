import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getAccessToken();

  const setHeaders: Record<string, string> = {
    // Lets the backend evaluate "today" in the user's timezone
    'X-Timezone': Intl.DateTimeFormat().resolvedOptions().timeZone,
  };
  if (token) {
    setHeaders['Authorization'] = `Bearer ${token}`;
  }

  return next(req.clone({ setHeaders }));
};
