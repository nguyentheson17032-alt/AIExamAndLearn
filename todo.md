# TODO

## Phase 1: Project Setup

- [x] Create Backend folder
- [x] Initialize Spring Boot backend
- [x] Configure project dependencies
- [x] Add Maven Wrapper
- [x] Configure application properties
- [x] Add Docker Compose for PostgreSQL

## Phase 2: Database

- [x] Configure Flyway
- [x] Create users table migration
- [x] Create refresh_tokens table migration
- [x] Create questions and question_choices tables migration
- [x] Create exams and exam_questions tables migration
- [x] Create attempts and attempt_answers tables migration
- [x] Create elo_history table migration

## Phase 3: Backend

- [x] Create ApiResponse wrapper
- [x] Create PageResponse wrapper
- [x] Create DomainException hierarchy
- [x] Create ProblemDetailExceptionHandler
- [x] Create User entity
- [x] Create UserRepository
- [x] Create Question entity
- [x] Create QuestionChoice entity
- [x] Create QuestionRepository
- [x] Create Exam entity
- [x] Create ExamQuestion entity
- [x] Create ExamRepository
- [x] Create Attempt entity
- [x] Create AttemptAnswer entity
- [x] Create AttemptRepository
- [x] Create EloHistory entity
- [x] Create EloHistoryRepository
- [x] Create EloService
- [x] Create Rank mapping from Elo
- [x] Create ExamAiClient interface
- [x] Create StubExamAiClient
- [x] Create Spring AI ChatClient configuration
- [x] Create SpringAiExamAiClient
- [x] Create question classification prompts
- [x] Create grading prompts
- [x] Create similar exercise generation prompts
- [x] Create exam set generation prompts
- [x] Create UserService
- [x] Create QuestionService
- [x] Create ExamService
- [x] Create AttemptService
- [x] Create adaptive practice selection

## Phase 4: Authentication

- [x] Create JwtProperties
- [x] Create JwtService
- [x] Create JwtAuthenticationFilter
- [x] Create SecurityConfig
- [x] Create RefreshToken entity support
- [x] Create AuthService
- [x] Create AuthController

## Phase 5: API

- [x] Implement register API
- [x] Implement login API
- [x] Implement refresh token API
- [x] Implement current user profile API
- [x] Implement upload question API
- [x] Implement list questions API
- [x] Implement get question API
- [x] Implement classify question API
- [x] Implement generate questions API
- [x] Implement create exam API
- [x] Implement create exercise API
- [x] Implement list exams API
- [x] Implement get exam API
- [x] Implement generate exam set API
- [x] Implement generate similar exercises API
- [x] Implement start attempt API
- [x] Implement submit answers API
- [x] Implement grade attempt and update Elo API
- [x] Implement adaptive practice API
- [x] Implement Elo history API

## Phase 6: Testing

- [x] Test EloService rating updates
- [x] Test Rank mapping
- [x] Test AuthController
- [x] Test QuestionController
- [x] Test ExamController
- [x] Test AttemptController
- [x] Test QuestionService validation
- [x] Test AttemptService grading and Elo
- [x] Test adaptive practice selection

## Phase 7: Documentation

- [x] Add README with API overview
- [x] Add environment variable documentation
