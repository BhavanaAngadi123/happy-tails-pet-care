# Happy Tails password reset setup

The password-reset feature is secure but intentionally disabled until SMTP delivery is configured.

## Render environment variables

Set these in the `happy-tails-pet-care` Render service:

- `MAIL_HOST` — SMTP host from your email provider
- `MAIL_PORT` — normally `587`
- `MAIL_USERNAME` — SMTP username
- `MAIL_PASSWORD` — SMTP password/API key
- `MAIL_FROM` — verified sender address
- `MAIL_SMTP_AUTH` — `true`
- `MAIL_STARTTLS` — `true`
- `PASSWORD_RESET_ENABLED` — keep `false` until the SMTP test succeeds, then set to `true`

`APP_BASE_URL` is optional on Render. Happy Tails falls back to Render's `RENDER_EXTERNAL_URL` automatically.

## Safe activation sequence

1. Add the SMTP credentials as Render secrets. Never put them in GitHub.
2. Deploy while `PASSWORD_RESET_ENABLED=false`.
3. Confirm `/actuator/health` remains healthy.
4. Set `PASSWORD_RESET_ENABLED=true` and redeploy.
5. Open the login page. `Forgot password?` should now appear.
6. Request a reset for an account you control.
7. Confirm the email arrives and the link points to the deployed Happy Tails domain.
8. Set a new password and verify the old password no longer works.
9. Verify the same reset link cannot be reused.

## Provider notes

Use an SMTP provider that supports authenticated TLS delivery. The application does not require a specific vendor. For production, use a verified sender/domain rather than a personal mailbox when possible.

## Security behavior

- Reset requests do not reveal whether an email is registered.
- Tokens expire after 30 minutes.
- Only token hashes are stored.
- Tokens are single-use.
- Issuing a new reset invalidates prior unused reset tokens.
- Passwords must be 10–128 characters and contain at least one letter and one number.
