import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { interviewApi, jobApi, candidateApi } from '../api';
import { Job, Candidate, InterviewSession, InterviewQuestion } from '../types';
import {
  Sparkles,
  Calendar,
  CheckCircle2,
  Clock,
  Edit2,
  Save,
  Award,
  BookOpen,
  ArrowRight,
  User,
  Star
} from 'lucide-react';

export const InterviewLabPage: React.FC = () => {
  const location = useLocation();
  const routeState = location.state as { candidateId?: number; jobId?: number } | null;

  const [jobs, setJobs] = useState<Job[]>([]);
  const [candidates, setCandidates] = useState<Candidate[]>([]);
  const [selectedCandidateId, setSelectedCandidateId] = useState<number | ''>(routeState?.candidateId || '');
  const [selectedJobId, setSelectedJobId] = useState<number | ''>(routeState?.jobId || '');
  const [sessionTitle, setSessionTitle] = useState('Technical Competency Round');

  const [generating, setGenerating] = useState(false);
  const [activeSession, setActiveSession] = useState<InterviewSession | null>(null);
  const [difficultyFilter, setDifficultyFilter] = useState<'ALL' | 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'>('ALL');

  // Question Edit State
  const [editingQuestionId, setEditingQuestionId] = useState<number | null>(null);
  const [editPrompt, setEditPrompt] = useState('');
  const [editExpected, setEditExpected] = useState('');
  const [editFollowUp, setEditFollowUp] = useState('');

  // Feedback State
  const [feedbackNotes, setFeedbackNotes] = useState('');
  const [competencyScore, setCompetencyScore] = useState(85);
  const [submittingFeedback, setSubmittingFeedback] = useState(false);

  useEffect(() => {
    jobApi.getAll({ status: 'ACTIVE' }).then((jList) => {
      setJobs(jList);
      if (!selectedJobId && jList.length > 0) {
        setSelectedJobId(jList[0].id);
      }
    }).catch(console.error);

    candidateApi.getAll().then((cList) => {
      setCandidates(cList);
      if (!selectedCandidateId && cList.length > 0) {
        setSelectedCandidateId(cList[0].id);
      }
    }).catch(console.error);

    // If preselected candidate has an existing interview session, load it
    if (routeState?.candidateId) {
      interviewApi.getByCandidate(routeState.candidateId).then((sessions) => {
        if (sessions && sessions.length > 0) {
          setActiveSession(sessions[0]);
        }
      }).catch(console.error);
    }
  }, []);

  const handleGenerateInterview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCandidateId || !selectedJobId) return;

    setGenerating(true);
    try {
      const session = await interviewApi.generate({
        candidateId: Number(selectedCandidateId),
        jobId: Number(selectedJobId),
        title: sessionTitle,
      });
      setActiveSession(session);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to generate tailored interview questions.');
    } finally {
      setGenerating(false);
    }
  };

  const handleStartEdit = (q: InterviewQuestion) => {
    if (!q.id) return;
    setEditingQuestionId(q.id);
    setEditPrompt(q.question);
    setEditExpected(q.expectedAnswer);
    setEditFollowUp(q.followUp || '');
  };

  const handleSaveEdit = async (questionId: number) => {
    try {
      const updated = await interviewApi.updateQuestion(questionId, {
        question: editPrompt,
        expectedAnswer: editExpected,
        followUp: editFollowUp,
      });

      if (activeSession) {
        const updatedQuestions = activeSession.questions.map((q) =>
          q.id === questionId ? { ...q, ...updated } : q
        );
        setActiveSession({ ...activeSession, questions: updatedQuestions });
      }
      setEditingQuestionId(null);
    } catch (err) {
      alert('Failed to save question edit.');
    }
  };

  const handleSubmitFeedback = async () => {
    if (!activeSession) return;
    setSubmittingFeedback(true);
    try {
      const updated = await interviewApi.submitFeedback(activeSession.id, {
        overallFeedback: feedbackNotes,
        competencyScore,
      });
      setActiveSession(updated);
      alert('Interview feedback and AI-assisted recommendations saved successfully!');
    } catch (err) {
      alert('Failed to submit feedback.');
    } finally {
      setSubmittingFeedback(false);
    }
  };

  const filteredQuestions = activeSession?.questions.filter((q) => {
    if (difficultyFilter === 'ALL') return true;
    return q.difficulty === difficultyFilter;
  }) || [];

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Technical Interview Orchestration</h1>
          <p className="page-subtitle">
            Generate tailored technical questions grounded in candidate experience and role prerequisites.
          </p>
        </div>
      </div>

      {/* Generation Config Panel */}
      <div className="panel" style={{ marginBottom: '28px' }}>
        <div className="panel-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Sparkles size={18} color="var(--accent-primary)" />
            <h3 className="panel-title">Session Configuration</h3>
          </div>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            Gemini 1.5 Flash Technical Rubric Generator
          </span>
        </div>
        <div className="panel-body">
          <form onSubmit={handleGenerateInterview} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr)) 180px', gap: '16px', alignItems: 'end' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Candidate</label>
              <select
                className="form-select"
                value={selectedCandidateId}
                onChange={(e) => setSelectedCandidateId(Number(e.target.value))}
              >
                {candidates.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.fullName} ({c.currentTitle || 'Candidate'})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Target Job Opening</label>
              <select
                className="form-select"
                value={selectedJobId}
                onChange={(e) => setSelectedJobId(Number(e.target.value))}
              >
                {jobs.map((j) => (
                  <option key={j.id} value={j.id}>
                    {j.title} &mdash; {j.department}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Assessment Title</label>
              <input
                type="text"
                className="form-input"
                value={sessionTitle}
                onChange={(e) => setSessionTitle(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={generating}
              className="btn btn-primary"
              style={{ height: '42px' }}
            >
              <Sparkles size={16} />
              <span>{generating ? 'Generating...' : 'Generate Rubric'}</span>
            </button>
          </form>
        </div>
      </div>

      {/* Active Interview Session Review */}
      {activeSession ? (
        <div className="fade-in">
          {/* Session Header Card */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '20px 24px',
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-lg)',
            marginBottom: '24px',
            boxShadow: 'var(--shadow-xs)',
            flexWrap: 'wrap',
            gap: '16px'
          }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                <span className={`badge ${activeSession.status === 'COMPLETED' ? 'badge-success' : 'badge-info'}`}>
                  {activeSession.status}
                </span>
                <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                  Candidate: <strong style={{ color: 'var(--text-main)' }}>{activeSession.candidateName}</strong> ({activeSession.candidateEmail})
                </span>
              </div>
              <h2 className="editorial-title" style={{ fontSize: '22px' }}>
                {activeSession.title} &mdash; {activeSession.jobTitle}
              </h2>
            </div>

            {/* Filter Tabs by Difficulty */}
            <div style={{ display: 'flex', gap: '6px' }}>
              {(['ALL', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'] as const).map((diff) => (
                <button
                  key={diff}
                  onClick={() => setDifficultyFilter(diff)}
                  className={`btn btn-sm ${difficultyFilter === diff ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ textTransform: 'capitalize' }}
                >
                  {diff.toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          {/* Question List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '32px' }}>
            {filteredQuestions.map((q, idx) => (
              <div key={q.id || idx} className="panel" style={{ marginBottom: 0, padding: '22px 24px' }}>
                {editingQuestionId === q.id ? (
                  /* Edit Form */
                  <div>
                    <div className="form-group">
                      <label className="form-label">Question Text</label>
                      <textarea
                        className="form-textarea"
                        rows={3}
                        value={editPrompt}
                        onChange={(e) => setEditPrompt(e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Expected Concepts to Evaluate</label>
                      <textarea
                        className="form-textarea"
                        rows={2}
                        value={editExpected}
                        onChange={(e) => setEditExpected(e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Probing Follow-Up</label>
                      <input
                        type="text"
                        className="form-input"
                        value={editFollowUp}
                        onChange={(e) => setEditFollowUp(e.target.value)}
                      />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                      <button onClick={() => setEditingQuestionId(null)} className="btn btn-secondary btn-sm">
                        Cancel
                      </button>
                      <button onClick={() => q.id && handleSaveEdit(q.id)} className="btn btn-primary btn-sm">
                        <Save size={14} />
                        <span>Save Changes</span>
                      </button>
                    </div>
                  </div>
                ) : (
                  /* Question Display */
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span className={`diff-${q.difficulty.toLowerCase()}`}>
                          {q.difficulty}
                        </span>
                        <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                          {q.category}
                        </span>
                      </div>
                      <button
                        onClick={() => handleStartEdit(q)}
                        className="btn btn-ghost btn-sm"
                        style={{ color: 'var(--text-muted)' }}
                      >
                        <Edit2 size={13} />
                        <span>Edit Question</span>
                      </button>
                    </div>

                    <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-main)', marginBottom: '12px', lineHeight: '1.4' }}>
                      {q.question}
                    </h3>

                    <div style={{
                      padding: '12px 16px',
                      backgroundColor: 'var(--bg-subtle)',
                      borderRadius: 'var(--radius-md)',
                      borderLeft: '3px solid var(--accent-primary)',
                      marginBottom: '10px'
                    }}>
                      <div style={{ fontSize: '11px', textTransform: 'uppercase', fontWeight: 700, color: 'var(--accent-primary)', marginBottom: '4px' }}>
                        Expected Candidate Concepts
                      </div>
                      <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                        {q.expectedAnswer}
                      </p>
                    </div>

                    {q.followUp && (
                      <div style={{ fontSize: '13px', color: 'var(--text-muted)', fontStyle: 'italic', paddingLeft: '4px' }}>
                        <strong>Probing Follow-Up:</strong> &ldquo;{q.followUp}&rdquo;
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Recruiter Evaluation & Feedback Section */}
          <div className="panel">
            <div className="panel-header">
              <div>
                <h3 className="panel-title">Evaluation & Interview Feedback</h3>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Provide notes to generate AI-assisted personalized candidate recommendations
                </span>
              </div>
              <span className="badge badge-vermilion">
                <Award size={14} /> AI-Assisted Feedback
              </span>
            </div>
            <div className="panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 220px', gap: '20px', marginBottom: '20px' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Recruiter Observations & Assessment Notes</label>
                  <textarea
                    rows={4}
                    className="form-textarea"
                    placeholder="Candidate demonstrated deep mastery of Spring Boot transactions and query planning. Stumbled slightly on distributed caching and Kafka consumer groups..."
                    value={feedbackNotes}
                    onChange={(e) => setFeedbackNotes(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Competency Score (0-100)</label>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '8px' }}>
                    <input
                      type="number"
                      min={0}
                      max={100}
                      className="form-input"
                      value={competencyScore}
                      onChange={(e) => setCompetencyScore(Number(e.target.value))}
                      style={{ fontSize: '20px', fontWeight: 700, textAlign: 'center' }}
                    />
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '8px' }}>
                    70+ Meets requirements &bull; 85+ Exceeds expectations
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '24px' }}>
                <button
                  onClick={handleSubmitFeedback}
                  disabled={submittingFeedback || !feedbackNotes.trim()}
                  className="btn btn-primary"
                >
                  <Sparkles size={16} />
                  <span>{submittingFeedback ? 'Synthesizing Roadmap...' : 'Save Feedback & Generate Learning Recommendations'}</span>
                </button>
              </div>

              {/* Synthesized AI Feedback Cards (If completed) */}
              {(activeSession.strengths || activeSession.improvementAreas || activeSession.learningRecommendations) && (
                <div style={{
                  padding: '24px',
                  backgroundColor: 'var(--bg-subtle)',
                  borderRadius: 'var(--radius-lg)',
                  border: '1px solid var(--border-subtle)'
                }}>
                  <div style={{ fontSize: '12px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--accent-primary)', marginBottom: '14px', letterSpacing: '0.04em' }}>
                    AI-Assisted Candidate Growth Report (Shared with Candidate)
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '20px' }}>
                    {/* Strengths */}
                    <div style={{ padding: '16px', backgroundColor: 'var(--color-success-bg)', borderRadius: 'var(--radius-md)', border: '1px solid #C3E6D0' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, fontSize: '13px', color: 'var(--color-success)', marginBottom: '8px' }}>
                        <CheckCircle2 size={16} />
                        <span>Demonstrated Strengths</span>
                      </div>
                      <ul style={{ paddingLeft: '18px', fontSize: '12px', color: 'var(--color-success)', lineHeight: '1.5' }}>
                        {activeSession.strengths?.map((str, idx) => (
                          <li key={idx}>{str}</li>
                        ))}
                      </ul>
                    </div>

                    {/* Improvement Areas */}
                    <div style={{ padding: '16px', backgroundColor: 'var(--color-warning-bg)', borderRadius: 'var(--radius-md)', border: '1px solid #FCD38D' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, fontSize: '13px', color: 'var(--color-warning)', marginBottom: '8px' }}>
                        <Clock size={16} />
                        <span>Areas for Growth</span>
                      </div>
                      <ul style={{ paddingLeft: '18px', fontSize: '12px', color: 'var(--color-warning)', lineHeight: '1.5' }}>
                        {activeSession.improvementAreas?.map((area, idx) => (
                          <li key={idx}>{area}</li>
                        ))}
                      </ul>
                    </div>

                    {/* Learning Roadmap */}
                    <div style={{ padding: '16px', backgroundColor: 'var(--color-info-bg)', borderRadius: 'var(--radius-md)', border: '1px solid #BEE3F8' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, fontSize: '13px', color: 'var(--color-info)', marginBottom: '8px' }}>
                        <BookOpen size={16} />
                        <span>Curated Learning Roadmap</span>
                      </div>
                      <ul style={{ paddingLeft: '18px', fontSize: '12px', color: 'var(--color-info)', lineHeight: '1.5' }}>
                        {activeSession.learningRecommendations?.map((rec, idx) => (
                          <li key={idx}>{rec}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="panel" style={{ textAlign: 'center', padding: '60px 24px' }}>
          <Sparkles size={40} color="var(--accent-primary)" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '18px', marginBottom: '6px' }}>Ready to Generate Assessment</h3>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', maxWidth: '480px', margin: '0 auto' }}>
            Select a candidate profile and an active job opening above to generate technical questions divided into Beginner, Intermediate, and Advanced tiers.
          </p>
        </div>
      )}
    </div>
  );
};
