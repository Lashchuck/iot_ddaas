import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080/iot'
});

instance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('Authorization header:', config.headers.Authorization);
        }else{
            console.log('No token found in localStorage');
        }
        return config;
    },
    (error) => {
        console.error('Request error:',error);
        return Promise.reject(error);
    }
);

instance.interceptors.response.use(
    (response) => {
        console.log('Response data:', response.data);
        return response;
    },
    (error) => {
        console.error('Response error', error.response ? error.response.data : error.message);
        return Promise.reject(error);
    }
);

export default instance;