import React, { useEffect, useState } from 'react';
import { jobApi } from '../api';
import { Job } from '../types';
import { useAuth } from '../context/AuthContext';
import {
  Briefcase,
  Plus,
  Search,
  MapPin,
  Clock,
  Users,
  X,
  Sparkles,
  Archive,
  CheckCircle,
  ExternalLink
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const JobsPage: React.FC = () => {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { isRecruiter, isAdmin } = useAuth();
  const canManageJobs = isRecruiter || isAdmin;
  const navigate = useNavigate();

  // New Job Form State
  const [title, setTitle] = useState('');
  const [deptInput, setDeptInput] = useState('Engineering');
  const [location, setLocation] = useState('Remote');
  const [jobType, setJobType] = useState('FULL_TIME');
  const [expMin, setExpMin] = useState(3);
  const [expMax, setExpMax] = useState(7);
  const [skillInput, setSkillInput] = useState('');
  const [skillsList, setSkillsList] = useState<string[]>(['Java', 'Spring Boot', 'PostgreSQL']);
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchJobs = async () => {
    try {
      setLoading(true);
      const data = await jobApi.getAll({
        search: search || undefined,
        department: department || undefined
      });
      setJobs(data);
    } catch (err) {
      console.error('Failed to load jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
  }, [department]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchJobs();
  };

  const handleAddSkill = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && skillInput.trim()) {
      e.preventDefault();
      if (!skillsList.includes(skillInput.trim())) {
        setSkillsList([...skillsList, skillInput.trim()]);
      }
      setSkillInput('');
    }
  };

  const handleRemoveSkill = (skillToRemove: string) => {
    setSkillsList(skillsList.filter(s => s !== skillToRemove));
  };

  const handleCreateJob = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await jobApi.create({
        title,
        department: deptInput,
        location,
        jobType,
        experienceMin: expMin,
        experienceMax: expMax,
        requiredSkills: skillsList,
        description,
        status: 'ACTIVE'
      });
      setIsModalOpen(false);
      resetForm();
      fetchJobs();
    } catch (err) {
      alert('Failed to publish job opening.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (job: Job) => {
    const nextStatus = job.status === 'ACTIVE' ? 'ARCHIVED' : 'ACTIVE';
    try {
      await jobApi.updateStatus(job.id, nextStatus);
      fetchJobs();
    } catch (err) {
      alert('Could not update status');
    }
  };

  const resetForm = () => {
    setTitle('');
    setDescription('');
    setSkillsList(['Java', 'Spring Boot', 'PostgreSQL']);
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Requisition & Job Management</h1>
          <p className="page-subtitle">
            Configure positions, define skill prerequisites, and monitor applicant pipelines.
          </p>
        </div>
        {canManageJobs && (
          <button onClick={() => setIsModalOpen(true)} className="btn btn-primary">
            <Plus size={16} />
            <span>Create Position</span>
          </button>
        )}
      </div>

      {/* Filter & Search Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '16px',
        marginBottom: '24px',
        flexWrap: 'wrap'
      }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '8px', flex: 1, maxWidth: '480px' }}>
          <div style={{ position: 'relative', width: '100%' }}>
            <input
              type="text"
              placeholder="Search by job title or keyword..."
              className="form-input"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: '38px' }}
            />
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
          </div>
          <button type="submit" className="btn btn-secondary">
            Search
          </button>
        </form>

        <div style={{ display: 'flex', gap: '8px' }}>
          <select
            className="form-select"
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
            style={{ width: '180px' }}
          >
            <option value="">All Departments</option>
            <option value="Platform Engineering">Platform Engineering</option>
            <option value="Product Engineering">Product Engineering</option>
            <option value="AI & Intelligence">AI & Intelligence</option>
          </select>
        </div>
      </div>

      {/* Jobs Grid / List */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
          Loading positions...
        </div>
      ) : jobs.length === 0 ? (
        <div className="panel" style={{ textAlign: 'center', padding: '48px 24px' }}>
          <Briefcase size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '18px', marginBottom: '6px' }}>No Job Openings Found</h3>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '18px' }}>
            Try adjusting your search criteria or create a new job opening.
          </p>
          {canManageJobs && (
            <button onClick={() => setIsModalOpen(true)} className="btn btn-primary">
              <Plus size={16} />
              <span>Post New Role</span>
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {jobs.map((job) => (
            <div key={job.id} className="panel" style={{ marginBottom: 0, padding: '24px' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '20px', flexWrap: 'wrap' }}>
                <div style={{ flex: 1, minWidth: '300px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                    <h2 style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-main)', fontFamily: 'inherit' }}>
                      {job.title}
                    </h2>
                    <span className={`badge ${job.status === 'ACTIVE' ? 'badge-active' : 'badge-neutral'}`}>
                      {job.status}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '18px', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '14px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{job.department}</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <MapPin size={14} color="var(--text-muted)" />
                      {job.location}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Clock size={14} color="var(--text-muted)" />
                      {job.experienceMin} - {job.experienceMax} Yrs Exp
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Users size={14} color="var(--text-muted)" />
                      {job.applicantCount} Applicant{job.applicantCount !== 1 ? 's' : ''}
                    </span>
                  </div>

                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: '1.5', marginBottom: '16px' }}>
                    {job.description}
                  </p>

                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                    {job.requiredSkills.map((skill, idx) => (
                      <span key={idx} className="skill-tag">
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'flex-end' }}>
                  {canManageJobs ? (
                    <>
                      <button
                        onClick={() => navigate('/candidates')}
                        className="btn btn-secondary btn-sm"
                      >
                        <Users size={14} />
                        <span>View Applicants</span>
                      </button>
                      <button
                        onClick={() => handleToggleStatus(job)}
                        className="btn btn-ghost btn-sm"
                        style={{ color: 'var(--text-muted)' }}
                      >
                        {job.status === 'ACTIVE' ? (
                          <>
                            <Archive size={14} />
                            <span>Archive Role</span>
                          </>
                        ) : (
                          <>
                            <CheckCircle size={14} />
                            <span>Activate Role</span>
                          </>
                        )}
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => navigate('/portal')}
                      className="btn btn-primary btn-sm"
                    >
                      <ExternalLink size={14} />
                      <span>Apply with Profile</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Job Modal */}
      {isModalOpen && (
        <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content fade-in" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h2 className="editorial-title" style={{ fontSize: '22px' }}>Publish New Position</h2>
                <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Set role competencies for AI candidate matching
                </span>
              </div>
              <button onClick={() => setIsModalOpen(false)} className="btn btn-ghost" style={{ padding: '6px' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateJob}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Position Title *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Senior Backend Systems Engineer"
                    className="form-input"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label className="form-label">Department</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. Platform Engineering"
                      value={deptInput}
                      onChange={(e) => setDeptInput(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Location / Work Style</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. Remote (US/India) or Hybrid"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label className="form-label">Min. Experience (Years)</label>
                    <input
                      type="number"
                      min={0}
                      className="form-input"
                      value={expMin}
                      onChange={(e) => setExpMin(Number(e.target.value))}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Max. Experience (Years)</label>
                    <input
                      type="number"
                      min={expMin}
                      className="form-input"
                      value={expMax}
                      onChange={(e) => setExpMax(Number(e.target.value))}
                    />
                  </div>
                </div>

                {/* Skill Tags Creator */}
                <div className="form-group">
                  <label className="form-label">
                    Required Competencies (Press Enter to Add)
                  </label>
                  <input
                    type="text"
                    placeholder="Type skill e.g. Java, Docker, REST APIs..."
                    className="form-input"
                    value={skillInput}
                    onChange={(e) => setSkillInput(e.target.value)}
                    onKeyDown={handleAddSkill}
                  />
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginTop: '10px' }}>
                    {skillsList.map((skill, idx) => (
                      <span key={idx} className="skill-tag" style={{ backgroundColor: 'var(--accent-light)', borderColor: 'var(--accent-border)' }}>
                        {skill}
                        <X
                          size={13}
                          style={{ marginLeft: '6px', cursor: 'pointer' }}
                          onClick={() => handleRemoveSkill(skill)}
                        />
                      </span>
                    ))}
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Job Description & Responsibilities *</label>
                  <textarea
                    rows={4}
                    required
                    className="form-textarea"
                    placeholder="Describe role expectations, deliverables, and engineering standards..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  <span>{submitting ? 'Publishing...' : 'Publish Position'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
