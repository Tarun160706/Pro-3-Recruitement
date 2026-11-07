import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ArrowRight, Lock, Mail, AlertCircle, CheckCircle2, UserCheck, Briefcase } from 'lucide-react';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isUnverified, setIsUnverified] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const successMessage = (location.state as any)?.message;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsUnverified(false);
    setIsSubmitting(true);

    try {
      const user = await login(email, password);

      if (user.role === 'ROLE_ADMIN') {
        navigate('/dashboard');
      } else if (user.role === 'ROLE_RECRUITER') {
        if (user.verificationStatus === 'VERIFIED') {
          navigate('/dashboard');
        } else if (user.verificationStatus === 'REVIEW_PENDING') {
          navigate('/recruiter/pending-review');
        } else if (user.verificationStatus === 'REJECTED') {
          navigate('/recruiter/rejected');
        } else {
          navigate(`/verify-email-notice?email=${encodeURIComponent(user.email)}&role=recruiter`);
        }
      } else {
        // Candidate
        navigate('/portal');
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Invalid email or password. Please try again.';
      setError(msg);
      if (
        msg.toLowerCase().includes('verify your email') ||
        msg.toLowerCase().includes('email is not verified')
      ) {
        setIsUnverified(true);
      }
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
      padding: '24px'
    }}>
      <div style={{
        maxWidth: '440px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '40px 36px',
        boxShadow: 'var(--shadow-md)'
      }}>
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '48px',
            height: '48px',
            backgroundColor: 'var(--accent-primary)',
            color: 'white',
            borderRadius: 'var(--radius-lg)',
            fontSize: '24px',
            fontWeight: 800,
            marginBottom: '14px',
            boxShadow: '0 4px 12px rgba(212, 74, 50, 0.3)'
          }}>
            T
          </div>
          <h1 className="editorial-title" style={{ fontSize: '28px', marginBottom: '6px' }}>
            TalentIQ
          </h1>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            AI-Powered Recruitment & Interview Intelligence Platform
          </p>
        </div>

        {successMessage && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            padding: '12px 16px',
            backgroundColor: '#F0FFF4',
            color: '#22543D',
            borderRadius: 'var(--radius-md)',
            fontSize: '13px',
            marginBottom: '20px',
            border: '1px solid #C6F6D5'
          }}>
            <CheckCircle2 size={16} />
            <span>{successMessage}</span>
          </div>
        )}

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
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
              <AlertCircle size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <div>{error}</div>
                {isUnverified && (
                  <div style={{ marginTop: '8px' }}>
                    <Link
                      to={`/verify-email-notice?email=${encodeURIComponent(email)}`}
                      style={{ color: 'var(--accent-primary)', fontWeight: 600, textDecoration: 'underline' }}
                    >
                      Resend verification link &rarr;
                    </Link>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <input
                type="email"
                required
                className="form-input"
                placeholder="name@organization.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ paddingLeft: '38px' }}
                autoComplete="email"
              />
              <Mail size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <div style={{ position: 'relative' }}>
              <input
                type="password"
                required
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ paddingLeft: '38px' }}
                autoComplete="current-password"
              />
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '13px' }} />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="btn btn-primary"
            style={{ width: '100%', padding: '12px', marginTop: '8px' }}
          >
            <span>{isSubmitting ? 'Authenticating...' : 'Sign In to Account'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        {/* Real Registration Options */}
        <div style={{
          marginTop: '28px',
          paddingTop: '20px',
          borderTop: '1px solid var(--border-subtle)',
          textAlign: 'center'
        }}>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '14px' }}>
            Don't have an account yet?
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <Link
              to="/register/candidate"
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'center', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <UserCheck size={14} />
              <span>Candidate Sign Up</span>
            </Link>
            <Link
              to="/register/recruiter"
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'center', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Briefcase size={14} />
              <span>Recruiter Sign Up</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Login;
