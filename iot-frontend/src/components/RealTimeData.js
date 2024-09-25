import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Box, Typography, CircularProgress} from '@mui/material';

function RealTimeData({ userId }) {
  const [iotData, setIotData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get('http://localhost:8080/iot/data', {
          params: { userId }, //  Pobieranie na podstawie userId
        });
        setIotData(response.data);
        setLoading(false);
      } catch (error) {
        console.error('Błąd podczas pobierania danych:', error);
        setLoading(false);
      }
    };

    // Wywołanie fetchData co 30 sekund
    const interval = setInterval(() => {
      fetchData();
    }, 30000);

    // Czyszczenie interwału
    return () => clearInterval(interval);
  }, [userId]);

  if (loading) {
    return <CircularProgress />;
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Dane w czasie rzeczywistym dla użytkownika {userId}
      </Typography>
      {iotData ? (
        <Box>
          <Typography variant="h6">Sensor 1: {iotData.sensor1}</Typography>
          <Typography variant="h6">Sensor 2: {iotData.sensor2}</Typography>
          <Typography variant="h6">Temperatura: {iotData.temperatureSensor}</Typography>
        </Box>
      ) : (
        <Typography>Brak danych</Typography>
      )}
    </Box>
  );
}

export default RealTimeData;

