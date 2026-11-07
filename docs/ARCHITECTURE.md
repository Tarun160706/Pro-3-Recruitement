# TalentIQ — Architecture & System Design

## 1. Executive Overview
TalentIQ is an AI-powered recruitment and interview intelligence platform built to eliminate recruiter burnout, objectively parse candidate competencies, compute explainable job matches, and generate tailored technical interview assessments grounded strictly in candidate evidence.

## 2. System Architecture
The platform is designed following a decoupled, layered micro-architecture:

```
+-------------------------------------------------------------+
|                      React + TypeScript Frontend            |
|     (Warm Ivory & Vermilion UI, Vite, React Router, Axios)  |
+------------------------------+------------------------------+
                               |
                        HTTPS / JSON REST APIs
                               |
+------------------------------v------------------------------+
|                   Spring Boot 3.2.x Backend                 |
|  +-------------------------------------------------------+  |
|  | Security & Auth Layer (Spring Security 6, JJWT, RBAC) |  |
|  +-------------------------------------------------------+  |
|  | Web Controllers (Auth, Jobs, Candidates, Resumes, etc)|  |
|  +-------------------------------------------------------+  |
|  | Business Service Layer:                               |  |
|  | - ResumeParserService (Apache PDFBox text extraction)  |  |
|  | - GeminiAiService (REST Structured JSON extraction)   |  |
|  | - JobMatchService (Explainable scoring algorithm)     |  |
|  | - InterviewService (Targeted technical assessments)   |  |
|  +-------------------------------------------------------+  |
|  | Data Persistence Layer (Spring Data JPA, Hibernate)   |  |
|  +-------------------------------------------------------+  |
+--------------------+---------------------+------------------+
                     |                     |
            PostgreSQL 18 Database     Gemini 1.5 Flash API
```

## 3. Key Design Decisions for Interview Demonstration

### Low Footprint (8GB RAM Optimization)
- **Cloud-driven AI Inference**: Uses Google Gemini 1.5 Flash REST endpoints instead of running local LLM weights (Ollama/PyTorch), conserving RAM and GPU resources.
- **Stateless Authentication**: JWT tokens with 24-hour expiration eliminate session storage bloat on the server.
- **Vite Build Tooling**: Fast HMR and lightweight developer server.

### AI Grounding & Anti-Hallucination Guardrails
- **Zero-Speculation Prompting**: Strict JSON schemas with explicit instruction to only extract factual data explicitly present in the candidate's resume text.
- **Explainable Matching**: Job match scores are broken down into:
  - Direct Skill Matches
  - Skill Gaps (Missing Skills)
  - Experience Alignment
  - Resume Evidence Citations
- **Ethical Safeguards**: Candidate scoring excludes any demographic or protected characteristics (gender, age, ethnicity, religion, disability).

### Role-Based Access Control (RBAC)
- **ROLE_RECRUITER**: Full access to job creation, candidate shortlisting, pipeline status changes, AI interview question generation, and candidate notes.
- **ROLE_CANDIDATE**: Portal access to view submitted applications, review interview invitations, access prep resources, and inspect shared feedback.
