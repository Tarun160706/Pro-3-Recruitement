import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ArrowRight, Lock, Mail, User, Phone, Briefcase, MapPin, FileText, CheckCircle2 } from 'lucide-react';

export const CandidateRegister: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [currentTitle, setCurrentTitle] = useState('');
  const [yearsOfExperience, setYearsOfExperience] = useState<number>(2);
  const [location, setLocation] = useState('');
  const [summary, setSummary] = useState('');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { registerCandidate } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }

    setIsSubmitting(true);

    try {
      await registerCandidate({
        fullName,
        email,
        password,
        confirmPassword,
        phone,
        currentTitle,
        yearsOfExperience: Number(yearsOfExperience),
        location,
        summary,
      });

      navigate(`/verify-email-notice?email=${encodeURIComponent(email)}&role=candidate`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please check your information.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: 'var(--bg-primary)',
      padding: '32px 20px'
    }}>
      <div style={{
        maxWidth: '560px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '36px 36px',
        boxShadow: 'var(--shadow-md)'
      }}>
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '46px',
            height: '46px',
            backgroundColor: 'var(--accent-primary)',
            color: 'white',
            borderRadius: 'var(--radius-lg)',
            fontSize: '22px',
            fontWeight: 800,
            marginBottom: '12px'
          }}>
            T
          </div>
          <h1 className="editorial-title" style={{ fontSize: '26px', marginBottom: '4px' }}>
            Candidate Registration
          </h1>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Build your AI-powered candidate profile and discover matched engineering roles
          </p>
        </div>

        {error && (
          <div style={{
            padding: '12px 16px',
            backgroundColor: 'var(--color-danger-bg)',
            color: 'var(--color-danger)',
            borderRadius: 'var(--radius-md)',
            fontSize: '13px',
            marginBottom: '20px',
            border: '1px solid #FED7D7'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Full Name *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Alex Rivera"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <User size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Email Address *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  required
                  className="form-input"
                  placeholder="alex.rivera@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Mail size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password (min 8 chars) *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  required
                  minLength={8}
                  className="form-input"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Confirm Password *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  required
                  minLength={8}
                  className="form-input"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="tel"
                  className="form-input"
                  placeholder="+1 (555) 234-5678"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Phone size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Current Job Title</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Senior Backend Engineer"
                  value={currentTitle}
                  onChange={(e) => setCurrentTitle(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Briefcase size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Years of Experience</label>
              <input
                type="number"
                min={0}
                max={50}
                className="form-input"
                value={yearsOfExperience}
                onChange={(e) => setYearsOfExperience(Number(e.target.value))}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Location (City, Country)</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="San Francisco, CA"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <MapPin size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Professional Summary</label>
              <div style={{ position: 'relative' }}>
                <textarea
                  className="form-input"
                  rows={3}
                  placeholder="Brief overview of your technical background, core technologies, and career interests..."
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                  style={{ resize: 'vertical' }}
                />
              </div>
            </div>
          </div>

          <div style={{
            padding: '12px 14px',
            backgroundColor: 'var(--bg-subtle)',
            borderRadius: 'var(--radius-md)',
            fontSize: '12px',
            color: 'var(--text-secondary)',
            marginBottom: '18px',
            lineHeight: 1.5
          }}>
            <CheckCircle2 size={14} color="var(--color-primary)" style={{ display: 'inline', marginRight: '6px', verticalAlign: 'middle' }} />
            After submitting, a secure verification link will be sent to your email address. You will verify your account before accessing candidate features.
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="btn btn-primary"
            style={{ width: '100%', padding: '12px' }}
          >
            <span>{isSubmitting ? 'Registering Candidate Profile...' : 'Complete Candidate Registration'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>
            Sign in here
          </Link>
          <span style={{ margin: '0 8px' }}>•</span>
          Hiring talent?{' '}
          <Link to="/register/recruiter" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>
            Recruiter Sign Up
          </Link>
        </div>
      </div>
    </div>
  );
};
export default CandidateRegister;

