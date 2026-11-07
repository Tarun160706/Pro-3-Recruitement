import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Plus, Upload, ShieldCheck } from 'lucide-react';

interface TopBarProps {
  onOpenNewJob?: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({ onOpenNewJob }) => {
  const { isRecruiter, isAdmin } = useAuth();
  const hasRecruiterAccess = isRecruiter || isAdmin;

  return (
    <header className="top-bar">
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <span className="badge badge-vermilion" style={{ padding: '4px 10px' }}>
          <ShieldCheck size={14} /> Production Ready
        </span>
        <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          PostgreSQL 18 &bull; Spring Boot 3.2 &bull; Gemini AI &bull; Stateless JWT
        </span>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        {hasRecruiterAccess ? (
          <>
            <Link to="/resumes" className="btn btn-secondary btn-sm">
              <Upload size={14} />
              <span>Parse Resume</span>
            </Link>
            {onOpenNewJob ? (
              <button onClick={onOpenNewJob} className="btn btn-primary btn-sm">
                <Plus size={14} />
                <span>Post Job</span>
              </button>
            ) : (
              <Link to="/jobs" className="btn btn-primary btn-sm">
                <Plus size={14} />
                <span>Job Openings</span>
              </Link>
            )}
          </>
        ) : (
          <Link to="/jobs" className="btn btn-primary btn-sm">
            <span>Browse Active Openings</span>
          </Link>
        )}
      </div>
    </header>
  );
};
