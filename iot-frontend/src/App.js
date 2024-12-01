import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import Home from './components/Home';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import DashboardUser1 from './components/dashboards/DashboardUser1';
import DashboardUser2 from './components/dashboards/DashboardUser2';

const theme = createTheme({
    palette: {
        primary: {
            main: '#1976d2',
        },
        secondary: {
            main: '#dc004e',
        },
    },
});

function App() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard-user1" element={<DashboardUser1 />} />
            <Route path="/dashboard-user2" element={<DashboardUser2 />} />
        </Routes>
    );
}

export default App;