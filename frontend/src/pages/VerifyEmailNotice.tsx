import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { authApi } from '../api';
import { Mail, CheckCircle2, AlertCircle, RefreshCw, ArrowLeft, Shield } from 'lucide-react';

export const VerifyEmailNotice: React.FC = () => {
  const [searchParams] = useSearchParams();
  const emailParam = searchParams.get('email') || '';
  const roleParam = searchParams.get('role') || '';

  const [email, setEmail] = useState(emailParam);
  const [cooldown, setCooldown] = useState(0);
  const [isResending, setIsResending] = useState(false);
  const [resendStatus, setResendStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    let timer: any;
    if (cooldown > 0) {
      timer = setInterval(() => {
        setCooldown((c) => Math.max(0, c - 1));
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [cooldown]);

  const handleResend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || cooldown > 0 || isResending) return;

    setIsResending(true);
    setResendStatus(null);

    try {
      const res = await authApi.resendVerification(email);
      setResendStatus({
        type: 'success',
        message: res.message || 'If an account exists with this email, a new verification link has been sent.',
      });
      setCooldown(120); // 2-minute cooldown
    } catch (err: any) {
      setResendStatus({
        type: 'error',
        message: err.response?.data?.message || 'Failed to resend verification link. Please wait or try again later.',
      });
      if (err.response?.status === 429) {
        setCooldown(120);
      }
    } finally {
      setIsResending(false);
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
        maxWidth: '500px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '40px 36px',
        boxShadow: 'var(--shadow-md)',
        textAlign: 'center'
      }}>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '64px',
          height: '64px',
          backgroundColor: '#EBF8FF',
          color: '#3182CE',
          borderRadius: '50%',
          marginBottom: '20px'
        }}>
          <Mail size={32} />
        </div>

        <h1 className="editorial-title" style={{ fontSize: '24px', marginBottom: '8px' }}>
          Check Your Inbox
        </h1>

        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '20px' }}>
          We have sent a secure, single-use verification link to:
          <br />
          <strong style={{ color: 'var(--text-primary)', wordBreak: 'break-all' }}>{email || 'your registered email'}</strong>
        </p>

        {roleParam === 'recruiter' && (
          <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '10px',
            padding: '12px 14px',
            backgroundColor: 'var(--bg-subtle)',
            borderRadius: 'var(--radius-md)',
            fontSize: '12px',
            color: 'var(--text-secondary)',
            textAlign: 'left',
            marginBottom: '24px',
            lineHeight: 1.5
          }}>
            <Shield size={16} color="var(--accent-primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <strong>Note for Recruiters:</strong> Clicking the verification link confirms your work email address. After email verification, our team will review your organization credentials.
            </div>
          </div>
        )}

        {resendStatus && (
          <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '8px',
            padding: '12px 14px',
            backgroundColor: resendStatus.type === 'success' ? '#F0FFF4' : 'var(--color-danger-bg)',
            color: resendStatus.type === 'success' ? '#22543D' : 'var(--color-danger)',
            borderRadius: 'var(--radius-md)',
            fontSize: '13px',
            textAlign: 'left',
            marginBottom: '20px',
            border: `1px solid ${resendStatus.type === 'success' ? '#C6F6D5' : '#FED7D7'}`
          }}>
            {resendStatus.type === 'success' ? <CheckCircle2 size={16} style={{ flexShrink: 0, marginTop: '2px' }} /> : <AlertCircle size={16} style={{ flexShrink: 0, marginTop: '2px' }} />}
            <span>{resendStatus.message}</span>
          </div>
        )}

        {/* Resend Form */}
        <div style={{
          backgroundColor: 'var(--bg-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: '20px',
          marginBottom: '24px',
          textAlign: 'left'
        }}>
          <label className="form-label" style={{ fontSize: '12px' }}>Didn't receive an email or link expired?</label>
          <form onSubmit={handleResend} style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
            <input
              type="email"
              required
              placeholder="Enter email address"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              style={{ flex: 1, fontSize: '13px' }}
            />
            <button
              type="submit"
              disabled={isResending || cooldown > 0}
              className="btn btn-outline"
              style={{ whiteSpace: 'nowrap', fontSize: '13px' }}
            >
              {isResending ? (
                <>
                  <RefreshCw size={14} className="spin" />
                  <span>Sending...</span>
                </>
              ) : cooldown > 0 ? (
                <span>Wait {cooldown}s</span>
              ) : (
                <span>Resend Link</span>
              )}
            </button>
          </form>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px', marginBottom: 0 }}>
            Verification links expire after 24 hours. Rate limited to 1 request per 2 minutes.
          </p>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <Link
            to="/login"
            className="btn btn-ghost"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '13px', textDecoration: 'none' }}
          >
            <ArrowLeft size={14} />
            <span>Return to Sign In</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
export default VerifyEmailNotice;

