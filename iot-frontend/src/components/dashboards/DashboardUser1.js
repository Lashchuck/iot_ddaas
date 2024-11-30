import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Line } from "react-chartjs-2";
import { Link } from "react-router-dom";
import {
    AppBar,
    Toolbar,
    Card,
    CardContent,
    Typography,
    Grid,
    Box,
    CircularProgress,
    Button,
    ButtonGroup,
    Tab,
    Tabs,
    List,
    ListItem,
    ListItemText,
    TextField,
} from "@mui/material";
import { format } from 'date-fns';
import GaugeChart from 'react-gauge-chart';
import { DateTimePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFnsV3';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import 'chartjs-adapter-date-fns';
import 'chartjs-adapter-moment'
import { pl } from 'date-fns/locale';
import zoomPlugin from 'chartjs-plugin-zoom';

import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    TimeScale,
} from "chart.js";

// Rejestracja komponentów dla chart.js
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    TimeScale,
    zoomPlugin
);

const DashboardUser1 = () => {
  const [selectedTab, setSelectedTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingAnomalies, setLoadingAnomalies] = useState(false);
  const [historicalData, setHistoricalData] = useState([]);
  const [loadingHistoricalData, setLoadingHistoricalData] = useState(false)
  const [sensorData, setSensorData] = useState([]);
  const [sensor1Value, setSensor1Value] = useState(null);
  const [sensor2Value, setSensor2Value] = useState(null);
  const [anomalies, setAnomalies] = useState([]);
  const [selectedRange, setSelectedRange] = useState(12);
  const [startDate, setStartDate] = useState(new Date());
  const [endDate, setEndDate] = useState(new Date());
  const canvasRef = useRef(null);
  const navigate = useNavigate();

  // Obsługa zmiany zakładek
  const handleTabChange = (_, newValue) => setSelectedTab(newValue);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    navigate("/login")
  };

  // Pobieranie danych z czujników sensor1 i sensor2
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get("http://localhost:8080/iot/soil-moisture/realtime", {
          params: { userId: 1 },
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        });
        setSensorData(response.data);
        setSensor1Value(response.data.at(-1)?.sensor1 || 0);
        setSensor2Value(response.data.at(-1)?.sensor2 || 0);
      } catch (error) {
        console.error("Błąd pobierania danych:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
      if (selectedTab === 1) {
        // Pobieranie anomalii
        setLoadingAnomalies(true);
        axios.get("http://localhost:8080/iot/anomalies", {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        })
          .then((response) => {
            const formattedAnomalies = response.data.map((anomaly) => ({
                    ...anomaly,
                    formattedDate: formatDateForAPI(new Date(anomaly.timestamp))  // Formatowanie daty
                }));
            setAnomalies(response.data);
            setLoadingAnomalies(false);
          })
          .catch((error) => {
            console.error("Błąd podczas ładowania anomalii", error);
            setLoadingAnomalies(false);
          });
      }
  }, [selectedTab]);

   // Funkcja do usuwania anomalii
   const deleteAnomaly = async (id) => {
       const token = localStorage.getItem('token'); // Pobierz token z lokalnego storage
       console.log('Token:', token);
       try {
           const response = await axios.delete(`/iot/anomalies/${id}`, {
               headers: {
                   Authorization: `Bearer ${token}`, // Dodaj token do nagłówka
               },
           });
           console.log(response.data);
           // Aktualizacja stanu po usunięciu anomalii z listy
           setAnomalies(anomalies.filter((anomaly) => anomaly.id !== id));
       } catch (error) {
           console.error('Error deleting anomaly:', error);
       }
   };


   const fetchHistoricalData = async () => {
    setLoadingHistoricalData(true);

    const formattedStartDate = formatDateForAPI(startDate);
    const formattedEndDate = formatDateForAPI(endDate);

    try {
        const userId = localStorage.getItem("userId");
        const response = await axios.get("http://localhost:8080/iot/historical-data", {
            params: {
                userId: userId,
                startDate: formattedStartDate,
                endDate: formattedEndDate,
            },
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        });
        setHistoricalData(response.data);
    } catch (error) {
        console.error("Błąd podczas ładowania danych historycznych", error);
    } finally {
         setLoadingHistoricalData(false);
    }
   };

   useEffect(() => {
          if (selectedTab === 2) {
              fetchHistoricalData();
          }
   }, [selectedTab]);


   const formatDateForAPI = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Miesiące zaczynają się od 0, więc dodajemy 1
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    const second = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
   }

   const handleDateChange = (date, isStartDate) => {
    if (isStartDate) {
        setStartDate(date || new Date());
    } else {
        setEndDate(date || new Date());
    }
   };

   const handleRangeChange = (range) => {
    setSelectedRange(range);
   };


  // Filtrowanie danych na podstawie wybranego zakresu czasu
  const filterDataByRange = (data, hours) => {
    const now = new Date();
    const pastTime = new Date(now.getTime() - hours * 60 * 60 * 1000);

    const filtered = data.filter((entry) => {
        let entryDate;

        if (Array.isArray(entry.timestamp)) {
            const [year, month, day, hour, minute, second] = entry.timestamp;
            entryDate = new Date(year, month - 1, day, hour, minute, second);
        } else if (typeof entry.timestamp === 'string') {
            entryDate = new Date(entry.timestamp);
        } else {
            console.error('Nieznany format timestamp:', entry.timestamp);
            return false;
        }

         const isWithinRange = entryDate >= pastTime && entryDate <= now;
         return isWithinRange;
    });
    return filtered;
  };

  const formatTimestamp = (timestamp) => {
    if (Array.isArray(timestamp)) {
        const [year, month, day, hour, minute, second] = timestamp;
        const date = new Date(year, month - 1, day, hour, minute, second || 0);

        if (isNaN(date.getTime())) {
            console.error('Nieprawidłowa data z tablicy:', timestamp)
            return null;
        }
        return date;
    } else if (typeof timestamp === 'string') {
        const date = new Date(timestamp);

        if (isNaN(date.getTime())) {
            console.error('Nieprawidłowa data z ISO:', timestamp)
            return null;
        }
        return date;
    } else {
        console.error('Nieznany format daty:', timestamp);
        return null;
    }
  };

  // Tworzenie pełnej listy godzin z pustymi wartościami tam, gdzie nie ma danych
  const fillMissingTimestamps = (data) => {
    if (data.length === 0) return [];

    const sortedData = [...data].sort((a, b) =>  {
        const dateA = formatTimestamp(a.timestamp);
        const dateB = formatTimestamp(b.timestamp);
        return dateA - dateB;
    });

    const startTime = formatTimestamp(sortedData[0]?.timestamp);
    const endTime = formatTimestamp(sortedData[sortedData.length - 1]?.timestamp);

    if (!startTime || !endTime) {
        console.error('Niepoprawny zakres czasowy:', startTime, endTime);
        return [];
    }

    const filledData = [];
    const dataMap = new Map(sortedData.map(item => {
        const entryDate = formatTimestamp(item.timestamp);
        return entryDate ? [Math.floor(entryDate.getTime() / 60000) * 60000, item] : null;
    }).filter(entry => entry !== null));

    let currentTime = new Date(startTime);

    while (currentTime <= endTime) {
        const timestampString = currentTime.toISOString();
        const roundedTime = Math.floor(currentTime.getTime() / 60000) * 60000;
        const existingData = dataMap.get(roundedTime);

        if (existingData) {
            filledData.push(existingData);
        } else {
            filledData.push({ timestamp: timestampString, sensor1: null, sensor2: null });
        }
        currentTime.setMinutes(currentTime.getMinutes() + 5);
    }
    console.log('Wypełnione dane:', filledData);
    return filledData;
  }

  const filteredData = fillMissingTimestamps(filterDataByRange(sensorData, selectedRange));

  filteredData.forEach((data, index) => {
    console.log(`Data #${index + 1}`, {
        timestamp: data.timestamp,
        sensor1: data.sensor1,
        sensor2: data.sensor2,
    });
  });


  // Funkcja do zaokrąglania w dół i w górę do najbliższej dziesiątki
  const getRoundedMin = (value) => Math.floor(value / 10) * 10;
  const getRoundedMax = (value) => Math.ceil(value / 10) * 10;

  // Szukanie minimalnej wartości spośród czujników
  const minValue = Math.min(
    ...filteredData.map(data => data.sensor1 ?? Infinity),
    ...filteredData.map(data => data.sensor2 ?? Infinity));

  // Szukanie maksymalnej wartości spośród czujników
   const maxValue = Math.max(
      ...filteredData.map(data => data.sensor1 ?? -Infinity),
      ...filteredData.map(data => data.sensor2 ?? -Infinity));

  // Ustalanie wartości początkowej osi y na podstawie minimalnej i maksymalnej wartości
  const suggestedMin = getRoundedMin(minValue);
  const suggestedMax = getRoundedMax(maxValue);

  useEffect(() => {
      if (canvasRef.current && historicalData && historicalData.length > 0) {
        const filledData = fillMissingTimestamps(historicalData); // Wypełnij brakujące dane

        const filteredDataSensor1 = filledData
          .map((data) => ({
            x: formatTimestamp(data.timestamp),
            y: (data.sensor1 === null || data.sensor1 == undefined) ? null : data.sensor1,
          }))
          .filter((data) => data.y !== null); // Filtruj puste dane

        const filteredDataSensor2 = filledData
          .map((data) => ({
            x: formatTimestamp(data.timestamp),
            y: data.sensor2 === null || data.sensor2 === undefined ? null : data.sensor2,
          }))
          .filter((data) => data.y !== null); // Filtruj puste dane

        const chartInstance = new ChartJS(canvasRef.current, {
          type: 'line',
          data: {
            datasets: [
              {
                label: 'Sensor 1',
                data: filteredDataSensor1,
                borderColor: '#3e95cd',
                fill: false,
                spanGaps: false,
                pointRadius: 3,
                borderWidth: 2,
                tension: 0.4,
              },
              {
                label: 'Sensor 2',
                data: filteredDataSensor2,
                borderColor: '#8e5ea2',
                fill: false,
                showLine: true,
                spanGaps: false,
                pointRadius: 3,
                borderWidth: 2,
                tension: 0.4,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              x: {
                type: 'time',
                time: {
                  unit: 'hour',
                  tooltipFormat: 'DD.MM.yy HH:mm',
                  displayFormats: {
                    hour: 'HH:mm',
                  },
                  adapters: {
                    date: {
                      locale: pl,
                    },
                  },
                },
                title: {
                  display: true,
                  text: 'Time',
                },
              },
              y: {
                title: {
                  display: true,
                  text: 'Moisture %',
                },
              },
            },
            plugins: {
                zoom: {
                    pan: {
                        enabled: true,
                        mode: 'x',
                    },
                    zoom: {
                        wheel: {
                            enabled: true,
                        },
                        pinch: {
                            enabled: true,
                        },
                        mode: 'x',
                    },
                },
            },
          },
        });

        // Zniszczenie wykresu przy odmontowywaniu komponentu
        return () => {
          chartInstance.destroy();
        };
      }
  }, [historicalData]);


  const chartData = {
      labels: filteredData && filteredData.length > 0 ? filteredData.map((data) => {

        const date = formatTimestamp(data.timestamp);
        return date ? date.toLocaleTimeString([], {
            hour: '2-digit', minute: '2-digit'}) : "Brak danych";
      }) : [],

      datasets: [
        {
          label: "Sensor 1",
          data: filteredData && filteredData.length > 0 ? filteredData.map((data) =>
            data.sensor1 ?? null) : [],
          borderColor: "#3e95cd",
          fill: false,
          spanGaps: false,
          tension: 0.4,
        },
        {
          label: "Sensor 2",
          data: filteredData && filteredData.length > 0 ? filteredData.map((data) =>
            data.sensor2 ?? null) : [],
          borderColor: "#8e5ea2",
          fill: false,
          spanGaps: false,
          tension: 0.4,
        },
      ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: "top",
        },
        tooltip: {
            mode: "index",
            intersect: false,
        },
        zoom: {
            pan: {
                enabled: true,
                mode: 'x',
            },
            zoom: {
                pinch: {
                    enabled: true,
                },
                wheel: {
                    enabled: true,
                },
                mode: 'x',
            },
        },
    },
    scales: {
        x: {
            title: {
                display: true,
                text: "Time",
            },
        },
        y: {
            title: {
                display: true,
                text: "Value",
            },
            min: suggestedMin,
            max: suggestedMax,
        },
    },
    elements: {
        line: {
            tension: 0.4,
            borderCapStyle: 'round',
            borderJoinStyle: 'round',
        },
    },
  };

  return (
    <Box>
      {/* Górny pasek nawigacji */}
      <AppBar
        position="static"
        sx={{
          backgroundColor: "#6A1B9A", // Fioletowy pasek
        }}
      >
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Dashboard User 1 - Soil Moisture
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      {/* Zakładki */}
      <Box sx={{ padding: 4 }}>
        <Tabs
          value={selectedTab}
          onChange={handleTabChange}
          centered
          textColor="inherit" // Pozwoli ustawić niestandardowe kolory
          TabIndicatorProps={{
            style: {
              backgroundColor: "#6A1B9A", // Fioletowa linia pod zakładką
            },
          }}
        >
          <Tab
            label="Main"
            sx={{
              color: selectedTab === 0 ? "#6A1B9A" : "gray", // Fioletowy dla aktywnej
              fontWeight: selectedTab === 0 ? "bold" : "normal", // Pogrubienie dla aktywnej
            }}
          />
          <Tab
            label="Anomalies"
            sx={{
              color: selectedTab === 1 ? "#6A1B9A" : "gray",
              fontWeight: selectedTab === 1 ? "bold" : "normal",
            }}
          />
          <Tab
            label="History"
            sx={{
              color: selectedTab === 2 ? "#6A1B9A" : "gray",
              fontWeight: selectedTab === 2 ? "bold" : "normal",
            }}
          />
        </Tabs>

        {/* Zakładka główna */}
        {selectedTab === 0 && (
          <Box>
            {loading ? (
              <CircularProgress />
            ) : (
              <Grid container spacing={3}>
                {/* Główna sekcja */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Box display="flex" justifyContent="flex-end" mb={2}>
                        {/* Przyciski do wyboru zakresu czasu */}
                        <ButtonGroup variant="contained" size="small">
                          <Button
                            onClick={() => handleRangeChange(6)}
                            sx={{
                              backgroundColor: selectedRange === 6 ? "purple" : "gray",
                              color: "white",
                            }}
                          >
                            Last 6 hours
                          </Button>
                          <Button
                            onClick={() => handleRangeChange(12)}
                            sx={{
                              backgroundColor: selectedRange === 12 ? "purple" : "gray",
                              color: "white",
                            }}
                          >
                            Last 12 hours
                          </Button>
                          <Button
                            onClick={() => handleRangeChange(24)}
                            sx={{
                              backgroundColor: selectedRange === 24 ? "purple" : "gray",
                              color: "white",
                            }}
                          >
                            Last 24 hours
                          </Button>
                        </ButtonGroup>
                      </Box>
                      <Box sx={{ height: 400, width: "100%" }}>
                        <Line
                          data={chartData}
                          options={{ responsive: true, maintainAspectRatio: false }}
                        />
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Sensor 1 */}
                <Grid item xs={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1rem" }}>
                        Sensor 1
                      </Typography>
                      <Box
                        display="flex"
                        justifyContent="center"
                        alignItems="center"
                        sx={{ width: "100%", height: "100%" }}
                      >
                        <GaugeChart
                          id="gauge-chart1"
                          nrOfLevels={20}
                          percent={sensor1Value / 100}
                          colors={["#FF5F6D", "#FFC371", "#4CAF50"]}
                          arcWidth={0.3}
                          textColor="#000000"
                          style={{ width: "80%", height: "auto" }}
                        />
                      </Box>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1.2rem" }}>
                        {sensor1Value}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Sensor 2 */}
                <Grid item xs={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1rem" }}>
                        Sensor 2
                      </Typography>
                      <Box
                        display="flex"
                        justifyContent="center"
                        alignItems="center"
                        sx={{ width: "100%", height: "100%" }}
                      >
                        <GaugeChart
                          id="gauge-chart2"
                          nrOfLevels={20}
                          percent={sensor2Value / 100}
                          colors={["#FF5F6D", "#FFC371", "#4CAF50"]}
                          arcWidth={0.3}
                          textColor="#000000"
                          style={{ width: "80%", height: "auto" }}
                        />
                      </Box>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1.2rem" }}>
                        {sensor2Value}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            )}
          </Box>
        )}

        {/* Zakładka "Anomalie" */}
        {selectedTab === 1 && (
          <Box>
            <Typography variant="h5" align="center" gutterBottom style={{ fontSize: "2rem" }}>
              List of anomalies
            </Typography>
            {loadingAnomalies ? (
              <CircularProgress />
            ) : (
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" align="center" style={{ fontSize: "1.5rem" }}></Typography>
                      <List>
                        {anomalies.length > 0 ? (
                          anomalies
                            .filter(anomaly => anomaly.deviceId === "ESP-32-moisture-sensors")
                            .sort((a, b) => {
                              const dateA = Array.isArray(a.timestamp)
                                ? new Date(...a.timestamp)
                                : new Date(a.timestamp);
                              const dateB = Array.isArray(b.timestamp)
                                ? new Date(...b.timestamp)
                                : new Date(b.timestamp);
                              return dateB - dateA; // Sortowanie od najnowszych
                            })
                            .map((anomaly, index) => (
                              <ListItem key={index} divider>
                                <ListItemText
                                  primary={<span style={{ fontSize: "2rem" }}>Anomaly #{index + 1}</span>}
                                  secondary={
                                    <>
                                      <div style={{ fontSize: "1.2rem" }}>
                                        <strong>Date:</strong>{" "}
                                        {Array.isArray(anomaly.timestamp)
                                          ? format(new Date(...anomaly.timestamp), "dd.MM.yyyy HH:mm")
                                          : format(new Date(anomaly.timestamp), "dd.MM.yyyy HH:mm")}
                                      </div>
                                      <div style={{ fontSize: "1.2rem" }}>
                                        <strong>Description:</strong> {anomaly.type || "Brak opisu"}
                                      </div>
                                      <div style={{ fontSize: "1.2rem" }}>
                                        <strong>Device ID:</strong> {anomaly.deviceId}
                                      </div>
                                      <div style={{ fontSize: "1.2rem" }}>
                                        <strong>Value:</strong> {anomaly.wartosc}
                                      </div>
                                    </>
                                  }
                                />
                                {/* Dodanie przycisku do usunięcia */}
                                <button
                                  onClick={() => deleteAnomaly(anomaly.id)}
                                  style={{
                                    marginLeft: "20px",
                                    backgroundColor: "red",
                                    color: "white",
                                    border: "none",
                                    padding: "8px 12px",
                                    cursor: "pointer",
                                    fontSize: "1rem",
                                  }}
                                >
                                  Delete
                                </button>
                              </ListItem>
                            ))
                        ) : (
                          <Typography variant="body2" color="textSecondary" style={{ fontSize: "1.2rem" }}>
                            Brak anomalii.
                          </Typography>
                        )}
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            )}
          </Box>
        )}

        {selectedTab === 2 && (
          <Box>
            <Typography variant="h5" align="center" gutterBottom></Typography>
            {loadingHistoricalData ? (
              <CircularProgress />
            ) : (
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6">Select a date</Typography>
                      <LocalizationProvider dateAdapter={AdapterDateFns}>
                        <Grid container spacing={2}>
                          <Grid item>
                            <DateTimePicker
                              label="Start date"
                              value={startDate}
                              onChange={(date) => setStartDate(date)}
                              renderInput={(params) => <TextField {...params} />}
                              maxDate={endDate}
                              ampm={false}
                            />
                          </Grid>
                          <Grid item>
                            <DateTimePicker
                              label="End date"
                              value={endDate}
                              onChange={(date) => setEndDate(date)}
                              renderInput={(params) => <TextField {...params} />}
                              minDate={startDate}
                              ampm={false}
                            />
                          </Grid>
                          <Grid item>
                            <Button
                              variant="contained"
                              onClick={fetchHistoricalData}
                              sx={{
                                backgroundColor: "#6A1B9A",
                                color: "white",
                                "&:hover": {
                                  backgroundColor: "#8E24AA",
                                },
                                padding: "12px",
                                fontSize: "16px",
                                fontWeight: "bold",
                              }}
                            >
                              Display
                            </Button>
                          </Grid>
                        </Grid>
                      </LocalizationProvider>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Jeśli dane historyczne są dostępne, wyświetl wykres */}
                {historicalData && historicalData.length > 0 && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Box sx={{ height: 400, width: "100%" }}>
                          <canvas ref={canvasRef}></canvas>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </Grid>
            )}
          </Box>
        )}
      </Box>
    </Box>
  );
}

export default DashboardUser1;