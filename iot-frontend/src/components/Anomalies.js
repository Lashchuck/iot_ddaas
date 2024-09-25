import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Box, Typography, CircularProgress, List, ListItem, ListItemText} from '@mui/material';

function Anomalies({ userId }) {
    const [anomalies, setAnomalies] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAnomalies = async () => {
            try {
                const response = await axios.get('http://localhost:8080/iot/anomalies', {
                    params: { userId },
                });
                setAnomalies(response.data);
                setLoading(false);
            }catch (error) {
                console.error("Błąd podczas pobierania anomalii: ", error);
                setLoading(false);
            }
        };

        fetchAnomalies();
    }, []);

    if (loading) {
        return <CircularProgress />;
    }

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Anomalie
            </Typography>
            {anomalies.length > 0 ? (
                <List>
                    {anomalies.map((anomaly) =>  (
                        <ListItem key={anomaly.id}>
                            <ListItemText
                                primary={'Anomalia: ${anomaly.type}'}
                                secondary={'Urządzenie: ${anomaly.deviceId}, Czas: ${anomaly.timestamp}'}
                            />
                        </ListItem>
                    ))}
                </List>
            ) : (
                <Typography>Brak anomalii</Typography>
            )}
        </Box>
    );
}

export default Anomalies;