import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
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
