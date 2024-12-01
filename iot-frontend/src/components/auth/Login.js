import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
    TextField,
    Button,
    Grid,
    Box,
    Typography,
    Paper,
    CircularProgress
} from "@mui/material";

function Login(){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Obsługa wysłania formularza logowania
    const handleLogin = async (e) => {
        e.preventDefault(); // Zatrzymanie przeładowania strony przy wysyłąniu formularza
        try {
            // Wysyłanie żądania POST do API logowania
            const response = await axios.post("http://localhost:8080/auth/login", {
                email,
                password,
            });

            // Sprawdzanie czy logowanie zakończyło się sukcesem
            if (response.status === 200) {
                const { token } = response.data; // Pobieranie tokena z odpowiedzi
                const { user } = response.data; // Pobieranie danych użytkownika z odpowiedzi
                const userId = user.id;

                // Zapisanie tokena i userId do LocalStorage
                localStorage.setItem("token", token);
                localStorage.setItem("userId", userId);

                // Przekierowanie użytkownika na odpowiedni dashboard w zależności od emaila
                if (email === "matiif380@gmail.com") {
                    navigate("/dashboard-user1");
                } else if (email === "mateusz.laszczak19@gmail.com") {
                    navigate("/dashboard-user2");
                } else {
                    navigate("/");
                }
            }
        } catch (err) {
            console.error("Failed login", err);
            setError("Incorrect email or password");
        } finally {
            setLoading(false); // Ustawienie stanu ładowania na false
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
      {/* Karta formularza logowania */}
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
          Log In
        </Typography>
        <form onSubmit={handleLogin}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                variant="outlined"
                value={email}
                onChange={(e) => setEmail(e.target.value)} // Aktualizacja email
                required
                sx={{ backgroundColor: "#F5F5F5", borderRadius: 2 }}
              />
            </Grid>
            {/* Pole hasła */}
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Password"
                type="password"
                variant="outlined"
                value={password}
                onChange={(e) => setPassword(e.target.value)} // Aktualizacja hasła
                required
                sx={{ backgroundColor: "#F5F5F5", borderRadius: 2 }}
              />
            </Grid>
            {/* Wyświetlanie błędu logowania */}
            {error && (
              <Grid item xs={12}>
                <Typography variant="body2" color="error" align="center">
                  {error}
                </Typography>
              </Grid>
            )}
            {/* Przycisk logowania */}
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
                {loading ? <CircularProgress size={24} color="inherit" /> : "Log in"}
              </Button>
            </Grid>
            {/* Przycisk do rejestracji */}
            <Grid item xs={12}>
              <Button
                fullWidth
                variant="text"
                color="secondary"
                onClick={() => navigate("/register")} // Przekierowanie do rejestracji
                sx={{
                  padding: "12px",
                  fontSize: "14px",
                  marginTop: "10px",
                }}
              >
                Don't have an account? Sign up
              </Button>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
}

export default Login;