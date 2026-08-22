# Review UAT Environment Scripts

This directory contains repeatable startup, stop, and health-check scripts for
the Review Engine browser-level UAT environment.

## Service List

The current code configuration uses these local test ports:

| Service | Directory | Default Port | Source |
| --- | --- | ---: | --- |
| gateway | `old-code/teaching-gateway` | `9889` | `bootstrap-test.yml` and `old-code-admin/vite.config.js` |
| auth | `old-code/teaching-auth` | `9224` | `bootstrap-test.yml` |
| system | `old-code/teaching-modules/teaching-system` | `9211` | `bootstrap-test.yml` |
| competition | `old-code/teaching-modules/teaching-competition` | `9205` | `bootstrap-test.yml` |
| old-code-admin | `old-code-admin` | `8081` | `vite.config.js` |

Note: `9211` is the system service in the current code. The gateway used by the
admin Vite proxy is `9889`.

## Local Configuration

Copy the example file and fill local-only secrets:

```bash
cp scripts/review/review_uat.env.example scripts/review/review_uat.env
```

Required for database auth checks:

```bash
MYSQL_PASSWORD=your_local_password
```

All ports can be overridden:

```bash
GATEWAY_PORT=9889
AUTH_PORT=9224
SYSTEM_PORT=9211
COMPETITION_PORT=9205
ADMIN_PORT=8081
```

If Nacos, Redis, or MySQL are exposed through a local proxy address, set:

```bash
NACOS_HOST=10.10.10.10
REDIS_HOST=10.10.10.10
MYSQL_HOST=10.10.10.10
```

The startup script passes `NACOS_HOST:NACOS_PORT` as local runtime arguments to
Spring Boot. It does not modify production or repository configuration files.

## Start

```bash
scripts/review/start_review_uat.sh
```

The startup script:

- checks whether expected ports are already listening;
- refuses to kill unrelated processes;
- optionally prebuilds Maven module dependencies with `mvn -pl <module> -am -Dmaven.test.skip=true install`;
- starts gateway, auth, system, competition, and admin services;
- writes logs and pid files to `logs/review-uat/`;
- runs the health check after startup.

To start only selected services:

```bash
START_ADMIN=0 START_GATEWAY=1 scripts/review/start_review_uat.sh
```

To skip the Maven prebuild when dependencies are already installed:

```bash
MAVEN_PREBUILD=0 scripts/review/start_review_uat.sh
```

## Check

```bash
MYSQL_PASSWORD=your_local_password scripts/review/check_review_uat.py
```

The health check verifies:

- port listeners and owning processes;
- HTTP reachability;
- gateway review path forwarding;
- admin dev-server proxy forwarding;
- MySQL connection to `jiaoxue_test`;
- Nacos reachability;
- Redis PING.

The generated check report is written to:

```text
logs/review-uat/check_report.md
```

## Stop

```bash
scripts/review/stop_review_uat.sh
```

The stop script only stops processes recorded in pid files created by
`start_review_uat.sh`. The startup script refreshes pid files to the actual
listening process after ports become ready. The stop script does not kill by
port.

## Logs

```text
logs/review-uat/gateway.log
logs/review-uat/auth.log
logs/review-uat/system.log
logs/review-uat/competition.log
logs/review-uat/admin.log
```

Pid files:

```text
logs/review-uat/gateway.pid
logs/review-uat/auth.pid
logs/review-uat/system.pid
logs/review-uat/competition.pid
logs/review-uat/admin.pid
```

## Common Issues

### Port Not Listening

Run:

```bash
scripts/review/check_review_uat.py
```

Then inspect the corresponding log in `logs/review-uat/`.

### Port Already Occupied

The startup script prints the listener PID and command. Stop the unrelated
process manually, or change the port through environment variables.

### Frontend 404

Confirm `old-code-admin` is running on `ADMIN_PORT` and the page route exists.
The admin Vite server proxies API calls from `/dev-api` to the gateway.

### Gateway Forwarding Failed

Confirm gateway `9889`, auth `9224`, system `9211`, and competition `9205` are
all running and registered in Nacos. A `401` or `403` response can still prove
the route is reachable; `404` or `502` usually means routing or service registry
is wrong.

### Backend Startup Failed

Check `logs/review-uat/*.log`. Common causes are missing Nacos, missing shared
Nacos config, database connection failure, or Java/Maven dependency issues.

### Database Connection Failed

Set `MYSQL_PASSWORD` in local environment or `scripts/review/review_uat.env`.
If the database is exposed through a proxy address, set `MYSQL_HOST`.

### Nacos Or Redis Unavailable

The code defaults to `127.0.0.1:8848` for Nacos and `127.0.0.1:6379` for Redis.
If your local UAT dependencies listen on another address, override
`NACOS_HOST` or `REDIS_HOST`.
