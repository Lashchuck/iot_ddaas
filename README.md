# IoT DDAAS (Data Detection and Anomaly System of IoT Devices)

**Project Description**

The goal of this project is to develop a system for detecting and analyzing anomalies in data collected from IoT devices. The system gathers data from microcontrollers (ESP32 - 2 soil moisture sensors, ESP8266 - temperature sensor),  identifies anomalies, and generates reports and notifications. Project is useful for detecting irregularities such as device malfunctions, environmental changes, or suspicious activity, making it ideal for monitoring IoT infrastructures.


## Technologies

**Backend (Spring Boot)**

- **Java:** Main programming language.
- **Spring Boot:** Backend development, dependency management and configuration.
- **Docker:** Application containerization.
- **HTTP/REST:** Communication between IoT devices and the server.
- **PostgreSQL:** Storing IoT device data and user information.
- **Unit/Integration Testing:** Ensuring code quality and system correctness (JUnit, Mockito).
---------
**Frontend (React)**

- **React:** JavaScript library for building user interfaces.
- **JWT Authentication:** Secure user authentication and session management.
- **Axios:** For making HTTP requests to the backend API.
- **Material-UI (MUI):** UI component library for building responsive, modern user interfaces.


## System Features

- **Data Collection:** Integration with IoT devices to collect real-time data.
- **Anomaly Detection:** Algorithms for identifying irregularities in the data.
- **Application status monitoring:** Utilization of Spring Boot Actuator for monitoring application health, database availability and container status within Docker Desktop.
- **Notifications:** Informing users of detected anomalies via email using Sendinblue integration.

## System Architecture

**Backend**

- **Spring Boot:** Main framework for building the application.
- **REST API:** Communication interface between the frontend and backend.
- **Modules:**

    - Data collection from IoT devices.
    - Anomaly detection.
    - Notification handling (email).

- **PostgreSQL:** Database to store IoT data and anomaly information.
#

**Containerization**

- **Docker:** Used for containerizing the application and data base.
#
**Testing**

- **JUnit, Mockito:** Frameworks used for unit testing services and controllers, validating that methods: 

    - return and save data.
    - return a list of anomalies.
    - remove data based on ID.
    - remove an anomaly based on ID.
- **Integration Testing:** Ensures proper interaction between system components by:

    - Testing logic at the service layer (repositories). Testing operations for writing and reading data from tables in the database.
    - Testing the operation and functionality of the API controller (IoTController) and the correctness of HTTP responses.
- **GitHub Actions:** Automated testing via CI/CD.

## 🔗 Link to microcontrollers codes
[![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Lashchuck/iot_ddaas_iot_devices)
