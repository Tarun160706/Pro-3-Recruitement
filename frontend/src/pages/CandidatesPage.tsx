import React, { useEffect, useState } from 'react';
import { candidateApi, jobApi, matchApi } from '../api';
import { Candidate, Job, JobMatchResponse } from '../types';
import { MatchScoreModal } from '../components/MatchScoreModal';
import {
  Users,
  Search,
  Sparkles,
  Calendar,
  Eye,
  X,
  FileText,
  Mail,
  MapPin,
  Briefcase
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const CandidatesPage: React.FC = () => {
  const [candidates, setCandidates] = useState<Candidate[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [minExp, setMinExp] = useState<number | undefined>(undefined);

  // Match Evaluation State
  const [selectedCandidate, setSelectedCandidate] = useState<Candidate | null>(null);
  const [evalJobId, setEvalJobId] = useState<number | ''>('');
  const [evaluating, setEvaluating] = useState(false);
  const [activeMatch, setActiveMatch] = useState<JobMatchResponse | null>(null);

  // Candidate Profile View Modal
  const [viewingCandidate, setViewingCandidate] = useState<Candidate | null>(null);

  const navigate = useNavigate();

  const fetchCandidates = async () => {
    try {
      setLoading(true);
      const data = await candidateApi.getAll({
        search: search || undefined,
        minExp: minExp !== undefined ? minExp : undefined,
      });
      setCandidates(data);
    } catch (err) {
      console.error('Failed to load candidates:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCandidates();
    jobApi.getAll({ status: 'ACTIVE' }).then(setJobs).catch(console.error);
  }, []);

  const handleEvaluateClick = (candidate: Candidate) => {
    setSelectedCandidate(candidate);
    if (jobs.length > 0) {
      setEvalJobId(jobs[0].id);
    }
  };

  const handleRunEvaluation = async () => {
    if (!selectedCandidate || !evalJobId) return;
    setEvaluating(true);
    try {
      const match = await matchApi.evaluate({
        jobId: Number(evalJobId),
        candidateId: selectedCandidate.id,
      });
      setSelectedCandidate(null);
      setActiveMatch(match);
    } catch (err) {
      alert('Could not complete match evaluation. Ensure candidate has an uploaded resume.');
    } finally {
      setEvaluating(false);
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Candidate Pipeline & Intelligence</h1>
          <p className="page-subtitle">
            Direct profile inspection, structured skills breakdown, and explainable AI job matching.
          </p>
        </div>
        <button onClick={() => navigate('/resumes')} className="btn btn-primary">
          <FileText size={16} />
          <span>Upload New Resume</span>
        </button>
      </div>

      {/* Filter Header */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '16px',
        marginBottom: '24px',
        flexWrap: 'wrap'
      }}>
        <div style={{ position: 'relative', flex: 1, maxWidth: '440px' }}>
          <input
            type="text"
            placeholder="Search candidate by name, title, or skills..."
            className="form-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && fetchCandidates()}
            style={{ paddingLeft: '38px' }}
          />
          <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <select
            className="form-select"
            value={minExp === undefined ? '' : minExp}
            onChange={(e) => setMinExp(e.target.value ? Number(e.target.value) : undefined)}
            style={{ width: '180px' }}
          >
            <option value="">Any Experience</option>
            <option value="2">2+ Years</option>
            <option value="4">4+ Years</option>
            <option value="6">6+ Years</option>
          </select>
          <button onClick={fetchCandidates} className="btn btn-secondary">
            Apply Filters
          </button>
        </div>
      </div>

      {/* Candidate Data Table */}
      <div className="panel" style={{ overflow: 'hidden' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>Candidate Name & Title</th>
              <th>Experience</th>
              <th>Location</th>
              <th>Verified Competencies</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '40px' }}>
                  Loading candidates...
                </td>
              </tr>
            ) : candidates.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '40px' }}>
                  No candidates found matching criteria.
                </td>
              </tr>
            ) : (
              candidates.map((cand) => (
                <tr key={cand.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div className="user-avatar" style={{ width: '38px', height: '38px', fontSize: '13px' }}>
                        {cand.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: '14px' }}>
                          {cand.fullName}
                        </div>
                        <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                          {cand.currentTitle || 'Software Professional'} &bull; {cand.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span style={{ fontWeight: 600 }}>{cand.yearsOfExperience} Yrs</span>
                  </td>
                  <td>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                      {cand.location || 'Remote'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', maxWidth: '360px' }}>
                      {cand.skills && cand.skills.length > 0 ? (
                        cand.skills.slice(0, 4).map((s, idx) => (
                          <span key={idx} className="skill-tag">
                            {s}
                          </span>
                        ))
                      ) : (
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>No skills indexed</span>
                      )}
                      {cand.skills && cand.skills.length > 4 && (
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)', alignSelf: 'center' }}>
                          +{cand.skills.length - 4} more
                        </span>
                      )}
                    </div>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '6px' }}>
                      <button
                        onClick={() => setViewingCandidate(cand)}
                        className="btn btn-ghost btn-sm"
                        title="View Profile"
                      >
                        <Eye size={15} />
                      </button>
                      <button
                        onClick={() => handleEvaluateClick(cand)}
                        className="btn btn-secondary btn-sm"
                        style={{ color: 'var(--accent-primary)', borderColor: 'var(--accent-border)' }}
                      >
                        <Sparkles size={14} />
                        <span>AI Match</span>
                      </button>
                      <button
                        onClick={() => navigate('/interviews', { state: { candidateId: cand.id } })}
                        className="btn btn-primary btn-sm"
                      >
                        <Calendar size={14} />
                        <span>Interview</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Select Job for AI Match Modal */}
      {selectedCandidate && (
        <div className="modal-overlay" onClick={() => setSelectedCandidate(null)}>
          <div className="modal-content fade-in" style={{ maxWidth: '520px' }} onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 className="editorial-title" style={{ fontSize: '20px' }}>Run AI Job Match</h3>
                <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Evaluating: {selectedCandidate.fullName}
                </span>
              </div>
              <button onClick={() => setSelectedCandidate(null)} className="btn btn-ghost" style={{ padding: '6px' }}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Select Target Job Requisition</label>
                <select
                  className="form-select"
                  value={evalJobId}
                  onChange={(e) => setEvalJobId(Number(e.target.value))}
                >
                  {jobs.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.title} &mdash; {j.department}
                    </option>
                  ))}
                </select>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                The AI matching engine parses the candidate's verified resume skills, calculates skill overlap, and computes an explainable fit breakdown with direct resume evidence.
              </p>
            </div>
            <div className="modal-footer">
              <button onClick={() => setSelectedCandidate(null)} className="btn btn-secondary">
                Cancel
              </button>
              <button
                onClick={handleRunEvaluation}
                disabled={evaluating}
                className="btn btn-primary"
              >
                <Sparkles size={14} />
                <span>{evaluating ? 'Analyzing...' : 'Compute Match'}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Detailed Candidate Profile Modal */}
      {viewingCandidate && (
        <div className="modal-overlay" onClick={() => setViewingCandidate(null)}>
          <div className="modal-content fade-in" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div className="user-avatar" style={{ width: '48px', height: '48px', fontSize: '16px' }}>
                  {viewingCandidate.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
                </div>
                <div>
                  <h2 className="editorial-title" style={{ fontSize: '22px' }}>{viewingCandidate.fullName}</h2>
                  <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {viewingCandidate.currentTitle || 'Candidate Profile'}
                  </div>
                </div>
              </div>
              <button onClick={() => setViewingCandidate(null)} className="btn btn-ghost" style={{ padding: '6px' }}>
                <X size={20} />
              </button>
            </div>

            <div className="modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <Mail size={15} color="var(--text-muted)" />
                  <span>{viewingCandidate.email}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <MapPin size={15} color="var(--text-muted)" />
                  <span>{viewingCandidate.location || 'Remote'}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <Briefcase size={15} color="var(--text-muted)" />
                  <span>{viewingCandidate.yearsOfExperience} Years Experience</span>
                </div>
              </div>

              <div style={{ marginBottom: '20px' }}>
                <h4 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '8px' }}>
                  Executive Biography
                </h4>
                <p style={{ fontSize: '14px', lineHeight: '1.6', color: 'var(--text-secondary)' }}>
                  {viewingCandidate.summary || 'No bio provided.'}
                </p>
              </div>

              <div>
                <h4 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '8px' }}>
                  Extracted Competencies ({viewingCandidate.skills ? viewingCandidate.skills.length : 0})
                </h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {viewingCandidate.skills && viewingCandidate.skills.map((skill, idx) => (
                    <span key={idx} className="skill-tag">
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button onClick={() => setViewingCandidate(null)} className="btn btn-secondary">
                Close
              </button>
              <button
                onClick={() => {
                  const cand = viewingCandidate;
                  setViewingCandidate(null);
                  handleEvaluateClick(cand);
                }}
                className="btn btn-primary"
              >
                <Sparkles size={14} />
                <span>Evaluate Job Fit</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Explainable AI Match Modal */}
      {activeMatch && (
        <MatchScoreModal
          match={activeMatch}
          onClose={() => setActiveMatch(null)}
        />
      )}
    </div>
  );
};
