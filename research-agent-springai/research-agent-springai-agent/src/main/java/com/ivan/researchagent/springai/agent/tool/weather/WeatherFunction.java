package com.ivan.researchagent.springai.agent.tool.weather;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 模拟天气查询服务 - React Agent调用的外部工具
 */
@Slf4j
public class WeatherFunction implements Function<WeatherService.Request, WeatherService.Response> {

    private WeatherService weatherService;

    public WeatherFunction(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public WeatherService.Response apply(WeatherService.Request request) {
        return weatherService.getWeather(request);
    }

}
