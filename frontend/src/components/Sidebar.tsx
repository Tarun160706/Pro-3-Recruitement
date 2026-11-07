import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Briefcase,
  Users,
  FileText,
  Sparkles,
  UserCheck,
  LogOut,
  Compass,
  ShieldCheck
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user, logout, isRecruiter, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const hasRecruiterAccess = isRecruiter || isAdmin;

  return (
    <aside className="sidebar">
      <div className="brand-block">
        <div className="brand-logo">T</div>
        <div>
          <div className="brand-name">TalentIQ</div>
          <div className="brand-tag">Recruitment AI</div>
        </div>
      </div>

      <nav className="nav-group">
        {hasRecruiterAccess ? (
          <>
            <div className="nav-label">RECRUITMENT STUDIO</div>
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <LayoutDashboard size={18} />
              <span>Overview</span>
            </NavLink>
            <NavLink to="/jobs" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Briefcase size={18} />
              <span>Job Openings</span>
            </NavLink>
            <NavLink to="/candidates" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Users size={18} />
              <span>Candidates</span>
            </NavLink>

            <div className="nav-label" style={{ marginTop: '16px' }}>AI INTELLIGENCE</div>
            <NavLink to="/resumes" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <FileText size={18} />
              <span>Resume Analyzer</span>
            </NavLink>
            <NavLink to="/interviews" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Sparkles size={18} />
              <span>Interview Generator</span>
            </NavLink>

            {isAdmin && (
              <>
                <div className="nav-label" style={{ marginTop: '16px' }}>ADMIN CONSOLE</div>
                <NavLink to="/admin/recruiters" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <ShieldCheck size={18} />
                  <span>Verify Recruiters</span>
                </NavLink>
              </>
            )}
          </>
        ) : (
          <>
            <div className="nav-label">CANDIDATE PORTAL</div>
            <NavLink to="/portal" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <UserCheck size={18} />
              <span>My Applications</span>
            </NavLink>
            <NavLink to="/jobs" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Compass size={18} />
              <span>Explore Roles</span>
            </NavLink>
          </>
        )}
      </nav>

      {user && (
        <div className="sidebar-user">
          <div className="user-info">
            <div className="user-avatar">
              {user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
            </div>
            <div className="user-details">
              <div className="user-name">{user.fullName}</div>
              <div className="user-role">
                {isAdmin ? 'Administrator' : isRecruiter ? (user.designation || 'Verified Recruiter') : 'Candidate'}
              </div>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="btn btn-ghost"
            style={{ padding: '6px', color: 'var(--text-muted)' }}
            title="Sign out"
          >
            <LogOut size={16} />
          </button>
        </div>
      )}
    </aside>
  );
};
