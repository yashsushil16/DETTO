import React, { createContext, useContext, useState, useEffect } from 'react';
import { initializeApp } from 'firebase/app';
import { getAuth, onAuthStateChanged, signInWithEmailAndPassword, createUserWithEmailAndPassword, signOut } from 'firebase/auth';

// Firebase config for the WEB app.
// IMPORTANT: In Firebase Console → Project Settings → Your Apps → Web App
// Copy the firebaseConfig object and paste here to replace this one.
const firebaseConfig = {
  apiKey: "AIzaSyDN3-xpCiPmXyfUYZcEE2_1MF2GT07BTp8",
  authDomain: "detto-8e2d5.firebaseapp.com",
  projectId: "detto-8e2d5",
  storageBucket: "detto-8e2d5.firebasestorage.app",
  messagingSenderId: "1003767564581",
  appId: "1:1003767564581:web:034f4fffa93bba960bf2cc" // Get from Firebase Console → Add Web App
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  function login(email, password) {
    return signInWithEmailAndPassword(auth, email, password);
  }

  function signup(email, password) {
    return createUserWithEmailAndPassword(auth, email, password);
  }

  function logout() {
    return signOut(auth);
  }

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, user => {
      setCurrentUser(user);
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const value = {
    currentUser,
    login,
    signup,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}
