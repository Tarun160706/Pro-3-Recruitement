import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ArrowRight, Lock, Mail, User, Phone, Briefcase, Building2, Globe, MapPin, ShieldCheck } from 'lucide-react';

export const RecruiterRegister: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [companyWebsite, setCompanyWebsite] = useState('');
  const [companyAddress, setCompanyAddress] = useState('');
  const [designation, setDesignation] = useState('');

  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { registerRecruiter } = useAuth();
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
      await registerRecruiter({
        fullName,
        email,
        password,
        confirmPassword,
        phone,
        companyName,
        companyWebsite,
        companyAddress,
        designation,
      });

      navigate(`/verify-email-notice?email=${encodeURIComponent(email)}&role=recruiter`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Recruiter registration failed. Please check your information.');
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
        maxWidth: '580px',
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
            Recruiter & Organization Registration
          </h1>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Enterprise hiring intelligence, automated job matching, and AI interviews
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
          {/* Section: Personal Info */}
          <h3 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '12px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '6px' }}>
            Recruiter Details
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '20px' }}>
            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Full Name *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Eleanor Vance"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <User size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Work Email *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  required
                  className="form-input"
                  placeholder="eleanor@acmecorp.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Mail size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="tel"
                  required
                  className="form-input"
                  placeholder="+1 (555) 432-1098"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Phone size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Designation / Role Title *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Head of Technical Talent Acquisition"
                  value={designation}
                  onChange={(e) => setDesignation(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Briefcase size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
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
          </div>

          {/* Section: Company Info */}
          <h3 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '12px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '6px' }}>
            Company / Organization Verification Details
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '18px' }}>
            <div className="form-group">
              <label className="form-label">Company Legal Name *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Acme Technologies Inc."
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Building2 size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Official Website Domain *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="https://acmecorp.com"
                  value={companyWebsite}
                  onChange={(e) => setCompanyWebsite(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <Globe size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>

            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Headquarters / Office Address *</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="500 Howard Street, Suite 400, San Francisco, CA 94105"
                  value={companyAddress}
                  onChange={(e) => setCompanyAddress(e.target.value)}
                  style={{ paddingLeft: '38px' }}
                />
                <MapPin size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
              </div>
            </div>
          </div>

          {/* Multi-step Notice */}
          <div style={{
            padding: '14px',
            backgroundColor: '#F7FAFC',
            border: '1px solid #E2E8F0',
            borderRadius: 'var(--radius-md)',
            fontSize: '12px',
            color: 'var(--text-secondary)',
            marginBottom: '20px',
            lineHeight: 1.5
          }}>
            <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <ShieldCheck size={15} color="var(--accent-primary)" />
              2-Tier Verification Process
            </div>
            Recruiter accounts require two stages before recruiter features are unlocked:
            <ol style={{ paddingLeft: '18px', marginTop: '4px', marginBottom: 0 }}>
              <li><strong>Email Verification:</strong> Confirm your work email via single-use link.</li>
              <li><strong>Administrative Review:</strong> TalentIQ administrators verify your organization and approve your recruiter profile.</li>
            </ol>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="btn btn-primary"
            style={{ width: '100%', padding: '12px' }}
          >
            <span>{isSubmitting ? 'Submitting Recruiter Registration...' : 'Register Recruiter Account'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>
            Sign in here
          </Link>
          <span style={{ margin: '0 8px' }}>•</span>
          Looking for a job?{' '}
          <Link to="/register/candidate" style={{ color: 'var(--accent-primary)', fontWeight: 600 }}>
            Candidate Sign Up
          </Link>
        </div>
      </div>
    </div>
  );
};
export default RecruiterRegister;

