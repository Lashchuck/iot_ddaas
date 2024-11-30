import React from "react";
import { useNavigate } from "react-router-dom";
import { Button, Box, Typography } from "@mui/material";

function Home() {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    navigate("/login");
  };

  const handleRegisterClick = () => {
    navigate("/register");
  };

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        flexDirection: "column",
        textAlign: "center",
        background: "linear-gradient(135deg, #FF7A00, #6A1B9A, #00C6FF)",
        backgroundSize: "cover",
      }}
    >
      <Typography
        variant="h3"
        sx={{
          color: "#fff",
          fontWeight: "bold",
          marginBottom: 4,
        }}
      >
        Welcome to the Home Page!
      </Typography>
      <Box sx={{ display: "flex", gap: 2 }}>
        <Button
          variant="contained"
          color="primary"
          sx={{
            fontSize: "16px",
            padding: "12px 24px",
            backgroundColor: "#6A1B9A",
            "&:hover": {
              backgroundColor: "#8E24AA",
            },
          }}
          onClick={handleLoginClick}
        >
          Log In
        </Button>
        <Button
          variant="contained" // Zmieniamy na 'contained', aby miał tło
          sx={{
            fontSize: "16px",
            padding: "12px 24px",
            backgroundColor: "#6A1B9A", // Tło fioletowe
            color: "#fff", // Tekst biały
            "&:hover": {
              backgroundColor: "#8E24AA", // Zmiana tła po najechaniu
            },
          }}
          onClick={handleRegisterClick}
        >
          Sign Up
        </Button>
      </Box>
    </Box>
  );
}

export default Home;