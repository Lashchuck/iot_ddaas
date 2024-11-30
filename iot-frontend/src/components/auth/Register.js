import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import axios from 'axios';
import {
  TextField,
  Button,
  Grid,
  Box,
  Typography,
  Paper,
  CircularProgress
} from "@mui/material";

function Register() {
    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: ""
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/auth/register', formData);
            console.log('Rejestracja udana', response.data);
            navigate("/login");
        }catch (error) {
            console.error('Błąd rejestracji:', error);
            setError("Wystąpił problem podczas rejestracji");
        } finally {
            setLoading(false);
        }
    };

    return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        background: "linear-gradient(135deg, #FF7A00, #6A1B9A, #00C6FF)",
        backgroundSize: "cover",
        padding: 2,
      }}
    >
      <Paper
        elevation={10}
        sx={{
          width: { xs: "90%", sm: "400px" },
          padding: 4,
          borderRadius: 6,
          boxShadow: 10,
          backgroundColor: "#fff",
          opacity: 0.9,
          backdropFilter: "blur(10px)",
        }}
      >
        <Typography
          variant="h4"
          align="center"
          sx={{
            marginBottom: 3,
            fontWeight: "bold",
            color: "#6A1B9A",
          }}
        >
          Signup
        </Typography>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                name="email"
                variant="outlined"
                value={formData.email}
                onChange={handleChange}
                required
                sx={{ backgroundColor: "#F5F5F5", borderRadius: 2 }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Password"
                type="password"
                name="password"
                variant="outlined"
                value={formData.password}
                onChange={handleChange}
                required
                sx={{ backgroundColor: "#F5F5F5", borderRadius: 2 }}
              />
            </Grid>
            {error && (
              <Grid item xs={12}>
                <Typography variant="body2" color="error" align="center">
                  {error}
                </Typography>
              </Grid>
            )}
            <Grid item xs={12}>
              <Button
                fullWidth
                variant="contained"
                color="primary"
                type="submit"
                disabled={loading}
                sx={{
                  padding: "12px",
                  fontSize: "16px",
                  backgroundColor: "#6A1B9A",
                  "&:hover": {
                    backgroundColor: "#8E24AA",
                  },
                }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : "Signup"}
              </Button>
            </Grid>
            {/* Przycisk do przejścia na stronę logowania */}
            <Grid item xs={12}>
              <Button
                fullWidth
                variant="text"
                color="secondary"
                onClick={() => navigate("/login")}
                sx={{
                  padding: "12px",
                  fontSize: "14px",
                  marginTop: "10px",
                }}
              >
                Have you got an account yet? Log in
              </Button>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
}

export default Register;