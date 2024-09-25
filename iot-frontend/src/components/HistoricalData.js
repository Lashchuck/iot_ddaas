import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Box, Typography, CircularProgress, TextField, Button } from '@mui/material';
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip } from 'recharts';

function HistoricalData({ userId }) {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchHistoricalData = async () => {
        setLoading(true);
        try {
            const response = await axios.get('http://localhost:8080/iot/historical-data', {
                params: { userId, startDate, endDate},
            });
            setData(response.data);
            setLoading(false);
        }catch (error) {
            console.error('Błąd podczas pobierania danych historycznych: ', error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistoricalData();
    }, []);

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Dane historyczne
            </Typography>
            <Box display="flex" gap={2} mb={2}>
                <TextField
                    label="Początkowa data"
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    InputLabelProps={{ shrink : true }}
                />
                <TextField
                    label="Końcowa data"
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    />
                    <Button variant="contained" onClick={handleFetch}>
                        Pokaż dane
                    </Button>
            </Box>
            {loading ? (
                <CircularProgress />
            ) : (
                <LineChart width={600} height={300} data={data}>
                    <Line type="monotone" dataKey="sensor1" stroke="#8884d8" />
                    <Line type="monotone" dataKey="sensor2" stroke="#82ca9d" />
                    <Line type="monotone" dataKey="temperatureSensor" stroke="#ffc658" />
                    <CartesianGrid stroke="#ccc" />
                    <XAxis dataKey="timestamp" />
                    <YAxis />
                    <Tooltip />
                </LineChart>
            )}
        </Box>
    );
}

export default HistoricalData;