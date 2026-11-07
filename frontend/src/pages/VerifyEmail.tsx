import React, { useEffect, useState } from 'react';
import { useSearchParams, Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import { MessageResponse } from '../types';
import { CheckCircle2, XCircle, Loader2, ArrowRight, ShieldCheck, Mail } from 'lucide-react';

export const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [isLoading, setIsLoading] = useState(true);
  const [result, setResult] = useState<MessageResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      setIsLoading(false);
      setError('Verification token is missing. Please check the link from your email.');
      return;
    }

    const verify = async () => {
      try {
        const data = await authApi.verifyEmail(token);
        setResult(data);
      } catch (err: any) {
        setError(
          err.response?.data?.message ||
          'Verification failed. The token may be expired or already used.'
        );
      } finally {
        setIsLoading(false);
      }
    };

    verify();
  }, [token]);

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
        maxWidth: '480px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '40px 36px',
        boxShadow: 'var(--shadow-md)',
        textAlign: 'center'
      }}>
        {isLoading && (
          <div>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '64px',
              height: '64px',
              backgroundColor: 'var(--bg-subtle)',
              borderRadius: '50%',
              marginBottom: '20px'
            }}>
              <Loader2 size={32} color="var(--accent-primary)" className="spin" />
            </div>
            <h2 className="editorial-title" style={{ fontSize: '22px', marginBottom: '8px' }}>
              Verifying Your Email
            </h2>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
              Validating your cryptographic security token with TalentIQ server...
            </p>
          </div>
        )}

        {!isLoading && result && (
          <div>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '64px',
              height: '64px',
              backgroundColor: '#F0FFF4',
              color: '#38A169',
              borderRadius: '50%',
              marginBottom: '20px'
            }}>
              <CheckCircle2 size={36} />
            </div>

            <h2 className="editorial-title" style={{ fontSize: '24px', marginBottom: '8px' }}>
              {result.verificationStatus === 'REVIEW_PENDING' ? 'Work Email Verified!' : 'Email Verified Successfully!'}
            </h2>

            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '24px' }}>
              {result.message}
            </p>

            {result.verificationStatus === 'REVIEW_PENDING' && (
              <div style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px',
                padding: '14px',
                backgroundColor: '#FFFDF5',
                border: '1px solid #FEFCBF',
                borderRadius: 'var(--radius-md)',
                fontSize: '13px',
                color: '#744210',
                textAlign: 'left',
                marginBottom: '24px',
                lineHeight: 1.5
              }}>
                <ShieldCheck size={18} color="#D69E2E" style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <strong>Next Step: Organization Review</strong>
                  <div style={{ marginTop: '4px' }}>
                    Your organization profile is now under administrative review. Once approved, you will have access to post jobs, search candidates, and run AI interviews.
                  </div>
                </div>
              </div>
            )}

            <button
              onClick={() => navigate('/login', { state: { message: 'Email verified. Please sign in.' } })}
              className="btn btn-primary"
              style={{ width: '100%', padding: '12px' }}
            >
              <span>Continue to Sign In</span>
              <ArrowRight size={16} />
            </button>
          </div>
        )}

        {!isLoading && error && (
          <div>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '64px',
              height: '64px',
              backgroundColor: '#FFF5F5',
              color: '#E53E3E',
              borderRadius: '50%',
              marginBottom: '20px'
            }}>
              <XCircle size={36} />
            </div>

            <h2 className="editorial-title" style={{ fontSize: '24px', marginBottom: '8px' }}>
              Verification Failed
            </h2>

            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '24px' }}>
              {error}
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <Link
                to="/verify-email-notice"
                className="btn btn-primary"
                style={{ justifyContent: 'center', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px' }}
              >
                <Mail size={16} />
                <span>Request New Verification Link</span>
              </Link>
              <Link
                to="/login"
                className="btn btn-outline"
                style={{ justifyContent: 'center', textDecoration: 'none' }}
              >
                Return to Sign In
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
export default VerifyEmail;

