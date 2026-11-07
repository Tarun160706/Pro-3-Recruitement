export type Role = 'ROLE_ADMIN' | 'ROLE_RECRUITER' | 'ROLE_CANDIDATE';
export type VerificationStatus = 'EMAIL_PENDING' | 'REVIEW_PENDING' | 'VERIFIED' | 'REJECTED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  phone?: string;
  emailVerified: boolean;
  verificationStatus: VerificationStatus;
  companyName?: string;
  companyWebsite?: string;
  companyAddress?: string;
  designation?: string;
  rejectionReason?: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface CandidateRegisterPayload {
  fullName: string;
  email: string;
  password: string;
  confirmPassword?: string;
  phone?: string;
  currentTitle?: string;
  yearsOfExperience?: number;
  location?: string;
  summary?: string;
}

export interface RecruiterRegisterPayload {
  fullName: string;
  email: string;
  password: string;
  confirmPassword?: string;
  phone: string;
  companyName: string;
  companyWebsite: string;
  companyAddress: string;
  designation: string;
}

export interface MessageResponse {
  message: string;
  verificationStatus?: VerificationStatus;
  emailVerified?: boolean;
  email?: string;
}

export interface Job {
  id: number;
  recruiterId: number;
  recruiterName: string;
  title: string;
  department: string;
  location: string;
  jobType: string;
  experienceMin: number;
  experienceMax: number;
  requiredSkills: string[];
  description: string;
  status: 'ACTIVE' | 'ARCHIVED' | 'DRAFT';
  applicantCount: number;
  createdAt: string;
}

export interface Candidate {
  id: number;
  userId?: number;
  fullName: string;
  email: string;
  phone?: string;
  currentTitle?: string;
  yearsOfExperience: number;
  location?: string;
  summary?: string;
  latestResumeId?: number;
  skills: string[];
  createdAt: string;
}

export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  department: string;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  candidateTitle: string;
  resumeId?: number;
  status: string;
  stage: string;
  recruiterNotes?: string;
  matchScore?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface ResumeAnalysis {
  candidateName?: string;
  email?: string;
  phone?: string;
  currentTitle?: string;
  yearsOfExperience?: number;
  location?: string;
  summary?: string;
  skills: string[];
  experience: Array<{
    company: string;
    role: string;
    duration: string;
    description: string;
  }>;
  education: Array<{
    degree: string;
    institution: string;
    year: string;
  }>;
  projects: Array<{
    title: string;
    techStack: string;
    description: string;
  }>;
  strengths: string[];
  missingInfo: string[];
}

export interface ResumeUploadResponse {
  resumeId: number;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  fileName: string;
  fileSize: number;
  rawText: string;
  analysis: ResumeAnalysis;
  createdAt: string;
}

export interface JobMatchResponse {
  id: number;
  jobId: number;
  jobTitle: string;
  candidateId: number;
  candidateName: string;
  overallScore: number;
  skillsScore: number;
  experienceScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  explanation: string;
  resumeEvidence: string[];
  createdAt: string;
}

export interface InterviewQuestion {
  id?: number;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  category: string;
  question: string;
  expectedAnswer: string;
  followUp?: string;
  candidateAnswer?: string;
  recruiterRating?: number;
  isAiGenerated?: boolean;
}

export interface InterviewSession {
  id: number;
  jobId: number;
  jobTitle: string;
  candidateId: number;
  candidateName: string;
  candidateEmail: string;
  recruiterId: number;
  recruiterName: string;
  title: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  scheduledAt: string;
  overallFeedback?: string;
  competencyScore?: number;
  strengths?: string[];
  improvementAreas?: string[];
  learningRecommendations?: string[];
  questions: InterviewQuestion[];
  createdAt: string;
}

export interface DashboardMetrics {
  totalCandidates: number;
  activeJobs: number;
  interviewsScheduled: number;
  candidatesAwaitingReview: number;
  hiringRate: number;
  pipeline: Array<{ stage: string; count: number }>;
  recentActivity: Array<{
    id: number;
    action: string;
    title: string;
    description: string;
    createdAt: string;
  }>;
}
