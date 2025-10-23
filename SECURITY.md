# Security Policy

## Supported Versions

We take the security of OpenIsle seriously. The following versions are currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 0.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We appreciate your efforts to responsibly disclose your findings and will make every effort to acknowledge your contributions.

### How to Report a Security Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them via one of the following methods:

1. **Email**: Send a detailed report to the project maintainer (check the repository for contact information)
2. **GitHub Security Advisory**: Use GitHub's private vulnerability reporting feature at https://github.com/nagisa77/OpenIsle/security/advisories/new

### What to Include in Your Report

To help us better understand the nature and scope of the issue, please include as much of the following information as possible:

- Type of issue (e.g., SQL injection, XSS, authentication bypass, etc.)
- Full paths of source file(s) related to the manifestation of the issue
- The location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit it

### Response Timeline

- **Initial Response**: We will acknowledge your report within 48 hours
- **Status Updates**: We will provide status updates at least every 5 business days
- **Resolution**: We aim to resolve critical vulnerabilities within 30 days of disclosure

### What to Expect

After you submit a report:

1. We will confirm receipt of your vulnerability report and may ask for additional information
2. We will investigate the issue and determine its impact and severity
3. We will work on a fix and coordinate disclosure timing with you
4. Once the fix is ready, we will release it and publicly acknowledge your contribution (unless you prefer to remain anonymous)

## Security Considerations for Deployment

### Authentication & Authorization

- **JWT Tokens**: Ensure `JWT_SECRET` environment variable is set to a strong, random value (minimum 256 bits)
- **OAuth Credentials**: Keep OAuth client secrets secure and never commit them to version control
- **Session Management**: Configure appropriate session timeout values

### Database Security

- Use strong database passwords
- Never expose database ports publicly
- Use database connection encryption when available
- Regularly backup your database

### API Security

- Enable rate limiting to prevent abuse
- Validate all user inputs on both client and server side
- Use HTTPS in production environments
- Configure CORS properly to restrict origins

### Environment Variables

The following sensitive environment variables should be kept secure:

- `JWT_SECRET` - JWT signing key
- `GOOGLE_CLIENT_SECRET` - Google OAuth credentials
- `GITHUB_CLIENT_SECRET` - GitHub OAuth credentials
- `DISCORD_CLIENT_SECRET` - Discord OAuth credentials
- `TWITTER_CLIENT_SECRET` - Twitter OAuth credentials
- `WEBPUSH_PRIVATE_KEY` - Web push notification private key
- Database connection strings and credentials
- Cloud storage credentials (Tencent COS)

**Never commit these values to version control or expose them in logs.**

### File Upload Security

- Validate file types and sizes
- Scan uploaded files for malware
- Store uploaded files outside the web root
- Use cloud storage with proper access controls

### Password Security

- Configure password strength requirements via environment variables
- Use bcrypt or similar strong hashing algorithms (already implemented in Spring Security)
- Implement account lockout after failed login attempts

### Web Push Notifications

- Keep `WEBPUSH_PRIVATE_KEY` secret and secure
- Only send notifications to users who have explicitly opted in
- Validate notification payloads

### Dependency Management

- Regularly update dependencies to patch known vulnerabilities
- Run `mvn dependency-check:check` to scan for vulnerable dependencies
- Monitor GitHub security advisories for this project

### Production Deployment Checklist

- [ ] Use HTTPS/TLS for all connections
- [ ] Set strong, unique secrets for all environment variables
- [ ] Enable CSRF protection
- [ ] Configure secure headers (CSP, X-Frame-Options, etc.)
- [ ] Disable debug mode and verbose error messages
- [ ] Set up proper logging and monitoring
- [ ] Implement rate limiting and DDoS protection
- [ ] Regular security updates and patches
- [ ] Database backups and disaster recovery plan
- [ ] Restrict admin access to trusted IPs when possible

## Known Security Features

OpenIsle includes the following security features:

- JWT-based authentication with configurable expiration
- OAuth 2.0 integration with major providers
- Password strength validation
- Protection codes for sensitive operations
- Input validation and sanitization
- SQL injection prevention through ORM (JPA/Hibernate)
- XSS protection in Vue.js templates
- CSRF protection (Spring Security)

## Security Best Practices for Contributors

- Never commit credentials, API keys, or secrets
- Follow secure coding practices (OWASP Top 10)
- Validate and sanitize all user inputs
- Use parameterized queries for database operations
- Implement proper error handling without exposing sensitive information
- Write security tests for new features
- Review code for security issues before submitting PRs

## Disclosure Policy

When we receive a security bug report, we will:

1. Confirm the problem and determine affected versions
2. Audit code to find any similar problems
3. Prepare fixes for all supported versions
4. Release patches as soon as possible

We appreciate your help in keeping OpenIsle and its users safe!

## Attribution

We believe in recognizing security researchers who help improve OpenIsle's security. With your permission, we will acknowledge your contribution in:

- Security advisory
- Release notes
- A security hall of fame (if established)

If you prefer to remain anonymous, we will respect your wishes.

## Contact

For any security-related questions or concerns, please reach out through the channels mentioned above.

---

Thank you for helping keep OpenIsle secure!
