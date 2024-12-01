import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { Line } from "react-chartjs-2";
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
import { DateTimePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFnsV3';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import 'chartjs-adapter-date-fns';
import 'chartjs-adapter-moment'
import { pl } from 'date-fns/locale';

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
ChartJS.defaults.locale = "en-US";

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
);

  const Thermometer = ({ temperature }) => {
    // Ustal wysokość termometru jako procent zależny od temperatury
    const thermometerHeight = `${(temperature / 50) * 100}%`; // Skala 50°C na górze, 0°C na dole

    // Funkcja do określania kolorów w zależności od temperatury
    const getTemperatureColor = (value) => {
      if (value >= 30) {
        return 'linear-gradient(to top, #FF4500, #FF0000)'; // Czerwony na górze (wysoka temperatura)
      } else if (value >= 20) {
        return 'linear-gradient(to top, #FF7F00, #FF4500)'; // Pomarańczowy
      } else if (value >= 10) {
        return 'linear-gradient(to top, #FFFF00, #FFD700)'; // Żółty
      } else {
        return 'linear-gradient(to top, #ADD8E6, #0000FF)'; // Niebieski na dole (niska temperatura)
      }
    };

    return (
      <div style={{
        width: '100px',
        height: '300px',
        border: '5px solid #000',
        borderRadius: '20px',
        position: 'relative',
        backgroundColor: '#f0f0f0',
        boxShadow: '0px 0px 20px rgba(0, 0, 0, 0.3)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-end',
        margin: '0 auto',
        position: 'relative'
      }}>
        {/* Wypełnienie termometru */}
        <div
          style={{
            width: '100%',
            height: thermometerHeight,
            background: getTemperatureColor(temperature),
            position: 'absolute',
            bottom: 0,
            borderRadius: '15px',
            transition: 'all 0.3s ease', // Płynne animacje
          }}
        />

        {/* Linie poziome */}
        <div style={{
          position: 'absolute',
          top: '20%',
          left: 0,
          width: '100%',
          borderTop: '1px solid black'
        }} />
        <div style={{
          position: 'absolute',
          top: '40%',
          left: 0,
          width: '100%',
          borderTop: '1px solid black'
        }} />
        <div style={{
          position: 'absolute',
          top: '60%',
          left: 0,
          width: '100%',
          borderTop: '1px solid black'
        }} />
        <div style={{
          position: 'absolute',
          top: '80%',
          left: 0,
          width: '100%',
          borderTop: '1px solid black'
        }} />

        {/* Numery temperatury równolegle do linii */}
        <div style={{
         position: 'absolute',
         top: '0%',
         left: '110%',
         color: 'black',
         fontSize: '12px'
        }}>
         50°C
        </div>
        <div style={{
          position: 'absolute',
          top: '20%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          40°C
        </div>
        <div style={{
          position: 'absolute',
          top: '40%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          30°C
        </div>
        <div style={{
          position: 'absolute',
          top: '60%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          20°C
        </div>
        <div style={{
          position: 'absolute',
          top: '80%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          10°C
        </div>
        <div style={{
          position: 'absolute',
          top: '100%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          0°C
        </div>
        <div style={{
          position: 'absolute',
          top: '120%',
          left: '110%',
          color: 'black',
          fontSize: '12px'
        }}>
          0°C
        </div>
      </div>
    );
  };

const DashboardUser2 = () => {
  const [selectedTab, setSelectedTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingAnomalies, setLoadingAnomalies] = useState(false);
  const [historicalData, setHistoricalData] = useState([]);
  const [loadingHistoricalData, setLoadingHistoricalData] = useState(false)
  const [temperature, setTemperature] = useState(null);
  const [sensorData, setSensorData] = useState([]);
  const [sensorValue, setSensorValue] = useState(null);
  const [anomalies, setAnomalies] = useState([]);
  const [selectedRange, setSelectedRange] = useState(12);
  const [startDate, setStartDate] = useState(new Date());
  const [endDate, setEndDate] = useState(new Date());
  const [selectedDeviceId, setSelectedDeviceId] = useState(null);
  const canvasRef = useRef(null);
  const navigate = useNavigate();

  // Obsługa zmiany zakładek
  const handleTabChange = (_, newValue) => setSelectedTab(newValue);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    navigate("/login")
  };

  useEffect(() => {
      const fetchData = async () => {
      setLoading(true);
      try {
        const response = await axios.get("http://localhost:8080/iot/temperature/realtime", {
          params: { userId: 2 },
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        });
        setSensorData(response.data);
        setSensorValue(response.data.at(-1)?.temperatureSensor || 0);
      } catch (error) {
        console.error("Temperature download error:", error);
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
              setAnomalies(response.data);
              setLoadingAnomalies(false);
            })
            .catch((error) => {
              console.error("Error while loading anomalies", error);
              setLoadingAnomalies(false);
            });
        }
  }, [selectedTab]);


  const deleteAnomaly = async (id) => {
         const token = localStorage.getItem('token'); // Pobieranie tokenu z lokalnego storage
         console.log('Token:', token);
         try {
             const response = await axios.delete(`/iot/anomalies/${id}`, {
                 headers: {
                     Authorization: `Bearer ${token}`, // Dodawanie tokenu do nagłówka
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
          console.error("Error while loading historical data", error);
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
      const month = String(date.getMonth() + 1).padStart(2, '0'); // Miesiące zaczynają się od 0, trzeba dodać 1
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
              console.error('Unknown timestamp format:', entry.timestamp);
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
              console.error('Incorrect date from the array:', timestamp)
              return null;
          }
          return date;
      } else if (typeof timestamp === 'string') {
          const date = new Date(timestamp);

          if (isNaN(date.getTime())) {
              console.error('Incorrect date from ISO:', timestamp)
              return null;
          }
          return date;
      } else {
          console.error('Unknown date format:', timestamp);
          return null;
      }
  };

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
          console.error('Incorrect time range:', startTime, endTime);
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
              filledData.push({ timestamp: timestampString, temperatureSensor: null });
          }
          currentTime.setMinutes(currentTime.getMinutes() + 5);
      }
      console.log('Filled data:', filledData);
      return filledData;
  }

  const filteredData = fillMissingTimestamps(filterDataByRange(sensorData, selectedRange));

  filteredData.forEach((data, index) => {
      console.log(`Data #${index + 1}`, {
          timestamp: data.timestamp,
          temperatureSensor: data.temperatureSensor,
      });
  });

  useEffect(() => {
        if (canvasRef.current && historicalData && historicalData.length > 0) {
          const filledData = fillMissingTimestamps(historicalData); // Wypełnienie brakujących danych

          const filteredDataSensor = filledData
            .map((data) => ({
              x: formatTimestamp(data.timestamp),
              y: (data.temperatureSensor === null || data.temperatureSensor == undefined) ? null : data.temperatureSensor,
            }))
            .filter((data) => data.y !== null); // Filtrowanie pustych danych

          const chartInstance = new ChartJS(canvasRef.current, {
            type: 'line',
            data: {
              datasets: [
                {
                  label: 'Temperature',
                  data: filteredDataSensor,
                  borderColor: '#3e95cd',
                  fill: false,
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
                    text: 'Temperature',
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

          // Zniszczenie wykresu
          return () => {
            chartInstance.destroy();
          };
        }
  }, [historicalData]);

  const chartData = {
        labels: filteredData && filteredData.length > 0 ? filteredData.map((data) => {

          const date = formatTimestamp(data.timestamp);
          return date ? date.toLocaleTimeString([], {
              hour: '2-digit', minute: '2-digit'}) : "No data available";
        }) : [],

        datasets: [
          {
            label: "Temperature",
            data: filteredData && filteredData.length > 0 ? filteredData.map((data) =>
              data.temperatureSensor ?? null) : [],
            borderColor: "#3e95cd",
            fill: false,
            spanGaps: false,
            tension: 0.4,
          },
        ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'hour',
          tooltipFormat: 'dd.MM.yyy HH:mm',
          displayFormats: {
            hour: 'HH:mm',
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
          text: 'Temperature (°C)',
        },
        min: 0,
        max: 50,
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
            Dashboard User 2 - Temperature
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
          textColor="inherit"
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
                        <Line data={chartData} options={{ responsive: true, maintainAspectRatio: false }} />
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Termometr */}
                <Grid item xs={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1rem" }}>
                        Current temperature
                      </Typography>
                      <Box display="flex" justifyContent="center" alignItems="center" sx={{ width: "100%", height: "100%" }}>
                        <Thermometer temperature={sensorValue} />
                      </Box>
                      <Typography variant="h6" align="center" sx={{ fontSize: "1.2rem" }}>
                        {sensorValue}°C
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
                      <List>
                        {anomalies.length > 0 ? (
                          anomalies
                            .filter(anomaly => anomaly.deviceId === "ESP8266-temperature-sensor")
                            .sort((a, b) => {
                              const dateA = Array.isArray(a.timestamp) ? new Date(...a.timestamp) : new Date(a.timestamp);
                              const dateB = Array.isArray(b.timestamp) ? new Date(...b.timestamp) : new Date(b.timestamp);
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
                                        <strong>Description:</strong> {anomaly.type || "No description"}
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
                            No anomalies.
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

        {/* Zakładka "Historia" */}
        {selectedTab === 2 && (
          <Box>
            <Typography variant="h5" align="center" gutterBottom>
            </Typography>
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

export default DashboardUser2;