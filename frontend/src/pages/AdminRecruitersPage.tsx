import React, { useEffect, useState } from 'react';
import { adminApi } from '../api';
import { User, VerificationStatus } from '../types';
import { ShieldCheck, Check, X, AlertTriangle, Building2, Globe, Mail, Phone, MapPin, RefreshCw, Briefcase } from 'lucide-react';

export const AdminRecruitersPage: React.FC = () => {
  const [recruiters, setRecruiters] = useState<User[]>([]);
  const [filter, setFilter] = useState<string>('REVIEW_PENDING');
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [rejectionModalUser, setRejectionModalUser] = useState<User | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const fetchRecruiters = async () => {
    setIsLoading(true);
    try {
      const data = await adminApi.getRecruiters(filter === 'ALL' ? undefined : filter);
      setRecruiters(data);
    } catch (err: any) {
      console.error('Failed to load recruiters:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRecruiters();
  }, [filter]);

  const handleApprove = async (recruiter: User) => {
    if (!window.confirm(`Approve recruiter ${recruiter.fullName} and company ${recruiter.companyName}?`)) {
      return;
    }

    setActionLoading(recruiter.id);
    setStatusMessage(null);
    try {
      await adminApi.approveRecruiter(recruiter.id);
      setStatusMessage(`Recruiter ${recruiter.fullName} (${recruiter.companyName}) approved successfully.`);
      await fetchRecruiters();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve recruiter.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleRejectConfirm = async () => {
    if (!rejectionModalUser || !rejectionReason.trim()) return;

    setActionLoading(rejectionModalUser.id);
    try {
      await adminApi.rejectRecruiter(rejectionModalUser.id, rejectionReason.trim());
      setStatusMessage(`Recruiter ${rejectionModalUser.fullName} application rejected.`);
      setRejectionModalUser(null);
      setRejectionReason('');
      await fetchRecruiters();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject recruiter.');
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div style={{ padding: '32px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span className="badge badge-vermilion" style={{ fontSize: '11px', textTransform: 'uppercase' }}>
              Admin Console
            </span>
          </div>
          <h1 className="editorial-title" style={{ fontSize: '28px', margin: 0 }}>
            Recruiter Organization Verification
          </h1>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Review company credentials, verify domain authenticity, and authorize platform access.
          </p>
        </div>

        <button
          onClick={fetchRecruiters}
          disabled={isLoading}
          className="btn btn-outline btn-sm"
          style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
        >
          <RefreshCw size={14} className={isLoading ? 'spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      {statusMessage && (
        <div style={{
          padding: '12px 16px',
          backgroundColor: '#F0FFF4',
          color: '#22543D',
          borderRadius: 'var(--radius-md)',
          fontSize: '13px',
          marginBottom: '20px',
          border: '1px solid #C6F6D5'
        }}>
          {statusMessage}
        </div>
      )}

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        {[
          { key: 'REVIEW_PENDING', label: 'Pending Review' },
          { key: 'VERIFIED', label: 'Verified / Approved' },
          { key: 'REJECTED', label: 'Rejected' },
          { key: 'ALL', label: 'All Organizations' },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`btn btn-sm ${filter === tab.key ? 'btn-primary' : 'btn-ghost'}`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Recruiter List */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '48px', color: 'var(--text-secondary)' }}>
          Loading recruiter applications...
        </div>
      ) : recruiters.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '48px',
          backgroundColor: 'var(--bg-surface)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border-default)',
          color: 'var(--text-secondary)'
        }}>
          No recruiter applications found for filter: <strong>{filter}</strong>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '16px' }}>
          {recruiters.map((rec) => {
            const isPending = rec.verificationStatus === 'REVIEW_PENDING';
            const isVerified = rec.verificationStatus === 'VERIFIED';
            const isRejected = rec.verificationStatus === 'REJECTED';

            return (
              <div
                key={rec.id}
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  borderRadius: 'var(--radius-lg)',
                  border: '1px solid var(--border-default)',
                  padding: '24px',
                  boxShadow: 'var(--shadow-sm)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                      <h3 style={{ fontSize: '18px', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
                        {rec.companyName || 'Unknown Company'}
                      </h3>
                      <span className={`badge ${
                        isVerified ? 'badge-vermilion' : isPending ? 'badge-blue' : 'badge-gold'
                      }`} style={{ fontSize: '11px' }}>
                        {rec.verificationStatus}
                      </span>
                      {rec.emailVerified && (
                        <span style={{ fontSize: '11px', color: '#38A169', display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <Check size={12} /> Email Confirmed
                        </span>
                      )}
                    </div>

                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', fontSize: '13px', color: 'var(--text-secondary)', marginTop: '8px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Briefcase size={14} color="var(--text-muted)" />
                        <strong>{rec.fullName}</strong> ({rec.designation || 'Recruiter'})
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Mail size={14} color="var(--text-muted)" />
                        <a href={`mailto:${rec.email}`} style={{ color: 'inherit' }}>{rec.email}</a>
                      </div>
                      {rec.phone && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <Phone size={14} color="var(--text-muted)" />
                          <span>{rec.phone}</span>
                        </div>
                      )}
                      {rec.companyWebsite && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <Globe size={14} color="var(--accent-primary)" />
                          <a href={rec.companyWebsite.startsWith('http') ? rec.companyWebsite : `https://${rec.companyWebsite}`} target="_blank" rel="noreferrer" style={{ color: 'var(--accent-primary)', fontWeight: 500 }}>
                            {rec.companyWebsite}
                          </a>
                        </div>
                      )}
                    </div>

                    {rec.companyAddress && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-muted)', marginTop: '8px' }}>
                        <MapPin size={13} />
                        <span>{rec.companyAddress}</span>
                      </div>
                    )}

                    {rec.rejectionReason && (
                      <div style={{ marginTop: '10px', fontSize: '12px', color: '#E53E3E', backgroundColor: '#FFF5F5', padding: '6px 10px', borderRadius: '4px' }}>
                        <strong>Rejection Reason:</strong> {rec.rejectionReason}
                      </div>
                    )}
                  </div>

                  {/* Actions */}
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {!isVerified && (
                      <button
                        onClick={() => handleApprove(rec)}
                        disabled={actionLoading === rec.id}
                        className="btn btn-primary btn-sm"
                        style={{ display: 'flex', alignItems: 'center', gap: '6px', backgroundColor: '#38A169', borderColor: '#38A169' }}
                      >
                        <Check size={14} />
                        <span>Approve</span>
                      </button>
                    )}
                    {!isRejected && (
                      <button
                        onClick={() => {
                          setRejectionModalUser(rec);
                          setRejectionReason('');
                        }}
                        disabled={actionLoading === rec.id}
                        className="btn btn-outline btn-sm"
                        style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#E53E3E', borderColor: '#FED7D7' }}
                      >
                        <X size={14} />
                        <span>Decline</span>
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Decline Reason Modal */}
      {rejectionModalUser && (
        <div style={{
          position: 'fixed',
          inset: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            maxWidth: '460px',
            width: '100%',
            backgroundColor: 'var(--bg-surface)',
            borderRadius: 'var(--radius-lg)',
            padding: '24px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '6px' }}>
              Decline Application: {rejectionModalUser.companyName}
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Provide clear feedback for why this recruiter account was not approved. The applicant will see this feedback.
            </p>

            <textarea
              rows={4}
              className="form-input"
              placeholder="e.g., Company domain could not be verified; work email does not match organization domain."
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              style={{ width: '100%', marginBottom: '16px' }}
            />

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button
                onClick={() => setRejectionModalUser(null)}
                className="btn btn-ghost btn-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleRejectConfirm}
                disabled={!rejectionReason.trim() || actionLoading === rejectionModalUser.id}
                className="btn btn-primary btn-sm"
                style={{ backgroundColor: '#E53E3E', borderColor: '#E53E3E' }}
              >
                Confirm Decline
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default AdminRecruitersPage;

