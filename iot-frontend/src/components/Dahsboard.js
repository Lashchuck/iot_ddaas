import React, { useState } from "react";
import { Tabs, Tab, Box } from '@mui/material';
import RealTimeData from './RealTimeData';
import Anomalies from './Anomalies';
import HistoricalData from './HistoricalData';

function Dashboard({ userId }) {
    const [currentTab, setCurrentTab] = useState(0);

    const handleTabChange = (event, newValue) => {
        setCurrentTab(newValue);
    };

    return (
       <Box sx={{ width: '100%' }}>
        <Tabs value={currentTab} onChange={handleTabChange} centered>
            <Tab label="Dane w czasie rzeczywistym" />
            <Tab label="Anomalie" />
            <Tab label="Dane historyczne" />
        </Tabs>
        {currentTab === 0 && <RealTimeData userId={userId} />}
        {currentTab === 1 && <Anomalies userId={userId} />}
        {<currentTab === 2 && <HistoricalData userId={userId} />}
       </Box>
    );
}

export default Dashboard;

