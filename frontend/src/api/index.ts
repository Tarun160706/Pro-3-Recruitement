import { apiClient } from './client';
import {
  AuthResponse,
  Candidate,
  CandidateRegisterPayload,
  RecruiterRegisterPayload,
  MessageResponse,
  DashboardMetrics,
  InterviewQuestion,
  InterviewSession,
  Job,
  JobMatchResponse,
  ResumeUploadResponse,
  User,
  Application
} from '../types';

export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/login', { email, password });
    return res.data;
  },
  registerCandidate: async (payload: CandidateRegisterPayload): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>('/auth/register/candidate', payload);
    return res.data;
  },
  registerRecruiter: async (payload: RecruiterRegisterPayload): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>('/auth/register/recruiter', payload);
    return res.data;
  },
  verifyEmail: async (token: string): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>('/auth/verify-email', { token });
    return res.data;
  },
  resendVerification: async (email: string): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>('/auth/resend-verification', { email });
    return res.data;
  },
  logout: async (): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>('/auth/logout');
    return res.data;
  },
  getCurrentUser: async (): Promise<User> => {
    const res = await apiClient.get<User>('/auth/me');
    return res.data;
  },
};

export const adminApi = {
  getRecruiters: async (status?: string): Promise<User[]> => {
    const res = await apiClient.get<User[]>('/admin/recruiters', { params: status ? { status } : {} });
    return res.data;
  },
  getRecruiterById: async (id: number): Promise<User> => {
    const res = await apiClient.get<User>(`/admin/recruiters/${id}`);
    return res.data;
  },
  approveRecruiter: async (id: number): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>(`/admin/recruiters/${id}/approve`);
    return res.data;
  },
  rejectRecruiter: async (id: number, reason: string): Promise<MessageResponse> => {
    const res = await apiClient.post<MessageResponse>(`/admin/recruiters/${id}/reject`, { reason });
    return res.data;
  },
};

export const dashboardApi = {
  getMetrics: async (): Promise<DashboardMetrics> => {
    const res = await apiClient.get<DashboardMetrics>('/dashboard/metrics');
    return res.data;
  },
};

export const jobApi = {
  getAll: async (params?: { status?: string; department?: string; search?: string }): Promise<Job[]> => {
    const res = await apiClient.get<Job[]>('/jobs', { params });
    return res.data;
  },
  getById: async (id: number): Promise<Job> => {
    const res = await apiClient.get<Job>(`/jobs/${id}`);
    return res.data;
  },
  create: async (job: Partial<Job>): Promise<Job> => {
    const res = await apiClient.post<Job>('/jobs', job);
    return res.data;
  },
  update: async (id: number, job: Partial<Job>): Promise<Job> => {
    const res = await apiClient.put<Job>(`/jobs/${id}`, job);
    return res.data;
  },
  updateStatus: async (id: number, status: string): Promise<Job> => {
    const res = await apiClient.patch<Job>(`/jobs/${id}/status`, null, { params: { status } });
    return res.data;
  },
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/jobs/${id}`);
  },
};

export const candidateApi = {
  getAll: async (params?: { search?: string; minExp?: number }): Promise<Candidate[]> => {
    const res = await apiClient.get<Candidate[]>('/candidates', { params });
    return res.data;
  },
  getById: async (id: number): Promise<Candidate> => {
    const res = await apiClient.get<Candidate>(`/candidates/${id}`);
    return res.data;
  },
  create: async (candidate: Partial<Candidate>): Promise<Candidate> => {
    const res = await apiClient.post<Candidate>('/candidates', candidate);
    return res.data;
  },
  getApplications: async (candidateId: number): Promise<Application[]> => {
    const res = await apiClient.get<Application[]>(`/candidates/${candidateId}/applications`);
    return res.data;
  },
  updateApplicationStatus: async (candidateId: number, appId: number, payload: { status?: string; stage?: string; recruiterNotes?: string }): Promise<Application> => {
    const res = await apiClient.patch<Application>(`/candidates/${candidateId}/applications/${appId}/status`, payload);
    return res.data;
  },
};

export const resumeApi = {
  upload: async (file: File, candidateId?: number, jobId?: number): Promise<ResumeUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    if (candidateId) formData.append('candidateId', candidateId.toString());
    if (jobId) formData.append('jobId', jobId.toString());

    const res = await apiClient.post<ResumeUploadResponse>('/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },
  getById: async (id: number): Promise<ResumeUploadResponse> => {
    const res = await apiClient.get<ResumeUploadResponse>(`/resumes/${id}`);
    return res.data;
  },
};

export const matchApi = {
  evaluate: async (payload: { jobId: number; candidateId: number; resumeId?: number }): Promise<JobMatchResponse> => {
    const res = await apiClient.post<JobMatchResponse>('/matches/evaluate', payload);
    return res.data;
  },
  getById: async (id: number): Promise<JobMatchResponse> => {
    const res = await apiClient.get<JobMatchResponse>(`/matches/${id}`);
    return res.data;
  },
  getByJob: async (jobId: number): Promise<JobMatchResponse[]> => {
    const res = await apiClient.get<JobMatchResponse[]>(`/matches/job/${jobId}`);
    return res.data;
  },
};

export const interviewApi = {
  generate: async (payload: { jobId: number; candidateId: number; title?: string; scheduledAt?: string }): Promise<InterviewSession> => {
    const res = await apiClient.post<InterviewSession>('/interviews/generate', payload);
    return res.data;
  },
  getById: async (id: number): Promise<InterviewSession> => {
    const res = await apiClient.get<InterviewSession>(`/interviews/${id}`);
    return res.data;
  },
  getByJob: async (jobId: number): Promise<InterviewSession[]> => {
    const res = await apiClient.get<InterviewSession[]>(`/interviews/job/${jobId}`);
    return res.data;
  },
  getByCandidate: async (candidateId: number): Promise<InterviewSession[]> => {
    const res = await apiClient.get<InterviewSession[]>(`/interviews/candidate/${candidateId}`);
    return res.data;
  },
  updateQuestion: async (questionId: number, question: Partial<InterviewQuestion>): Promise<InterviewQuestion> => {
    const res = await apiClient.put<InterviewQuestion>(`/interviews/questions/${questionId}`, question);
    return res.data;
  },
  submitFeedback: async (id: number, payload: {
    overallFeedback: string;
    competencyScore: number;
    strengths?: string[];
    improvementAreas?: string[];
    learningRecommendations?: string[];
    questionRatings?: Record<number, number>;
    candidateAnswers?: Record<number, string>;
  }): Promise<InterviewSession> => {
    const res = await apiClient.post<InterviewSession>(`/interviews/${id}/feedback`, payload);
    return res.data;
  },
};

export const candidatePortalApi = {
  getMyProfile: async (): Promise<Candidate> => {
    const res = await apiClient.get<Candidate>('/candidate/me/profile');
    return res.data;
  },
  getMyApplications: async (): Promise<Application[]> => {
    const res = await apiClient.get<Application[]>('/candidate/me/applications');
    return res.data;
  },
  getMyInterviews: async (): Promise<InterviewSession[]> => {
    const res = await apiClient.get<InterviewSession[]>('/candidate/me/interviews');
    return res.data;
  },
};
