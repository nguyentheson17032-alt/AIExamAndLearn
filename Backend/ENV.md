# Environment variables

| Variable | Required | Description |
| --- | --- | --- |
| `DATABASE_URL` | no | JDBC URL, default `jdbc:postgresql://localhost:5432/exam_warehouse` |
| `DATABASE_USERNAME` | no | Default `exam` |
| `DATABASE_PASSWORD` | no | Default `exam` |
| `JWT_SECRET` | yes | Base64-encoded HS256 key, at least 256 bits |
| `APP_AI_ENABLED` | no | `true` to use Spring AI / OpenAI |
| `OPENAI_API_KEY` | when AI enabled | OpenAI API key |
| `OPENAI_MODEL` | no | Default `gpt-4o-mini` |
