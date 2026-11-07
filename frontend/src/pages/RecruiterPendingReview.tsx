import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Clock, CheckCircle2, ShieldAlert, Building2, Globe, MapPin, Briefcase, RefreshCw, LogOut } from 'lucide-react';

export const RecruiterPendingReview: React.FC = () => {
  const { user, refreshUser, logout } = useAuth();
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleRefresh = async () => {
    setIsRefreshing(true);
    setMessage(null);
    try {
      const updated = await refreshUser();
      if (updated?.verificationStatus === 'VERIFIED') {
        navigate('/dashboard');
      } else if (updated?.verificationStatus === 'REJECTED') {
        navigate('/recruiter/rejected');
      } else {
        setMessage('Your application is still under review. Please check back later.');
      }
    } catch {
      setMessage('Could not check status. Please check your network connection.');
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
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
        maxWidth: '560px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '36px 36px',
        boxShadow: 'var(--shadow-md)'
      }}>
        {/* Header Badge */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '56px',
            height: '56px',
            backgroundColor: '#FFFDF5',
            color: '#D69E2E',
            borderRadius: '50%',
            marginBottom: '14px',
            border: '2px solid #FEFCBF'
          }}>
            <Clock size={28} />
          </div>
          <h1 className="editorial-title" style={{ fontSize: '24px', marginBottom: '6px' }}>
            Application Under Review
          </h1>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Your recruiter account is currently being reviewed by TalentIQ administrators
          </p>
        </div>

        {/* Progress Tracker */}
        <div style={{
          backgroundColor: 'var(--bg-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: '16px 20px',
          marginBottom: '24px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
            <CheckCircle2 size={18} color="#38A169" />
            <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Step 1: Work Email Verified
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
            <Clock size={18} color="#D69E2E" />
            <div style={{ fontSize: '13px', fontWeight: 600, color: '#B7791F' }}>
              Step 2: Organization Review & Domain Verification (In Progress)
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '18px', height: '18px', borderRadius: '50%', border: '2px dashed var(--border-default)' }} />
            <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Step 3: Recruiter Intelligence Dashboard Access
            </div>
          </div>
        </div>

        {/* Submitted Organization Details */}
        <div style={{
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '16px',
          marginBottom: '24px',
          fontSize: '13px'
        }}>
          <h3 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Submitted Credentials
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', color: 'var(--text-secondary)' }}>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Company</div>
              <div style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                <Building2 size={13} />
                <span>{user?.companyName || 'Not specified'}</span>
              </div>
            </div>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Website</div>
              <div style={{ fontWeight: 500, color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                <Globe size={13} />
                <span>{user?.companyWebsite || 'Not specified'}</span>
              </div>
            </div>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Recruiter</div>
              <div style={{ fontWeight: 500, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                <Briefcase size={13} />
                <span>{user?.fullName} ({user?.designation || 'Recruiter'})</span>
              </div>
            </div>
            <div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Headquarters</div>
              <div style={{ fontWeight: 500, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                <MapPin size={13} />
                <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user?.companyAddress || 'Not specified'}</span>
              </div>
            </div>
          </div>
        </div>

        {message && (
          <div style={{
            padding: '10px 14px',
            backgroundColor: '#FFFDF5',
            color: '#744210',
            border: '1px solid #FEFCBF',
            borderRadius: 'var(--radius-md)',
            fontSize: '12px',
            marginBottom: '16px',
            textAlign: 'center'
          }}>
            {message}
          </div>
        )}

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="btn btn-primary"
            style={{ flex: 1, padding: '10px 16px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
          >
            <RefreshCw size={14} className={isRefreshing ? 'spin' : ''} />
            <span>{isRefreshing ? 'Checking...' : 'Check Approval Status'}</span>
          </button>
          <button
            onClick={handleLogout}
            className="btn btn-outline"
            style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
          >
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </div>
  );
};
export default RecruiterPendingReview;

