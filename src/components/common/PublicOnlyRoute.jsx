import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export default function PublicOnlyRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <div className="flex h-64 items-center justify-center text-primary-600">Loading...</div>;
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : children;
}
