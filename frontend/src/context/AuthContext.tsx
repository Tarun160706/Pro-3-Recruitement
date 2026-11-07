import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { User, CandidateRegisterPayload, RecruiterRegisterPayload, MessageResponse } from '../types';
import { authApi } from '../api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<User>;
  registerCandidate: (payload: CandidateRegisterPayload) => Promise<MessageResponse>;
  registerRecruiter: (payload: RecruiterRegisterPayload) => Promise<MessageResponse>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<User | null>;
  isRecruiter: boolean;
  isPendingRecruiter: boolean;
  isRejectedRecruiter: boolean;
  isCandidate: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUser = useCallback(async (): Promise<User | null> => {
    try {
      const currentUser = await authApi.getCurrentUser();
      setUser(currentUser);
      return currentUser;
    } catch {
      setUser(null);
      return null;
    }
  }, []);

  useEffect(() => {
    const initAuth = async () => {
      await refreshUser();
      setIsLoading(false);
    };
    initAuth();
  }, [refreshUser]);

  const login = async (email: string, password: string): Promise<User> => {
    const res = await authApi.login(email, password);
    setUser(res.user);
    return res.user;
  };

  const registerCandidate = async (payload: CandidateRegisterPayload): Promise<MessageResponse> => {
    return await authApi.registerCandidate(payload);
  };

  const registerRecruiter = async (payload: RecruiterRegisterPayload): Promise<MessageResponse> => {
    return await authApi.registerRecruiter(payload);
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {
      // Ignore network errors on logout
    } finally {
      setUser(null);
    }
  };

  const isRecruiter = user?.role === 'ROLE_RECRUITER' && user?.verificationStatus === 'VERIFIED';
  const isPendingRecruiter = user?.role === 'ROLE_RECRUITER' && user?.verificationStatus === 'REVIEW_PENDING';
  const isRejectedRecruiter = user?.role === 'ROLE_RECRUITER' && user?.verificationStatus === 'REJECTED';
  const isCandidate = user?.role === 'ROLE_CANDIDATE' && !!user?.emailVerified;
  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        login,
        registerCandidate,
        registerRecruiter,
        logout,
        refreshUser,
        isRecruiter,
        isPendingRecruiter,
        isRejectedRecruiter,
        isCandidate,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
