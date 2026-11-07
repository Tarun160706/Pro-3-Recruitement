# TalentIQ — REST API Specification

All protected endpoints require the HTTP Header:
`Authorization: Bearer <JWT_TOKEN>`

## 1. Authentication (`/api/auth`)
- `POST /api/auth/register` (Public)
  - Request: `{ "email": "...", "password": "...", "fullName": "...", "role": "RECRUITER" | "CANDIDATE", "phone": "..." }`
  - Response: `{ "token": "...", "type": "Bearer", "user": { "id": 1, "email": "...", "fullName": "...", "role": "..." } }`
- `POST /api/auth/login` (Public)
  - Request: `{ "email": "...", "password": "..." }`
  - Response: `{ "token": "...", "type": "Bearer", "user": { ... } }`
- `GET /api/auth/me` (Protected)
  - Response: `{ "id": 1, "email": "...", "fullName": "...", "role": "..." }`

## 2. Recruiter Dashboard (`/api/dashboard`)
- `GET /api/dashboard/metrics` (RECRUITER)
  - Response: `{ "totalCandidates": 45, "activeJobs": 6, "interviewsScheduled": 8, "candidatesAwaitingReview": 12, "hiringRate": 85 }`
- `GET /api/dashboard/pipeline` (RECRUITER)
  - Response: `[ { "stage": "APPLIED", "count": 20 }, { "stage": "SCREENED", "count": 12 }, ... ]`
- `GET /api/dashboard/recent-activity` (RECRUITER)
  - Response: `[ { "id": 1, "action": "RESUME_ANALYZED", "description": "AI analyzed John Doe for Senior Backend", "timestamp": "..." } ]`

## 3. Job Management (`/api/jobs`)
- `GET /api/jobs` (Public / Authenticated)
  - Query Params: `?search=java&status=ACTIVE&department=Engineering`
- `GET /api/jobs/{id}` (Public / Authenticated)
- `POST /api/jobs` (RECRUITER)
  - Request: `{ "title": "...", "department": "...", "location": "...", "jobType": "FULL_TIME", "experienceMin": 3, "experienceMax": 7, "requiredSkills": ["Java", "Spring Boot", "PostgreSQL", "Docker"], "description": "..." }`
- `PUT /api/jobs/{id}` (RECRUITER)
- `PATCH /api/jobs/{id}/status` (RECRUITER) - Change status to ACTIVE / ARCHIVED / DRAFT
- `DELETE /api/jobs/{id}` (RECRUITER)

## 4. Candidate Management (`/api/candidates`)
- `GET /api/candidates` (RECRUITER)
  - Query Params: `?search=john&skill=Java&minExp=2`
- `GET /api/candidates/{id}` (RECRUITER)
- `POST /api/candidates` (RECRUITER) - Manually create or enroll candidate
- `GET /api/candidates/{id}/applications` (RECRUITER)
- `PATCH /api/candidates/{id}/applications/{appId}/status` (RECRUITER)
  - Request: `{ "status": "SHORTLISTED", "stage": "Technical Round", "notes": "Strong system design" }`

## 5. Resume Intelligence & AI Parsing (`/api/resumes`)
- `POST /api/resumes/upload` (RECRUITER / CANDIDATE)
  - Form Data: `file: MultipartFile (PDF)`, `candidateId: optional Long`, `jobId: optional Long`
  - Extraction Process:
    1. Apache PDFBox extracts raw textual stream.
    2. Gemini AI structured prompt extracts skills, experience, education, projects, strengths, and missing items.
    3. Grounding validation checks that extracted claims match raw text.
  - Response: `{ "resumeId": 12, "candidateId": 5, "rawText": "...", "analysis": { "skills": [...], "experience": [...], "education": [...], "projects": [...], "strengths": [...], "missingInfo": [...] } }`
- `GET /api/resumes/{id}` (RECRUITER / CANDIDATE)

## 6. AI Job Matching (`/api/matches`)
- `POST /api/matches/evaluate` (RECRUITER)
  - Request: `{ "jobId": 1, "candidateId": 5, "resumeId": 12 }`
  - Response:
    ```json
    {
      "matchId": 7,
      "overallScore": 88,
      "skillsScore": 90,
      "experienceScore": 85,
      "matchedSkills": ["Java", "Spring Boot", "PostgreSQL", "REST APIs"],
      "missingSkills": ["Kubernetes", "Kafka"],
      "explanation": "Candidate demonstrates 5+ years building scalable microservices in Spring Boot with PostgreSQL. Exceeds core backend requirements; lacks distributed streaming experience in Kafka.",
      "resumeEvidence": [
        "Architected Spring Boot microservices serving 2M daily requests",
        "Optimized PostgreSQL complex queries reducing latency by 40%"
      ]
    }
    ```
- `GET /api/matches/job/{jobId}` (RECRUITER) - Get all candidate match scores for a job
- `GET /api/matches/{id}` (RECRUITER)

## 7. AI Interview Generation & Feedback (`/api/interviews`)
- `POST /api/interviews/generate` (RECRUITER)
  - Request: `{ "jobId": 1, "candidateId": 5, "title": "Technical Assessment Round 1", "scheduledAt": "2026-11-05T10:00:00" }`
  - Response: Generates tailored questions (Beginner, Intermediate, Advanced) with expected answer criteria & follow-ups grounded in candidate's project history.
- `GET /api/interviews/{id}` (RECRUITER / CANDIDATE)
- `PUT /api/interviews/questions/{questionId}` (RECRUITER) - Edit question text or expected answer
- `POST /api/interviews/{id}/feedback` (RECRUITER)
  - Request: `{ "overallFeedback": "...", "competencyScore": 82, "notes": "..." }`
  - Backend AI Assistant: Enriches feedback with categorized strengths, improvement areas, and personalized learning roadmap.
  - Response: Saved interview session with AI-assisted learning recommendations clearly labeled.

## 8. Candidate Portal (`/api/candidate`)
- `GET /api/candidate/me/profile` (CANDIDATE)
- `GET /api/candidate/me/applications` (CANDIDATE)
- `GET /api/candidate/me/interviews` (CANDIDATE)
- `GET /api/candidate/me/feedback/{interviewId}` (CANDIDATE)
