# AI Exam Warehouse

Backend for an exam question warehouse with Elo-based practice and optional Spring AI assistance.

## Stack

- Java 21
- Spring Boot 3.4
- PostgreSQL 16
- Flyway
- JWT authentication
- Spring AI (OpenAI), disabled by default

## Run locally

1. Copy `.env.example` and set `JWT_SECRET` to a base64-encoded 256-bit key.
2. Start the database:

```bash
cd Backend
docker compose up -d
```

3. Start the API (`JAVA_HOME` must point to JDK 21):

```bash
cd Backend
.\mvnw.cmd spring-boot:run
```

## Enable AI

Set:

```
APP_AI_ENABLED=true
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-4o-mini
```

Without AI, the stub client still classifies, grades exact matches, and generates placeholder practice items so the API remains usable.

## Main APIs

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Register |
| POST | `/api/v1/auth/login` | Login |
| POST | `/api/v1/auth/refresh` | Rotate refresh token |
| GET | `/api/v1/users/me` | Profile, Elo, rank |
| GET | `/api/v1/users/me/elo-history` | Elo history |
| POST | `/api/v1/questions` | Upload a question |
| GET | `/api/v1/questions` | List questions |
| POST | `/api/v1/questions/generate` | AI-generate questions |
| POST | `/api/v1/questions/{id}/classify` | AI-classify a question |
| POST | `/api/v1/exams` | Create exam or exercise |
| POST | `/api/v1/exams/generate` | AI-generate an exam set |
| POST | `/api/v1/exams/{id}/similar` | AI-generate similar exercises |
| POST | `/api/v1/exams/practice` | Adaptive practice around current Elo |
| POST | `/api/v1/exams/{examId}/attempts` | Start an attempt |
| POST | `/api/v1/attempts/{id}/answers` | Save answers |
| POST | `/api/v1/attempts/{id}/submit` | Grade and update Elo |

Success bodies use `ApiResponse`. Errors use RFC 9457 Problem Details.

## Elo ranks

| Rank | Elo |
| --- | --- |
| BRONZE | 0-999 |
| SILVER | 1000-1299 |
| GOLD | 1300-1599 |
| PLATINUM | 1600-1899 |
| DIAMOND | 1900+ |

After each graded attempt the backend updates learner Elo, question Elo, and Elo history. When AI is enabled it can also recommend the K-factor.

## Tests

```bash
cd Backend
.\mvnw.cmd test
```
