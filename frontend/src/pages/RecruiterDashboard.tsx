import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../api';
import { DashboardMetrics } from '../types';
import {
  Users,
  Briefcase,
  Calendar,
  Clock,
  TrendingUp,
  Activity,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const RecruiterDashboard: React.FC = () => {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const data = await dashboardApi.getMetrics();
        setMetrics(data);
      } catch (err: any) {
        setError('Failed to load recruitment intelligence metrics');
      } finally {
        setLoading(false);
      }
    };
    fetchMetrics();
  }, []);

  if (loading) {
    return (
      <div className="page-container" style={{ textAlign: 'center', padding: '100px 0' }}>
        <div style={{ fontSize: '15px', color: 'var(--text-secondary)' }}>Loading recruitment intelligence...</div>
      </div>
    );
  }

  if (error || !metrics) {
    return (
      <div className="page-container">
        <div style={{ padding: '24px', backgroundColor: 'var(--color-danger-bg)', color: 'var(--color-danger)', borderRadius: 'var(--radius-lg)' }}>
          {error || 'Unable to connect to recruitment analytics.'}
        </div>
      </div>
    );
  }

  return (
    <div className="page-container fade-in">
      {/* Editorial Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Executive Recruitment Overview</h1>
          <p className="page-subtitle">
            Autonomous talent pipeline telemetry, grounded AI resume parsing, and interview orchestration.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <Link to="/resumes" className="btn btn-secondary">
            <Sparkles size={16} color="var(--accent-primary)" />
            <span>AI Resume Parser</span>
          </Link>
          <Link to="/jobs" className="btn btn-primary">
            <span>Manage Job Openings</span>
          </Link>
        </div>
      </div>

      {/* 4 Core Metrics Grid */}
      <div className="metrics-grid">
        <div className="metric-card">
          <div className="metric-header">
            <span className="metric-title">Total Candidates</span>
            <div className="metric-icon">
              <Users size={18} />
            </div>
          </div>
          <div className="metric-value">{metrics.totalCandidates}</div>
          <div className="metric-footer">
            <CheckCircle2 size={13} color="var(--color-success)" />
            <span>Profile intelligence indexed</span>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-header">
            <span className="metric-title">Active Openings</span>
            <div className="metric-icon">
              <Briefcase size={18} />
            </div>
          </div>
          <div className="metric-value">{metrics.activeJobs}</div>
          <div className="metric-footer">
            <span style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>Active</span>
            <span>in current hiring cycle</span>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-header">
            <span className="metric-title">Interviews Scheduled</span>
            <div className="metric-icon">
              <Calendar size={18} />
            </div>
          </div>
          <div className="metric-value">{metrics.interviewsScheduled}</div>
          <div className="metric-footer">
            <span style={{ color: 'var(--color-info)', fontWeight: 600 }}>Generated</span>
            <span>with technical rubric</span>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-header">
            <span className="metric-title">Awaiting Review</span>
            <div className="metric-icon">
              <Clock size={18} />
            </div>
          </div>
          <div className="metric-value">{metrics.candidatesAwaitingReview}</div>
          <div className="metric-footer">
            <AlertCircle size={13} color="var(--color-warning)" />
            <span>Requires screening review</span>
          </div>
        </div>
      </div>

      {/* Recruitment Pipeline Kanban Strip */}
      <div className="panel">
        <div className="panel-header">
          <div>
            <h3 className="panel-title">Hiring Pipeline Progression</h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Real-time candidate volume across stage gates
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <TrendingUp size={16} color="var(--color-success)" />
            <span style={{ fontSize: '13px', fontWeight: 600 }}>
              Efficiency Rating: <strong style={{ color: 'var(--accent-primary)' }}>{metrics.hiringRate}%</strong>
            </span>
          </div>
        </div>
        <div className="panel-body">
          <div className="pipeline-container">
            {metrics.pipeline.map((stage, idx) => (
              <div key={idx} className="pipeline-stage">
                <div className="stage-title">
                  <span>{stage.stage}</span>
                  <span className="badge badge-neutral">{stage.count}</span>
                </div>
                <div style={{ height: '6px', backgroundColor: 'var(--border-subtle)', borderRadius: 'var(--radius-full)', overflow: 'hidden', marginTop: '12px' }}>
                  <div
                    style={{
                      height: '100%',
                      width: `${Math.min(100, Math.max(15, stage.count * 25))}%`,
                      backgroundColor: idx === 0 ? 'var(--accent-primary)' : idx === 1 ? 'var(--color-info)' : 'var(--color-success)',
                      borderRadius: 'var(--radius-full)'
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Two Column Grid: Recent Activity & Quick Action Center */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.6fr 1fr', gap: '24px' }}>
        {/* Real-time Activity Log */}
        <div className="panel" style={{ marginBottom: 0 }}>
          <div className="panel-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={18} color="var(--accent-primary)" />
              <h3 className="panel-title">Audit Activity Feed</h3>
            </div>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Auto-logged transactions</span>
          </div>
          <div className="panel-body" style={{ padding: '8px 24px' }}>
            {metrics.recentActivity && metrics.recentActivity.length > 0 ? (
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                {metrics.recentActivity.map((act) => (
                  <div
                    key={act.id}
                    style={{
                      display: 'flex',
                      alignItems: 'flex-start',
                      gap: '14px',
                      padding: '16px 0',
                      borderBottom: '1px solid var(--border-subtle)'
                    }}
                  >
                    <div style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: 'var(--radius-full)',
                      backgroundColor: 'var(--accent-light)',
                      color: 'var(--accent-primary)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                      marginTop: '2px'
                    }}>
                      <Sparkles size={14} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2px' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600 }}>{act.title}</span>
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                          {new Date(act.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                      <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                        {act.description}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ padding: '24px 0', textAlign: 'center', color: 'var(--text-muted)' }}>
                No recent activity recorded yet.
              </div>
            )}
          </div>
        </div>

        {/* Quick Launchpad & Architecture Spotlight */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          <div className="panel" style={{ marginBottom: 0 }}>
            <div className="panel-header">
              <h3 className="panel-title">AI Candidate Intelligence</h3>
            </div>
            <div className="panel-body">
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px', lineHeight: '1.5' }}>
                TalentIQ extracts verifiable competencies directly from PDF resumes, cross-references job requirements without hallucination, and generates tailored technical interview sessions.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <Link to="/resumes" className="btn btn-secondary" style={{ justifyContent: 'space-between' }}>
                  <span>Upload & Analyze PDF</span>
                  <ArrowRight size={14} />
                </Link>
                <Link to="/candidates" className="btn btn-secondary" style={{ justifyContent: 'space-between' }}>
                  <span>Inspect Candidate Profiles</span>
                  <ArrowRight size={14} />
                </Link>
                <Link to="/interviews" className="btn btn-primary" style={{ justifyContent: 'space-between' }}>
                  <span>Generate Technical Questions</span>
                  <ArrowRight size={14} />
                </Link>
              </div>
            </div>
          </div>

          <div style={{
            padding: '20px',
            backgroundColor: 'var(--bg-subtle)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border-subtle)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <span className="badge badge-success">Ethical AI Guardrails</span>
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
              Scoring strictly excludes demographic parameters (gender, age, ethnicity, religion, disability) and requires verbatim resume evidence citations.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
