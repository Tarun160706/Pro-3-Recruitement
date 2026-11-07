import React, { useState, useEffect } from 'react';
import { resumeApi, jobApi, candidateApi } from '../api';
import { Job, Candidate, ResumeUploadResponse } from '../types';
import {
  UploadCloud,
  FileText,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Briefcase,
  GraduationCap,
  Layers,
  ArrowRight,
  ShieldCheck
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ResumeAnalyzerPage: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [candidates, setCandidates] = useState<Candidate[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<string>('');
  const [selectedCandidateId, setSelectedCandidateId] = useState<string>('');
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<ResumeUploadResponse | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [activeTab, setActiveTab] = useState<'structured' | 'raw'>('structured');

  const navigate = useNavigate();

  useEffect(() => {
    jobApi.getAll({ status: 'ACTIVE' }).then(setJobs).catch(console.error);
    candidateApi.getAll().then(setCandidates).catch(console.error);
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleUploadAndAnalyze = async () => {
    if (!file) return;
    setUploading(true);
    try {
      const response = await resumeApi.upload(
        file,
        selectedCandidateId ? Number(selectedCandidateId) : undefined,
        selectedJobId ? Number(selectedJobId) : undefined
      );
      setResult(response);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to extract and parse resume. Please ensure valid PDF or text format.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Resume Intelligence Studio</h1>
          <p className="page-subtitle">
            PDF text extraction via Apache PDFBox &bull; Strict factual grounding &bull; Structured competencies.
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span className="badge badge-success">
            <ShieldCheck size={14} /> Zero Hallucination Mode
          </span>
        </div>
      </div>

      {/* Two Column Layout: Uploader on Left, Results on Right */}
      <div style={{ display: 'grid', gridTemplateColumns: result ? '1fr 1.6fr' : '1fr', gap: '28px' }}>
        {/* Upload Column */}
        <div className="panel" style={{ height: 'fit-content' }}>
          <div className="panel-header">
            <h3 className="panel-title">Upload Candidate Resume</h3>
          </div>
          <div className="panel-body">
            {/* Drag & Drop Zone */}
            <div
              className={`dropzone-box ${isDragOver ? 'drag-active' : ''}`}
              onDragOver={(e) => { e.preventDefault(); setIsDragOver(true); }}
              onDragLeave={() => setIsDragOver(false)}
              onDrop={handleDrop}
              onClick={() => document.getElementById('resumeFileInput')?.click()}
            >
              <input
                id="resumeFileInput"
                type="file"
                accept=".pdf,.txt"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <UploadCloud size={44} color="var(--accent-primary)" style={{ margin: '0 auto 12px' }} />
              {file ? (
                <div>
                  <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: '15px' }}>
                    {file.name}
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>
                    {(file.size / 1024).toFixed(1)} KB &bull; Click or drop another file to replace
                  </div>
                </div>
              ) : (
                <div>
                  <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: '15px', marginBottom: '4px' }}>
                    Click to browse or drop resume PDF here
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Supports PDF and Text resumes up to 10MB
                  </div>
                </div>
              )}
            </div>

            {/* Optional Linking Selectors */}
            <div style={{ marginTop: '20px' }}>
              <div className="form-group">
                <label className="form-label">Link to Job Opening (Optional)</label>
                <select
                  className="form-select"
                  value={selectedJobId}
                  onChange={(e) => setSelectedJobId(e.target.value)}
                >
                  <option value="">Do not link to specific opening</option>
                  {jobs.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.title} ({j.department})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Link to Existing Candidate (Optional)</label>
                <select
                  className="form-select"
                  value={selectedCandidateId}
                  onChange={(e) => setSelectedCandidateId(e.target.value)}
                >
                  <option value="">Auto-create new profile from resume</option>
                  {candidates.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.fullName} ({c.email})
                    </option>
                  ))}
                </select>
              </div>

              <button
                onClick={handleUploadAndAnalyze}
                disabled={!file || uploading}
                className="btn btn-primary"
                style={{ width: '100%', padding: '12px', marginTop: '10px' }}
              >
                <Sparkles size={16} />
                <span>{uploading ? 'Extracting & Parsing with AI...' : 'Analyze Resume Document'}</span>
              </button>
            </div>
          </div>
        </div>

        {/* Results Column */}
        {result && (
          <div className="panel fade-in">
            <div className="panel-header">
              <div>
                <h3 className="panel-title">Extracted Profile Intelligence</h3>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Grounded strictly in {result.fileName}
                </span>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={() => setActiveTab('structured')}
                  className={`btn btn-sm ${activeTab === 'structured' ? 'btn-secondary' : 'btn-ghost'}`}
                >
                  Structured Data
                </button>
                <button
                  onClick={() => setActiveTab('raw')}
                  className={`btn btn-sm ${activeTab === 'raw' ? 'btn-secondary' : 'btn-ghost'}`}
                >
                  Raw Text
                </button>
              </div>
            </div>

            <div className="panel-body">
              {activeTab === 'structured' ? (
                <div>
                  {/* Candidate Overview Card */}
                  <div style={{
                    padding: '16px 20px',
                    backgroundColor: 'var(--bg-subtle)',
                    borderRadius: 'var(--radius-lg)',
                    border: '1px solid var(--border-subtle)',
                    marginBottom: '20px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                      <h3 style={{ fontSize: '18px', fontWeight: 700 }}>
                        {result.analysis.candidateName || result.candidateName}
                      </h3>
                      <span className="badge badge-vermilion">
                        {result.analysis.yearsOfExperience || 0} Yrs Experience
                      </span>
                    </div>
                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                      {result.analysis.currentTitle || 'Software Engineer'} &bull; {result.analysis.email || result.candidateEmail} &bull; {result.analysis.location || 'Remote'}
                    </div>
                    {result.analysis.summary && (
                      <p style={{ fontSize: '13px', marginTop: '10px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                        {result.analysis.summary}
                      </p>
                    )}
                  </div>

                  {/* Skills Section */}
                  <div style={{ marginBottom: '22px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                      <Layers size={16} color="var(--accent-primary)" />
                      <h4 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                        Extracted Skills ({result.analysis.skills ? result.analysis.skills.length : 0})
                      </h4>
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                      {result.analysis.skills && result.analysis.skills.map((skill, idx) => (
                        <span key={idx} className="skill-tag" style={{ backgroundColor: 'white', borderColor: 'var(--border-default)' }}>
                          {skill}
                        </span>
                      ))}
                    </div>
                  </div>

                  {/* Strengths & Missing Information Grid */}
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '22px' }}>
                    <div style={{ padding: '14px', backgroundColor: 'var(--color-success-bg)', borderRadius: 'var(--radius-md)', border: '1px solid #C3E6D0' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                        <CheckCircle2 size={15} color="var(--color-success)" />
                        <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-success)' }}>Verified Strengths</span>
                      </div>
                      <ul style={{ paddingLeft: '18px', fontSize: '12px', color: 'var(--color-success)', lineHeight: '1.5' }}>
                        {result.analysis.strengths && result.analysis.strengths.map((str, idx) => (
                          <li key={idx}>{str}</li>
                        ))}
                      </ul>
                    </div>

                    <div style={{ padding: '14px', backgroundColor: 'var(--color-warning-bg)', borderRadius: 'var(--radius-md)', border: '1px solid #FCD38D' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                        <AlertTriangle size={15} color="var(--color-warning)" />
                        <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-warning)' }}>Information Gaps</span>
                      </div>
                      <ul style={{ paddingLeft: '18px', fontSize: '12px', color: 'var(--color-warning)', lineHeight: '1.5' }}>
                        {result.analysis.missingInfo && result.analysis.missingInfo.map((gap, idx) => (
                          <li key={idx}>{gap}</li>
                        ))}
                      </ul>
                    </div>
                  </div>

                  {/* Experience Timeline */}
                  {result.analysis.experience && result.analysis.experience.length > 0 && (
                    <div style={{ marginBottom: '22px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
                        <Briefcase size={16} color="var(--accent-primary)" />
                        <h4 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                          Career History
                        </h4>
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {result.analysis.experience.map((exp, idx) => (
                          <div key={idx} style={{ padding: '12px 16px', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', fontWeight: 700 }}>
                              <span>{exp.role}</span>
                              <span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>{exp.duration}</span>
                            </div>
                            <div style={{ fontSize: '12px', color: 'var(--accent-primary)', fontWeight: 600, marginBottom: '4px' }}>
                              {exp.company}
                            </div>
                            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                              {exp.description}
                            </p>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Education Timeline */}
                  {result.analysis.education && result.analysis.education.length > 0 && (
                    <div style={{ marginBottom: '22px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
                        <GraduationCap size={16} color="var(--accent-primary)" />
                        <h4 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                          Education
                        </h4>
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {result.analysis.education.map((edu, idx) => (
                          <div key={idx} style={{ fontSize: '13px', padding: '10px 14px', backgroundColor: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)' }}>
                            <strong>{edu.degree}</strong> &bull; {edu.institution} {edu.year ? `(${edu.year})` : ''}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Bottom Action Triggers */}
                  <div style={{ display: 'flex', gap: '12px', paddingTop: '16px', borderTop: '1px solid var(--border-subtle)' }}>
                    <button
                      onClick={() => navigate('/candidates')}
                      className="btn btn-secondary"
                    >
                      <span>View in Candidate Pipeline</span>
                    </button>
                    <button
                      onClick={() => navigate('/interviews', { state: { candidateId: result.candidateId } })}
                      className="btn btn-primary"
                    >
                      <Sparkles size={15} />
                      <span>Generate Technical Interview</span>
                      <ArrowRight size={14} />
                    </button>
                  </div>
                </div>
              ) : (
                <div>
                  <pre style={{
                    fontSize: '12px',
                    lineHeight: '1.5',
                    fontFamily: 'JetBrains Mono, monospace',
                    backgroundColor: 'var(--bg-subtle)',
                    padding: '16px',
                    borderRadius: 'var(--radius-md)',
                    overflowX: 'auto',
                    whiteSpace: 'pre-wrap',
                    maxHeight: '500px'
                  }}>
                    {result.rawText}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
