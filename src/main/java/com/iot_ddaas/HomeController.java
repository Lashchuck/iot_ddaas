package com.iot_ddaas;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class HomeController {

    private final ResourceLoader resourceLoader;

    public HomeController(ResourceLoader resourceLoader){
        this.resourceLoader=resourceLoader;
    }

    @GetMapping("/")
    public ResponseEntity<Resource> home(){
        System.out.println("Home controller is called");
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        return ResponseEntity.ok(resource);
    }
}
