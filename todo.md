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
- [ ] Create elo_history table migration

## Phase 3: Backend

- [ ] Create ApiResponse wrapper
- [ ] Create PageResponse wrapper
- [ ] Create DomainException hierarchy
- [ ] Create ProblemDetailExceptionHandler
- [ ] Create User entity
- [ ] Create UserRepository
- [ ] Create Question entity
- [ ] Create QuestionChoice entity
- [ ] Create QuestionRepository
- [ ] Create Exam entity
- [ ] Create ExamQuestion entity
- [ ] Create ExamRepository
- [ ] Create Attempt entity
- [ ] Create AttemptAnswer entity
- [ ] Create AttemptRepository
- [ ] Create EloHistory entity
- [ ] Create EloHistoryRepository
- [ ] Create EloService
- [ ] Create Rank mapping from Elo
- [ ] Create ExamAiClient interface
- [ ] Create StubExamAiClient
- [ ] Create Spring AI ChatClient configuration
- [ ] Create SpringAiExamAiClient
- [ ] Create question classification prompts
- [ ] Create grading prompts
- [ ] Create similar exercise generation prompts
- [ ] Create exam set generation prompts
- [ ] Create UserService
- [ ] Create QuestionService
- [ ] Create ExamService
- [ ] Create AttemptService
- [ ] Create adaptive practice selection

## Phase 4: Authentication

- [ ] Create JwtProperties
- [ ] Create JwtService
- [ ] Create JwtAuthenticationFilter
- [ ] Create SecurityConfig
- [ ] Create RefreshToken entity support
- [ ] Create AuthService
- [ ] Create AuthController

## Phase 5: API

- [ ] Implement register API
- [ ] Implement login API
- [ ] Implement refresh token API
- [ ] Implement current user profile API
- [ ] Implement upload question API
- [ ] Implement list questions API
- [ ] Implement get question API
- [ ] Implement classify question API
- [ ] Implement generate questions API
- [ ] Implement create exam API
- [ ] Implement create exercise API
- [ ] Implement list exams API
- [ ] Implement get exam API
- [ ] Implement generate exam set API
- [ ] Implement generate similar exercises API
- [ ] Implement start attempt API
- [ ] Implement submit answers API
- [ ] Implement grade attempt and update Elo API
- [ ] Implement adaptive practice API
- [ ] Implement Elo history API

## Phase 6: Testing

- [ ] Test EloService rating updates
- [ ] Test Rank mapping
- [ ] Test AuthController
- [ ] Test QuestionController
- [ ] Test ExamController
- [ ] Test AttemptController
- [ ] Test QuestionService validation
- [ ] Test AttemptService grading and Elo
- [ ] Test adaptive practice selection

## Phase 7: Documentation

- [ ] Add README with API overview
- [ ] Add environment variable documentation
