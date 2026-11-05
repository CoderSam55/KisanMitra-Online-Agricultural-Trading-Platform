import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import { useAuth } from './AuthContext';

const SocketContext = createContext();

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const { isAuthenticated, token } = useAuth();

  useEffect(() => {
    if (isAuthenticated && token) {
      const configuredApi = import.meta.env.VITE_API_BASE_URL;
      const socketUrl = import.meta.env.VITE_SOCKET_URL ||
        (configuredApi?.startsWith('http') ? configuredApi.replace(/\/api\/?$/, '') : null) ||
        import.meta.env.VITE_API_TARGET ||
        'http://localhost:5000';
      const newSocket = new Client({ brokerURL: `${socketUrl.replace(/^http/, 'ws')}/ws`, connectHeaders: { Authorization: `Bearer ${token}` }, reconnectDelay: 5000 });

      newSocket.onConnect = () => {
        setIsConnected(true);
      };

      newSocket.onDisconnect = () => {
        setIsConnected(false);
      };

      newSocket.activate();
      setSocket(newSocket);

      return () => {
        newSocket.deactivate();
      };
    } else if (socket) {
      socket.deactivate();
      setSocket(null);
      setIsConnected(false);
    }
  }, [isAuthenticated, token]);

  const emit = useCallback((event, data) => {
    if (socket) {
      socket.publish({ destination: `/app/${event}`, body: JSON.stringify(data) });
    }
  }, [socket]);

  const on = useCallback((event, callback) => {
    if (socket) {
      return socket.subscribe(`/topic/${event}`, (message) => callback(JSON.parse(message.body)));
    }
  }, [socket]);

  const off = useCallback((event) => {
    if (socket) {
      socket.off(event);
    }
  }, [socket]);

  return (
    <SocketContext.Provider value={{ socket, isConnected, emit, on, off }}>
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => useContext(SocketContext);
