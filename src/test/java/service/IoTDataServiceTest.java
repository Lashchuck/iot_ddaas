package service;

import com.iot_ddaas.Anomaly;
import com.iot_ddaas.IoTData;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.service.IoTDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;


// Integracja JUnit z Mockito
@ExtendWith(MockitoExtension.class)
public class IoTDataServiceTest {

    // @Mock tworzy atrapę (mocka). Symuluje zachowanie np. IoTDataRepository w testach.
    @Mock
    private IoTDataRepository dataRepository;

    @Mock
    private AnomalyRepository anomalyRepository;

    @InjectMocks
    private IoTDataService ioTDataService;

    // setUp() jest uruchamiana przed każdym testem, aby przygotować środowisko testowe.
    @BeforeEach
    void setUp(){
        // Inicjalizacja mocków
        MockitoAnnotations.openMocks(this);
    }

    // Sprawdzenie, czy metoda prawidłowo zwraca dane
    @Test
    void shouldReturnTwoRecordsAndInvokeFindByUserIdOnce(){
        // Tworzenie listy mockowanych danych IoTData
        List<IoTData> mockData = List.of(
                new IoTData(1L, "ESP-32-moisture-sensors", 20, 30, 1L, null, LocalDateTime.now()),
                new IoTData(2L, "ESP8266-temperature-sensor", null, null, 2L, 20.0f, LocalDateTime.now())
        );
        when(dataRepository.findByUserId(1L)).thenReturn(mockData);

        List<IoTData> result = ioTDataService.getAllData(1L);
        // Sprawdzenie czy rozmiar listy to 2
        assertEquals(2, result.size());
        // Weryfikacja, że metoda findAll() została wywołana raz
        verify(dataRepository, times(1)).findByUserId(1L);
    }

    // Sprawdzenie czy metoda poprawnie zwraca dane na podstawie ID
    @Test
    void shouldReturnDataWithCorrectDeviceIdById(){

        // Tworzenie mockowanych danych IoTData
        IoTData mockData = new IoTData(1L, "ESP-32-moisture-sensors", 20, 30, 1L, null, LocalDateTime.now());
        // Konfiguracja mock reporyzotrium, aby zwracało dane, gdy findById(1L) zostanie wywołane.
        when(dataRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockData));

        // Wywołanie getDataById i sprawdzenie czy dane są obecne i mają oczekiwany deviceId
        Optional<IoTData> result = ioTDataService.getDataById(1L, 1L);
        assertTrue(result.isPresent());
        assertEquals("ESP-32-moisture-sensors", result.get().getDeviceId());
    }

    // Sprawdzenie czy saveData() zapisuje dane i nie wywołuje zapisywania anomalii.
    @Test
    void shouldInvokeDataRepositoryOnceAndNotInvokeAnomalyRepository(){

        IoTData mockData = new IoTData(1L, "ESP-32-moisture-sensors", 20, 30, 1L, null, LocalDateTime.now());

        // Wywołanie saveData(mockData)
        ioTDataService.saveData(mockData);

        // Weryfikacja że metoda save() repozytorium została wywołana z danymi 1 raz.
        verify(dataRepository, times(1)).save(mockData);
        // Weryfikacja że metoda save() repozytorium anomalii nie została wywołana.
        verify(anomalyRepository, times(0)).save(any(Anomaly.class));
    }

    // Sprawdzenie czy deleteData() poprawnie usuwa dane na podstawie ID.
    @Test
    void shouldInvokeDeleteByIdOnceWhenDeletingData() {

        Long dataId = 1L;

        // Wywołanie deleteData(1L)
        ioTDataService.deleteData(dataId);
        // Weryfikacja że metoda deleteById() repozytorium została wywołana raz z danym ID.
        verify(dataRepository, times(1)).deleteById(dataId);
    }

    // Sprawdzenie czy metoda prawidłowo zwraca listę anomalii.
    @Test
    void shouldReturnTwoAnomaliesAndInvokeFindAllOnce() {

        // Tworzenie listy mockowanych anomalii
        List<Anomaly> mockAnomalies = Arrays.asList(new Anomaly(), new Anomaly());
        // Konfiguracja mock repozytorium anomalii, aby zwracało listę, gdy findAll() zostanie wywołane.
        when(anomalyRepository.findAll()).thenReturn(mockAnomalies);
        List<Anomaly> result = ioTDataService.getAllAnomalies();
        assertEquals(2, result.size());
        verify(anomalyRepository, times(1)).findAll();
    }

    // Sprawdzenie czy deleteAnomaly() poprawnie usuwa anomalię na podstawie ID.
    @Test
    void shouldInvokeDeleteAnomalyByIdOnce(){

        Long anomalyId = 1L;
        ioTDataService.deleteAnomaly(anomalyId);
        verify(anomalyRepository, times(1)).deleteById(anomalyId);
    }
}
