import React from 'react';
import { JobMatchResponse } from '../types';
import { X, CheckCircle2, AlertCircle, Quote, Sparkles, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface MatchScoreModalProps {
  match: JobMatchResponse | null;
  onClose: () => void;
}

export const MatchScoreModal: React.FC<MatchScoreModalProps> = ({ match, onClose }) => {
  const navigate = useNavigate();
  if (!match) return null;

  const getScoreClass = (score: number) => {
    if (score >= 80) return 'score-high';
    if (score >= 60) return 'score-mid';
    return 'score-low';
  };

  const handleScheduleInterview = () => {
    onClose();
    navigate('/interviews', { state: { jobId: match.jobId, candidateId: match.candidateId } });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content fade-in" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <span className="badge badge-vermilion">
                <Sparkles size={12} /> AI Job Matching Assessment
              </span>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Explainable Match Engine</span>
            </div>
            <h2 className="editorial-title" style={{ fontSize: '22px' }}>
              {match.candidateName} &mdash; {match.jobTitle}
            </h2>
          </div>
          <button onClick={onClose} className="btn btn-ghost" style={{ padding: '6px' }}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          {/* Top Score Summary Banner */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '20px',
            backgroundColor: 'var(--bg-subtle)',
            borderRadius: 'var(--radius-lg)',
            border: '1px solid var(--border-subtle)',
            marginBottom: '24px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <div className={`score-indicator ${getScoreClass(match.overallScore)}`} style={{ width: '60px', height: '60px', fontSize: '20px' }}>
                {match.overallScore}%
              </div>
              <div>
                <div style={{ fontSize: '16px', fontWeight: 700 }}>Overall Match Fit</div>
                <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Grounded composite evaluation based on role requirements
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '20px', textAlign: 'right' }}>
              <div>
                <div style={{ fontSize: '11px', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700 }}>
                  Skills Match
                </div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-success)' }}>
                  {match.skillsScore}%
                </div>
              </div>
              <div>
                <div style={{ fontSize: '11px', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: 700 }}>
                  Experience
                </div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-info)' }}>
                  {match.experienceScore}%
                </div>
              </div>
            </div>
          </div>

          {/* Explainability Explanation */}
          <div style={{ marginBottom: '24px' }}>
            <h4 style={{ fontSize: '14px', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', color: 'var(--text-muted)', marginBottom: '8px' }}>
              Reasoning & Fit Analysis
            </h4>
            <p style={{ fontSize: '14px', lineHeight: '1.6', color: 'var(--text-secondary)', backgroundColor: '#FAF8F4', padding: '14px 18px', borderRadius: 'var(--radius-md)', borderLeft: '4px solid var(--accent-primary)' }}>
              {match.explanation}
            </p>
          </div>

          {/* Matched vs Missing Skills */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '24px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
                <CheckCircle2 size={16} color="var(--color-success)" />
                <span style={{ fontSize: '13px', fontWeight: 700 }}>Verified Matched Skills ({match.matchedSkills.length})</span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                {match.matchedSkills.length > 0 ? (
                  match.matchedSkills.map((skill, idx) => (
                    <span key={idx} className="skill-tag matched">
                      {skill}
                    </span>
                  ))
                ) : (
                  <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>No direct matches</span>
                )}
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
                <AlertCircle size={16} color="var(--color-danger)" />
                <span style={{ fontSize: '13px', fontWeight: 700 }}>Skill Gaps / Missing ({match.missingSkills.length})</span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                {match.missingSkills.length > 0 ? (
                  match.missingSkills.map((skill, idx) => (
                    <span key={idx} className="skill-tag missing">
                      {skill}
                    </span>
                  ))
                ) : (
                  <span style={{ fontSize: '13px', color: 'var(--color-success)' }}>All core skills fulfilled</span>
                )}
              </div>
            </div>
          </div>

          {/* Grounded Resume Evidence */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
              <Quote size={16} color="var(--accent-primary)" />
              <span style={{ fontSize: '13px', fontWeight: 700 }}>Direct Resume Evidence (Anti-Hallucination Grounding)</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {match.resumeEvidence && match.resumeEvidence.length > 0 ? (
                match.resumeEvidence.map((quote, idx) => (
                  <div
                    key={idx}
                    style={{
                      fontSize: '13px',
                      fontStyle: 'italic',
                      color: 'var(--text-secondary)',
                      padding: '10px 14px',
                      backgroundColor: 'var(--bg-subtle)',
                      borderRadius: 'var(--radius-sm)',
                      borderLeft: '3px solid var(--border-strong)'
                    }}
                  >
                    &ldquo;{quote}&rdquo;
                  </div>
                ))
              ) : (
                <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                  Candidate qualifications match requirements without contradictory citations.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button onClick={onClose} className="btn btn-secondary">
            Close
          </button>
          <button onClick={handleScheduleInterview} className="btn btn-primary">
            <Calendar size={16} />
            <span>Generate Technical Interview</span>
          </button>
        </div>
      </div>
    </div>
  );
};
