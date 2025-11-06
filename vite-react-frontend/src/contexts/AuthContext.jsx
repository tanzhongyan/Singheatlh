import React, { createContext, useContext, useEffect, useState } from "react";
import apiClient from "../api/apiClient";
import { supabase } from "../lib/supabaseClient";

const AuthContext = createContext({});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userProfile, setUserProfile] = useState(null);

  useEffect(() => {
    const getSession = async () => {
      const {
        data: { session },
      } = await supabase.auth.getSession();
      setUser(session?.user ?? null);
      setLoading(false);
    };

    getSession();

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(async (event, session) => {
      setUser(session?.user ?? null);
      if (session?.user) {
        // Fetch user profile from backend
        try {
          const response = await apiClient.get(`/api/users/${session.user.id}`);
          setUserProfile(response.data);
        } catch (error) {
          console.error("Failed to fetch user profile:", error);
        }
      }
      setLoading(false);
    });

    return () => subscription.unsubscribe();
  }, []);

  const signUpAsAdmin = async (data) => {
    // Get current session to restore later
    const { data: { session: currentSession } } = await supabase.auth.getSession();

    // Create the new user (this will auto-login as the new user)
    const result = await supabase.auth.signUp(data);

    // If we had a session before, restore it
    if (currentSession) {
      // Sign out the newly created user
      await supabase.auth.signOut();

      // Restore the admin session
      await supabase.auth.setSession({
        access_token: currentSession.access_token,
        refresh_token: currentSession.refresh_token,
      });
    }

    return result;
  };

  const value = {
    signUp: (data) => supabase.auth.signUp(data),
    signUpAsAdmin,
    signIn: (data) => supabase.auth.signInWithPassword(data),
    signOut: () => supabase.auth.signOut(),
    user,
    loading,
    userProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
