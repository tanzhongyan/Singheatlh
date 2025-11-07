import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import apiClient from '../../api/apiClient';

const NowServingPage = () => {
  const { userProfile } = useAuth();
  const [doctors, setDoctors] = useState([]);
  const [servingData, setServingData] = useState({}); // { [doctorId]: { ticketId, queueTicket, loading, error } }
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const previousTicketIds = useRef({}); // Track previous ticket IDs to detect changes
  const audioContext = useRef(null);
  const soundInitialized = useRef(false);

  const clinicId = userProfile?.clinicId;

  // Initialize audio context automatically
  useEffect(() => {
    const initAudio = async () => {
      try {
        if (!audioContext.current && !soundInitialized.current) {
          audioContext.current = new (window.AudioContext || window.webkitAudioContext)();
          
          // Try to resume if suspended
          if (audioContext.current.state === 'suspended') {
            await audioContext.current.resume();
          }
          
          soundInitialized.current = true;
        }
      } catch (err) {
        console.error('Failed to initialize audio:', err);
      }
    };

    // Initialize immediately
    initAudio();

    // Also initialize on any user interaction
    const handleInteraction = () => {
      if (audioContext.current && audioContext.current.state === 'suspended') {
        audioContext.current.resume();
      }
    };

    document.addEventListener('click', handleInteraction, { once: true });
    document.addEventListener('keydown', handleInteraction, { once: true });

    return () => {
      document.removeEventListener('click', handleInteraction);
      document.removeEventListener('keydown', handleInteraction);
    };
  }, []);

  // Play clinic notification sound (two-tone beep)
  const playNotificationSound = useCallback(async () => {
    if (!audioContext.current) {
      return;
    }

    try {
      const ctx = audioContext.current;
      
      // Resume context if suspended
      if (ctx.state === 'suspended') {
        await ctx.resume();
      }
      
      const now = ctx.currentTime;

      // First tone (higher pitch)
      const oscillator1 = ctx.createOscillator();
      const gainNode1 = ctx.createGain();
      
      oscillator1.connect(gainNode1);
      gainNode1.connect(ctx.destination);
      
      oscillator1.frequency.value = 800; // Hz
      oscillator1.type = 'sine';
      
      gainNode1.gain.setValueAtTime(0, now);
      gainNode1.gain.linearRampToValueAtTime(0.3, now + 0.01);
      gainNode1.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
      
      oscillator1.start(now);
      oscillator1.stop(now + 0.3);

      // Second tone (lower pitch) - plays after first tone
      const oscillator2 = ctx.createOscillator();
      const gainNode2 = ctx.createGain();
      
      oscillator2.connect(gainNode2);
      gainNode2.connect(ctx.destination);
      
      oscillator2.frequency.value = 600; // Hz
      oscillator2.type = 'sine';
      
      gainNode2.gain.setValueAtTime(0, now + 0.3);
      gainNode2.gain.linearRampToValueAtTime(0.3, now + 0.31);
      gainNode2.gain.exponentialRampToValueAtTime(0.01, now + 0.6);
      
      oscillator2.start(now + 0.3);
      oscillator2.stop(now + 0.6);
    } catch (err) {
      console.error('Error playing notification sound:', err);
    }
  }, []);

  // Fetch all doctors in the clinic
  const fetchDoctors = useCallback(async () => {
    if (!clinicId) {
      setError('Clinic ID not found. Please contact administrator.');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get(`/api/doctor/clinic/${clinicId}`);
      setDoctors(response.data || []);
    } catch (err) {
      console.error('Error fetching doctors:', err);
      setError('Failed to load doctors. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [clinicId]);

  // Fetch currently serving ticket for a specific doctor
  const fetchServingTicket = useCallback(async (doctorId) => {
    try {
      setServingData((prev) => ({
        ...prev,
        [doctorId]: { ...prev[doctorId], loading: true, error: null },
      }));

      // Get current serving ticket ID
      const currentResponse = await apiClient.get(`/api/queue/current/${doctorId}`);
      const currentTicketId = currentResponse?.data?.currentTicketId;

      if (!currentTicketId || currentTicketId === 0) {
        // No one is currently being served
        setServingData((prev) => ({
          ...prev,
          [doctorId]: {
            ticketId: 0,
            queueTicket: null,
            loading: false,
            error: null,
          },
        }));
        return;
      }

      // Fetch full queue ticket details
      const ticketResponse = await apiClient.get(`/api/queue/ticket/${currentTicketId}`);
      const queueTicket = ticketResponse?.data;

      // Check if ticket ID changed (and it's not the initial load)
      const previousTicketId = previousTicketIds.current[doctorId];
      const hasChanged = previousTicketId !== undefined && 
                        previousTicketId !== currentTicketId && 
                        currentTicketId !== 0;

      // Update previous ticket ID
      previousTicketIds.current[doctorId] = currentTicketId;

      // Play sound if ticket changed
      if (hasChanged) {
        playNotificationSound();
      }

      setServingData((prev) => ({
        ...prev,
        [doctorId]: {
          ticketId: currentTicketId,
          queueTicket,
          loading: false,
          error: null,
        },
      }));
    } catch (err) {
      console.error(`Error fetching serving ticket for doctor ${doctorId}:`, err);
      setServingData((prev) => ({
        ...prev,
        [doctorId]: {
          ...prev[doctorId],
          loading: false,
          error: 'Failed to load current patient',
        },
      }));
    }
  }, [playNotificationSound]);

  // Poll for updates on all doctors
  const pollServingData = useCallback(() => {
    doctors.forEach((doctor) => {
      fetchServingTicket(doctor.doctorId);
    });
  }, [doctors, fetchServingTicket]);

  // Initial fetch of doctors
  useEffect(() => {
    if (userProfile?.clinicId) {
      fetchDoctors();
    }
  }, [userProfile, fetchDoctors]);

  // Fetch serving tickets when doctors are loaded
  useEffect(() => {
    if (doctors.length > 0) {
      pollServingData();
    }
  }, [doctors, pollServingData]);

  // Set up polling interval (every 5 seconds)
  useEffect(() => {
    if (doctors.length === 0) return;

    const interval = setInterval(() => {
      pollServingData();
    }, 5000); // Poll every 5 seconds

    return () => clearInterval(interval);
  }, [doctors, pollServingData]);

  // Calculate responsive column classes based on number of doctors
  // Max 3 columns, 2 rows (6 doctors visible)
  const getColumnClass = () => {
    const numDoctors = Math.min(doctors.length, 6); // Show max 6 doctors
    
    if (numDoctors === 1) {
      return 'col-12'; // 1 doctor: full width
    } else if (numDoctors === 2) {
      return 'col-md-6'; // 2 doctors: 2 columns
    } else if (numDoctors === 3) {
      return 'col-md-4'; // 3 doctors: 3 columns
    } else if (numDoctors === 4) {
      return 'col-md-6 col-lg-6'; // 4 doctors: 2x2 grid
    } else if (numDoctors === 5 || numDoctors === 6) {
      return 'col-md-6 col-lg-4'; // 5-6 doctors: 3 columns, 2 rows
    }
    return 'col-md-6 col-lg-4'; // Default
  };

  if (!userProfile) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 bg-light">
      <div className="container-fluid py-5">
        {/* Error Display */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center" role="alert">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        {/* Loading State */}
        {loading ? (
          <div className="text-center py-5">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <p className="mt-3 text-muted">Loading doctors...</p>
          </div>
        ) : (
          <>
            {/* Doctor Queue Display */}
            {doctors.length === 0 ? (
              <div className="empty-state text-center py-5">
                <i className="bi bi-person-x" style={{ fontSize: '3rem', color: '#dee2e6' }}></i>
                <p className="mt-3 text-muted">No doctors found for this clinic.</p>
              </div>
            ) : (
              <div className="row g-4 justify-content-center">
                {doctors.slice(0, 6).map((doctor, index) => {
                  const serving = servingData[doctor.doctorId];
                  const isServing = serving?.ticketId && serving?.ticketId !== 0;
                  const roomNumber = index + 1; // Generate room number starting from 1

                  return (
                    <div key={doctor.doctorId} className={getColumnClass()}>
                      <div className="card h-100 border shadow-sm">
                        <div className="card-body p-5 text-center" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                          <div>
                            {/* Room Number */}
                            <div className="mb-3">
                              <h2 className="text-primary fw-bold mb-0" style={{ fontSize: '2.5rem' }}>Room {roomNumber}</h2>
                            </div>

                            <hr />

                            {/* Doctor Name */}
                            <div className="mb-4">
                              <h4 className="fw-bold mb-0" style={{ fontSize: '1.75rem' }}>
                                {doctor.name || 'Unknown Doctor'}
                              </h4>
                            </div>

                            <hr />
                          </div>

                          <div>
                            {/* Ticket Number Label */}
                            <div className="mb-3">
                              <small className="text-muted" style={{ fontSize: '1rem' }}>Ticket Number</small>
                            </div>

                            {/* Ticket ID */}
                            {serving?.loading ? (
                              <div className="py-4">
                                <div className="spinner-border text-primary" role="status" style={{ width: '3rem', height: '3rem' }}>
                                  <span className="visually-hidden">Loading...</span>
                                </div>
                              </div>
                            ) : serving?.error ? (
                              <div className="py-4">
                                <p className="text-danger mb-0" style={{ fontSize: '1.25rem' }}>Error loading ticket</p>
                              </div>
                            ) : !isServing ? (
                              <div className="py-4">
                                <h1 className="text-muted mb-0" style={{ fontSize: '8rem', fontWeight: '300' }}>-</h1>
                              </div>
                            ) : (
                              <div className="py-4">
                                <h1 className="fw-bold text-success mb-0" style={{ fontSize: '8rem', letterSpacing: '0.1em' }}>
                                  {String(serving.ticketId).padStart(4, '0')}
                                </h1>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default NowServingPage;

