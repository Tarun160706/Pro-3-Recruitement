import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { XCircle, AlertTriangle, LogOut, Mail } from 'lucide-react';

export const RecruiterRejected: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

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
        maxWidth: '520px',
        width: '100%',
        backgroundColor: 'var(--bg-surface)',
        borderRadius: 'var(--radius-xl)',
        border: '1px solid var(--border-default)',
        padding: '36px 36px',
        boxShadow: 'var(--shadow-md)',
        textAlign: 'center'
      }}>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '56px',
          height: '56px',
          backgroundColor: '#FFF5F5',
          color: '#E53E3E',
          borderRadius: '50%',
          marginBottom: '16px',
          border: '2px solid #FED7D7'
        }}>
          <XCircle size={32} />
        </div>

        <h1 className="editorial-title" style={{ fontSize: '24px', marginBottom: '8px' }}>
          Recruiter Application Declined
        </h1>

        <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '20px' }}>
          We were unable to verify the organization credentials submitted for account{' '}
          <strong style={{ color: 'var(--text-primary)' }}>{user?.email}</strong>.
        </p>

        {user?.rejectionReason && (
          <div style={{
            padding: '14px 16px',
            backgroundColor: '#FFF5F5',
            border: '1px solid #FEB2B2',
            borderRadius: 'var(--radius-md)',
            textAlign: 'left',
            marginBottom: '24px',
            fontSize: '13px',
            color: '#9B2C2C'
          }}>
            <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
              <AlertTriangle size={15} />
              Reviewer Feedback:
            </div>
            <div>{user.rejectionReason}</div>
          </div>
        )}

        <div style={{
          backgroundColor: 'var(--bg-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '16px',
          fontSize: '13px',
          color: 'var(--text-secondary)',
          textAlign: 'left',
          marginBottom: '24px',
          lineHeight: 1.5
        }}>
          <strong>Next Steps:</strong>
          <ul style={{ paddingLeft: '18px', marginTop: '6px', marginBottom: 0 }}>
            <li>Ensure you registered using an official company domain email address (e.g., @yourcompany.com).</li>
            <li>Verify that your company website is live and publicly accessible.</li>
            <li>If you believe this decision was made in error, contact support at <strong>support@talentiq.ai</strong>.</li>
          </ul>
        </div>

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
          <button
            onClick={handleLogout}
            className="btn btn-outline"
            style={{ padding: '10px 20px', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </div>
  );
};
export default RecruiterRejected;

