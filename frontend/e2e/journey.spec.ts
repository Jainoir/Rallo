import { test, expect } from '@playwright/test';

// Full user journey across all services: register (auth), create a goal and
// check in (checkin), streak display, and the social panels.
test('register → create goal → check in → streak appears', async ({ page }) => {
  const n = Math.floor(Math.random() * 1_000_000);

  await page.goto('/auth/register');
  await page.fill('input[placeholder="Username"]', `e2e${n}`);
  await page.fill('input[placeholder="Email"]', `e2e${n}@rallo.dev`);
  await page.fill('input[placeholder="Password (min 8 chars)"]', 'password123');
  await page.click('button[type=submit]');
  await page.waitForURL('**/dashboard');

  // Create a goal
  await page.fill('input[placeholder="e.g. Gym session"]', 'E2E daily goal');
  await page.click('button:has-text("Add goal")');
  const goalRow = page.locator('li.goal', { hasText: 'E2E daily goal' });
  await expect(goalRow).toBeVisible();

  // Check in and see the streak reach 1
  await goalRow.locator('button:has-text("Check in")').click();
  await expect(page.locator('.message')).toContainText('Checked in');
  await expect(goalRow.locator('.streak')).toHaveText(/1/);

  // Duplicate check-in is handled gracefully
  await goalRow.locator('button:has-text("Check in")').click();
  await expect(page.locator('.message')).toContainText('Already checked in');

  // Social panels rendered with self on the friends leaderboard
  await expect(page.locator('app-friends')).toContainText(`e2e${n}`);
  await expect(page.locator('app-groups')).toContainText('No groups yet');
});

test('unauthenticated users are redirected to login', async ({ page }) => {
  await page.goto('/dashboard');
  await page.waitForURL('**/auth/login');
  await expect(page.locator('h2')).toHaveText('Sign in to Rallo');
});
