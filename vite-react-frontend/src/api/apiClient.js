import axios from 'axios';

// Use environment variable for backend URL, with fallback for development
// In production, this will be set during build via VITE_BACKEND_URL
const apiClient = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json'
    }
});

// You can add a request interceptor to include the auth token
apiClient.interceptors.request.use(config => {
    // In a real app, you would get the token from your auth context or local storage
    const token = localStorage.getItem('supabase_jwt'); 
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

export default apiClient;
