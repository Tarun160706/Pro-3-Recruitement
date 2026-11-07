import React, { useEffect, useState } from 'react';
import { candidatePortalApi } from '../api';
import { Candidate, Application, InterviewSession } from '../types';
import {
  User,
  Briefcase,
  Calendar,
  BookOpen,
  CheckCircle2,
  Clock,
  Sparkles,
  Award,
  Layers,
  MapPin,
  Mail
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const CandidatePortalPage: React.FC = () => {
  const [profile, setProfile] = useState<Candidate | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [interviews, setInterviews] = useState<InterviewSession[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [prof, apps, ints] = await Promise.all([
          candidatePortalApi.getMyProfile().catch(() => null),
          candidatePortalApi.getMyApplications().catch(() => []),
          candidatePortalApi.getMyInterviews().catch(() => []),
        ]);
        setProfile(prof);
        setApplications(apps);
        setInterviews(ints);
      } catch (err) {
        console.error('Failed to load candidate portal data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="page-container" style={{ textAlign: 'center', padding: '100px 0' }}>
        <div style={{ fontSize: '15px', color: 'var(--text-secondary)' }}>Loading your candidate workspace...</div>
      </div>
    );
  }

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Candidate Portal & Career Journey</h1>
          <p className="page-subtitle">
            Track your recruitment stages, prepare for technical rounds, and review personalized feedback.
          </p>
        </div>
        <Link to="/jobs" className="btn btn-primary">
          <span>Explore Open Roles</span>
        </Link>
      </div>

      {/* Candidate Profile Overview Card */}
      {profile && (
        <div className="panel" style={{ padding: '24px', marginBottom: '28px' }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '20px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <div className="user-avatar" style={{ width: '56px', height: '56px', fontSize: '20px' }}>
                {profile.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
              </div>
              <div>
                <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text-main)', marginBottom: '4px' }}>
                  {profile.fullName}
                </h2>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <span style={{ fontWeight: 600, color: 'var(--accent-primary)' }}>{profile.currentTitle || 'Software Engineer'}</span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Mail size={13} color="var(--text-muted)" />
                    {profile.email}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <MapPin size={13} color="var(--text-muted)" />
                    {profile.location || 'Remote'}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Briefcase size={13} color="var(--text-muted)" />
                    {profile.yearsOfExperience} Yrs Exp
                  </span>
                </div>
              </div>
            </div>

            <span className="badge badge-success" style={{ padding: '6px 12px' }}>
              <CheckCircle2 size={14} /> Profile Active
            </span>
          </div>

          {profile.summary && (
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '16px', lineHeight: '1.5' }}>
              {profile.summary}
            </p>
          )}

          {profile.skills && profile.skills.length > 0 && (
            <div style={{ marginTop: '16px' }}>
              <div style={{ fontSize: '11px', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '8px' }}>
                Verified Core Competencies
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                {profile.skills.map((s, idx) => (
                  <span key={idx} className="skill-tag">
                    {s}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Grid: Applications & Interviews */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: '28px' }}>
        {/* Left Column: Applications Tracker */}
        <div>
          <div className="panel">
            <div className="panel-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Briefcase size={18} color="var(--accent-primary)" />
                <h3 className="panel-title">Application Status Tracking</h3>
              </div>
              <span className="badge badge-neutral">{applications.length} Active</span>
            </div>
            <div className="panel-body" style={{ padding: '8px 24px' }}>
              {applications.length === 0 ? (
                <div style={{ padding: '36px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                  You have not submitted any applications yet.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  {applications.map((app) => (
                    <div
                      key={app.id}
                      style={{
                        padding: '18px 0',
                        borderBottom: '1px solid var(--border-subtle)',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '8px'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-main)' }}>
                            {app.jobTitle}
                          </div>
                          <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                            {app.department} &bull; Applied {new Date(app.createdAt).toLocaleDateString()}
                          </div>
                        </div>
                        <span className={`badge ${
                          app.status === 'OFFERED' ? 'badge-success' :
                          app.status === 'INTERVIEW_SCHEDULED' ? 'badge-info' :
                          app.status === 'REJECTED' ? 'badge-neutral' : 'badge-warning'
                        }`}>
                          {app.status.replace('_', ' ')}
                        </span>
                      </div>

                      <div style={{
                        padding: '8px 12px',
                        backgroundColor: 'var(--bg-subtle)',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: '12px',
                        color: 'var(--text-secondary)'
                      }}>
                        <strong>Current Stage:</strong> {app.stage}
                        {app.recruiterNotes && (
                          <div style={{ marginTop: '4px', fontStyle: 'italic', color: 'var(--text-muted)' }}>
                            Recruiter Note: &ldquo;{app.recruiterNotes}&rdquo;
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Technical Preparation Resources */}
          <div className="panel">
            <div className="panel-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <BookOpen size={18} color="var(--accent-primary)" />
                <h3 className="panel-title">Interview Preparation Center</h3>
              </div>
            </div>
            <div className="panel-body">
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ padding: '14px', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontWeight: 700, fontSize: '14px', marginBottom: '4px' }}>
                    Spring Boot Architecture & Microservices
                  </div>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                    Review IoC container mechanics, transactional propagation, Bean lifecycle, and Hibernate query optimization (avoiding N+1 selects).
                  </p>
                </div>
                <div style={{ padding: '14px', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontWeight: 700, fontSize: '14px', marginBottom: '4px' }}>
                    Relational Schema & Query Performance
                  </div>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                    Focus on B-tree indexing strategies, EXPLAIN ANALYZE execution plans, and ACID transaction isolation levels in PostgreSQL.
                  </p>
                </div>
                <div style={{ padding: '14px', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontWeight: 700, fontSize: '14px', marginBottom: '4px' }}>
                    Stateless Security & JWT Workflows
                  </div>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                    Be ready to explain token headers, payload claims, cryptographic HMAC signatures, and OncePerRequestFilter chain ordering.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Scheduled Interviews & Feedback */}
        <div>
          <div className="panel">
            <div className="panel-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Calendar size={18} color="var(--accent-primary)" />
                <h3 className="panel-title">Interview Invitations</h3>
              </div>
            </div>
            <div className="panel-body">
              {interviews.length === 0 ? (
                <div style={{ padding: '24px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No interview sessions scheduled yet.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {interviews.map((session) => (
                    <div
                      key={session.id}
                      style={{
                        padding: '16px',
                        backgroundColor: 'var(--bg-subtle)',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border-subtle)'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                        <span className={`badge ${session.status === 'COMPLETED' ? 'badge-success' : 'badge-info'}`}>
                          {session.status}
                        </span>
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                          {new Date(session.scheduledAt).toLocaleDateString()}
                        </span>
                      </div>
                      <div style={{ fontSize: '15px', fontWeight: 700, marginBottom: '2px' }}>
                        {session.title}
                      </div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '12px' }}>
                        Role: {session.jobTitle} &bull; Interviewer: {session.recruiterName}
                      </div>

                      {/* If completed, display AI learning recommendations */}
                      {session.learningRecommendations && session.learningRecommendations.length > 0 && (
                        <div style={{
                          marginTop: '12px',
                          paddingTop: '12px',
                          borderTop: '1px solid var(--border-default)'
                        }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: 700, color: 'var(--accent-primary)', marginBottom: '8px' }}>
                            <Award size={14} />
                            <span>AI-Assisted Growth Recommendations</span>
                          </div>
                          <ul style={{ paddingLeft: '16px', fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                            {session.learningRecommendations.map((rec, idx) => (
                              <li key={idx} style={{ marginBottom: '4px' }}>{rec}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
