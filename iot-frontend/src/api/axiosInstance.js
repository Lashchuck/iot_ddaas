import axios from 'axios';

// Tworzenie instancji Axios z adresem URL
const instance = axios.create({
    baseURL: 'http://localhost:8080/iot' // Adres do zapytań API
});

// interceptors.request dodaje nagłówek Authorization, jeśli token jest dostępny
instance.interceptors.request.use(
    (config) => {
        // Pobieranie tokena z LocalStorage
        const token = localStorage.getItem('token');
        if (token) {
            // Jeśli token istnieje, zostaje dodany do nagłówka Authorization
            config.headers.Authorization = `Bearer ${token}`;
            console.log('Authorization header:', config.headers.Authorization);
        }else{
            console.log('No token found in localStorage');
        }
        return config;
    },
    (error) => {
        // Obsługa błędów występujących podczas konfiguracji żądania
        console.error('Request error:',error);
        return Promise.reject(error);
    }
);

// interceptors.response obsługuje odpowiedzi i błędy
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