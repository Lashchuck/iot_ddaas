import React from 'react';
import { useNavigate } from 'react-router-dom';

const Home = () => {
    const navigate = useNavigate();

    const handleLoginClick = () => {
        navigate('/login'); // Przekierowanie do strony logowania
    };

    const handleRegisterClick = () => {
        navigate('/register'); // Przekierowanie do strony rejestracji
    };

    return (
        <div>
            <h1>Welcome to the Home Page!</h1>
            <button onClick={handleLoginClick}>Zaloguj się</button>
            <button onClick={handleRegisterClick}>Zarejestruj</button>
        </div>
    );
};

export default Home;