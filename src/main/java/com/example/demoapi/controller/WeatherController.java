package com.example.demoapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final String API_KEY = "70428157491abb380e0c291e48f097b3";

    @GetMapping
    public Map getWeather(@RequestParam String zip) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.openweathermap.org/data/2.5/weather?zip="
                + zip + ",us&units=imperial&appid=" + API_KEY;

        return restTemplate.getForObject(url, Map.class);
    }
}