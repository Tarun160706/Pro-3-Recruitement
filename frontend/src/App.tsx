import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { Login } from './pages/Login';
import { CandidateRegister } from './pages/CandidateRegister';
import { RecruiterRegister } from './pages/RecruiterRegister';
import { VerifyEmail } from './pages/VerifyEmail';
import { VerifyEmailNotice } from './pages/VerifyEmailNotice';
import { RecruiterPendingReview } from './pages/RecruiterPendingReview';
import { RecruiterRejected } from './pages/RecruiterRejected';
import { AdminRecruitersPage } from './pages/AdminRecruitersPage';
import { RecruiterDashboard } from './pages/RecruiterDashboard';
import { JobsPage } from './pages/JobsPage';
import { CandidatesPage } from './pages/CandidatesPage';
import { ResumeAnalyzerPage } from './pages/ResumeAnalyzerPage';
import { InterviewLabPage } from './pages/InterviewLabPage';
import { CandidatePortalPage } from './pages/CandidatePortalPage';

const ProtectedLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        backgroundColor: 'var(--bg-primary)',
        color: 'var(--text-secondary)'
      }}>
        Initializing TalentIQ Workspace...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Handle unverified / pending recruiter status
  if (user.role === 'ROLE_RECRUITER') {
    if (user.verificationStatus === 'EMAIL_PENDING') {
      return <Navigate to={`/verify-email-notice?email=${encodeURIComponent(user.email)}&role=recruiter`} replace />;
    }
    if (user.verificationStatus === 'REVIEW_PENDING') {
      return <Navigate to="/recruiter/pending-review" replace />;
    }
    if (user.verificationStatus === 'REJECTED') {
      return <Navigate to="/recruiter/rejected" replace />;
    }
  }

  // Handle unverified candidate status
  if (user.role === 'ROLE_CANDIDATE' && !user.emailVerified) {
    return <Navigate to={`/verify-email-notice?email=${encodeURIComponent(user.email)}&role=candidate`} replace />;
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <TopBar />
        <main>{children}</main>
      </div>
    </div>
  );
};

const RecruiterRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isRecruiter, isAdmin, isCandidate } = useAuth();
  if (isCandidate) {
    return <Navigate to="/portal" replace />;
  }
  if (!isRecruiter && !isAdmin) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const CandidateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isCandidate, isRecruiter, isAdmin } = useAuth();
  if (isRecruiter || isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }
  if (!isCandidate) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAdmin } = useAuth();
  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

const RootRedirect: React.FC = () => {
  const { user, isLoading, isRecruiter, isAdmin } = useAuth();
  if (isLoading) return null;
  if (!user) return <Navigate to="/login" replace />;

  if (user.role === 'ROLE_ADMIN') return <Navigate to="/dashboard" replace />;

  if (user.role === 'ROLE_RECRUITER') {
    if (user.verificationStatus === 'VERIFIED') return <Navigate to="/dashboard" replace />;
    if (user.verificationStatus === 'REVIEW_PENDING') return <Navigate to="/recruiter/pending-review" replace />;
    if (user.verificationStatus === 'REJECTED') return <Navigate to="/recruiter/rejected" replace />;
    return <Navigate to={`/verify-email-notice?email=${encodeURIComponent(user.email)}&role=recruiter`} replace />;
  }

  if (user.role === 'ROLE_CANDIDATE') {
    if (user.emailVerified) return <Navigate to="/portal" replace />;
    return <Navigate to={`/verify-email-notice?email=${encodeURIComponent(user.email)}&role=candidate`} replace />;
  }

  return <Navigate to="/login" replace />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Navigate to="/register/candidate" replace />} />
          <Route path="/register/candidate" element={<CandidateRegister />} />
          <Route path="/register/recruiter" element={<RecruiterRegister />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/verify-email-notice" element={<VerifyEmailNotice />} />
          <Route path="/recruiter/pending-review" element={<RecruiterPendingReview />} />
          <Route path="/recruiter/rejected" element={<RecruiterRejected />} />

          {/* Protected Recruiter Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedLayout>
                <RecruiterRoute>
                  <RecruiterDashboard />
                </RecruiterRoute>
              </ProtectedLayout>
            }
          />
          <Route
            path="/candidates"
            element={
              <ProtectedLayout>
                <RecruiterRoute>
                  <CandidatesPage />
                </RecruiterRoute>
              </ProtectedLayout>
            }
          />
          <Route
            path="/resumes"
            element={
              <ProtectedLayout>
                <RecruiterRoute>
                  <ResumeAnalyzerPage />
                </RecruiterRoute>
              </ProtectedLayout>
            }
          />
          <Route
            path="/interviews"
            element={
              <ProtectedLayout>
                <RecruiterRoute>
                  <InterviewLabPage />
                </RecruiterRoute>
              </ProtectedLayout>
            }
          />

          {/* Protected Admin Routes */}
          <Route
            path="/admin/recruiters"
            element={
              <ProtectedLayout>
                <AdminRoute>
                  <AdminRecruitersPage />
                </AdminRoute>
              </ProtectedLayout>
            }
          />

          {/* Protected Shared Route: Jobs */}
          <Route
            path="/jobs"
            element={
              <ProtectedLayout>
                <JobsPage />
              </ProtectedLayout>
            }
          />

          {/* Protected Candidate Route */}
          <Route
            path="/portal"
            element={
              <ProtectedLayout>
                <CandidateRoute>
                  <CandidatePortalPage />
                </CandidateRoute>
              </ProtectedLayout>
            }
          />

          {/* Fallback Root Redirect */}
          <Route path="*" element={<RootRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
