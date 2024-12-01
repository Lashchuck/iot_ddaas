package com.iot_ddaas;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * HomeController obsługuje żądania do głównego adresu URL („/”) i statycznego pliku index.html.
 */
@Controller
@ResponseBody // Adnotacja wskazuje, że wartość zwracana metod powinna być zapisywana bezpośrednio w treści odpowiedzi.
public class HomeController {

    private final ResourceLoader resourceLoader; // ResourceLoader jest używany do ładowania zasobów ze ścieżki klas.

    public HomeController(ResourceLoader resourceLoader){
        this.resourceLoader=resourceLoader;
    }

    // Obsługa żądania GET do głównego adresu URL („/”) i  pliku index.html.
    @GetMapping("/")
    public ResponseEntity<Resource> home(){
        // Załadowanie pliku index.html ze ścieżki klas (/static).
        Resource resource = resourceLoader.getResource("classpath:static/index.html");

        return ResponseEntity.ok(resource);
    }
}
