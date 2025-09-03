package com.ivan.researchagent.springai.llm.tools.search.serpapi;

import com.google.gson.Gson;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import com.ivan.researchagent.springai.llm.tools.search.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
@Slf4j
public class SerpApieService extends WebSearchService {

    private final WebClient webClient;

    private final String apikey;

    private final String engine;

    private final String baseUrl;

    private static final int MEMORY_SIZE = 5;

    private static final int BYTE_SIZE = 1024;

    private static final int MAX_MEMORY_SIZE = MEMORY_SIZE * BYTE_SIZE * BYTE_SIZE;

    public SerpApieService(SerpApiProperties properties) {
        this.apikey = properties.getApikey();
        this.engine = properties.getEngine();
        this.baseUrl = Optional.ofNullable(properties.getBaseUrl()).orElse(properties.SERP_API_URL);
        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, this.baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_MEMORY_SIZE))
                .build();
    }

    /**
     * 使用 serpai API 搜索数据
     * @param request the function argument
     * @return responseMono
     */
    public Map<String, Object> query(WebSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.query())) {
            return null;
        }
        try {
            Mono<String> responseMono = webClient.method(HttpMethod.GET)
                    .uri(uriBuilder -> uriBuilder.queryParam("api_key", apikey)
                            .queryParam("engine", engine)
                            .queryParam("q", request.query())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class);
            String response = responseMono.block();
            assert response != null;
            log.info("serpapi search: {},result:{}", request.query(), response);
            return parseJson(response);
        }
        catch (Exception e) {
            log.error("failed to invoke serpapi search, caused by:{}", e.getMessage());
            return null;
        }
    }

    @Override
    public WebSearchResponse search(WebSearchRequest request) {
        Map<String, Object> response = query(request);
        return parseResponse(response);
    }

    @Override
    public WebSearchResponse apply(WebSearchRequest request) {
        return search(request);
    }

    private Map<String, Object> parseJson(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, Map.class);
    }

    private WebSearchResponse parseResponse(Map<String, Object> response) {
        if (Objects.isNull( response)) {
            return null;
        }

        WebSearchResponse searchResponse = new WebSearchResponse();
        List<WebSearchResponse.ResultInfo> resultInfos = new ArrayList<>();

        if (response.containsKey("answer_box") && response.get("answer_box") instanceof List) {
            response.put("answer_box", ((List) response.get("answer_box")).get(0));
        }

        if (response.containsKey("answer_box")
                && ((Map<String, Object>) response.get("answer_box")).containsKey("answer")) {
            Map<String, Object> answerBox = (Map<String, Object>) response.get("answer_box");

            WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
            resultInfo.setTitle(answerBox.get("title").toString());
            resultInfo.setContent(answerBox.get("answer").toString());
            resultInfo.setUrl(answerBox.get("link").toString());

            resultInfos.add(resultInfo);
        }
        else if (response.containsKey("answer_box")
                && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet")) {
            Map<String, Object> answerBox = (Map<String, Object>) response.get("answer_box");

            WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
            resultInfo.setTitle(answerBox.get("title").toString());
            resultInfo.setContent(answerBox.get("snippet").toString());
            resultInfo.setUrl(answerBox.get("link").toString());

            resultInfos.add(resultInfo);
        }
        else if (response.containsKey("answer_box")
                && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet_highlighted_words")) {
            Map<String, Object> answerBox = (Map<String, Object>) response.get("answer_box");

            WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
            String content = ((List<String>) answerBox.get("snippet_highlighted_words")).get(0);
            resultInfo.setContent(content);

            resultInfos.add(resultInfo);
        }
        else if (response.containsKey("sports_results")
                && ((Map<String, Object>) response.get("sports_results")).containsKey("game_spotlight")) {
            Map<String, Object> results = (Map<String, Object>) response.get("sports_results");

            WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
            resultInfo.setContent(results.get("game_spotlight").toString());

            resultInfos.add(resultInfo);
        }
        else if (response.containsKey("shopping_results")
                && ((List<Map<String, Object>>) response.get("shopping_results")).get(0).containsKey("title")) {
            List<Map<String, Object>> shoppingResults = (List<Map<String, Object>>) response.get("shopping_results");

            List<Map<String, Object>> subList = shoppingResults.subList(0, 3);
            for (Map<String, Object> sub : subList) {
                WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
                resultInfo.setTitle(sub.get("title").toString());
                resultInfo.setContent(sub.get("title").toString());
                resultInfo.setUrl(sub.get("link").toString());

                resultInfos.add(resultInfo);
            }
        }
        else if (response.containsKey("knowledge_graph")
                && ((Map<String, Object>) response.get("knowledge_graph")).containsKey("description")) {
            Map<String, Object> answerBox = (Map<String, Object>) response.get("knowledge_graph");

            WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
            resultInfo.setTitle(answerBox.get("title").toString());
            resultInfo.setContent(answerBox.get("description").toString());
            resultInfo.setUrl(answerBox.get("knowledge_graph_search_link").toString());

            resultInfos.add(resultInfo);
        }
        else if ((((List<Map<String, Object>>) response.get("organic_results")).get(0)).containsKey("snippet")) {
            List<Map<String, Object>> organicResults = (List<Map<String, Object>>) response.get("organic_results");

            List<Map<String, Object>> subList = organicResults.subList(0, 3);
            for (Map<String, Object> sub : subList) {
                WebSearchResponse.ResultInfo resultInfo = new WebSearchResponse.ResultInfo();
                resultInfo.setTitle(sub.get("title").toString());
                resultInfo.setContent(sub.get("snippet").toString());
                resultInfo.setUrl(sub.get("link").toString());

                resultInfos.add(resultInfo);
            }
        }
        else if (response.containsKey("images_results")
                && ((Map<String, Object>) ((List<Map<String, Object>>) response.get("images_results")).get(0)).containsKey("thumbnail")) {

            List<Map<String, Object>> imageResults = (List<Map<String, Object>>) response.get("images_results");
            List<WebSearchResponse.ImageInfo> imageInfos = new ArrayList<>();
            for (Map<String, Object> item : imageResults.subList(0, 10)) {
                WebSearchResponse.ImageInfo imageInfo = new WebSearchResponse.ImageInfo();
                imageInfo.setUrl(item.get("thumbnail").toString());
                imageInfos.add(imageInfo);
            }
            searchResponse.setImages(imageInfos);
        }

        searchResponse.setResults(resultInfos);
        return searchResponse;
    }

    // 示例
    String s = """ 
            curl --get https://serpapi.com/search \\
             -d engine="google" \\
             -d q="Coffee" \\
             -d location="Seattle-Tacoma,+WA,+Washington,+United+States" \\
             -d hl="en" \\
             -d gl="us" \\
             -d google_domain="google.com" \\
             -d num="10" \\
             -d start="10" \\
             -d safe="active" \\
             -d api_key="a2a2ff3d89517ee39786d42adf017adec28cf8d29b2d3ec9736c58a8583fdbf0"
            
            
             curl --get https://serpapi.com/search \\
             -d engine="google" \\
             -d q="Coffee" \\
             -d api_key="a2a2ff3d89517ee39786d42adf017adec28cf8d29b2d3ec9736c58a8583fdbf0"
            
            
            
            {
              "search_metadata": {
                "id": "61afb3ace7d08a685b3bcbb1",
                "status": "Success",
                "json_endpoint": "https://serpapi.com/searches/c292c1c1fe17fc58/61afb3ace7d08a685b3bcbb1.json",
                "created_at": "2021-12-07 19:19:08 UTC",
                "processed_at": "2021-12-07 19:19:08 UTC",
                "google_url": "https://www.google.com/search?q=coffee&oq=coffee&uule=w+CAIQICIaQXVzdGluLFRleGFzLFVuaXRlZCBTdGF0ZXM&hl=en&gl=us&sourceid=chrome&ie=UTF-8",
                "raw_html_file": "https://serpapi.com/searches/c292c1c1fe17fc58/61afb3ace7d08a685b3bcbb1.html",
                "total_time_taken": 1.52
              },
              "search_parameters": {
                "engine": "google",
                "q": "coffee",
                "location_requested": "Austin, Texas, United States",
                "location_used": "Austin,Texas,United States",
                "google_domain": "google.com",
                "hl": "en",
                "gl": "us",
                "device": "desktop"
              },
              "search_information": {
                "organic_results_state": "Results for exact spelling",
                "query_displayed": "coffee",
                "total_results": 1340000000,
                "time_taken_displayed": 0.99
              },
              "recipes_results": [
                {
                  "title": "Bulletproof Coffee Recipe",
                  "link": "https://www.bulletproof.com/recipes/bulletproof-diet-recipes/bulletproof-coffee-recipe/",
                  "source": "Bulletproof",
                  "total_time": "5 min",
                  "ingredients": [
                    "Mct oil",
                    "bulletproof coffee",
                    "grass fed"
                  ],
                  "thumbnail": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTBL6y0xwQPtXHfPXpTETKfmTlFKCc53AZ66YJ4wmBf4BZQbMmR2szgOg&s=0"
                },
                {
                  "title": "Whipped Coffee",
                  "link": "https://cooking.nytimes.com/recipes/1021005-whipped-coffee",
                  "source": "NYT Cooking - The New York Times",
                  "rating": 4,
                  "reviews": 770,
                  "ingredients": [
                    "Instant coffee",
                    "ice",
                    "milk",
                    "hot water"
                  ],
                  "thumbnail": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRfvf7kwy5yjTVCoKUpROKBXX2UY9TH4Ko0WIxieGKGIwKUkgAxaTwTdg&s=0"
                }
              ],
              "shopping_results": [
                {
                  "position": 1,
                  "block_position": "top",
                  "title": "mudwtr.com - MUD\\\\WTR | Mushroom Coffee Alternative, 30 servings",
                  "price": "$50.00",
                  "extracted_price": 50,
                  "link": "https://www.google.com/aclk?sa=l&ai=DChcSEwiZtL6MtNL0AhWBn7MKHTqRDpEYABAEGgJxbg&ae=2&sig=AOD64_2QRwv8qgRnd6Jr65C9UhyHPLwhXA&ctype=5&q=&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ5bgDegQIAhA9&adurl=",
                  "source": "mudwtr.com",
                  "reviews": 6000,
                  "thumbnail": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/3dfd005c088bc5e4e2b7ab1c2868aa47743a6687f658162aac59c78c75d56d1d.png"
                },
                {
                  "position": 2,
                  "block_position": "top",
                  "title": "mudwtr.com - MUD\\\\WTR | Mushroom Coffee Replacement, 90 servings",
                  "price": "$125.00",
                  "extracted_price": 125,
                  "link": "https://www.google.com/aclk?sa=l&ai=DChcSEwiZtL6MtNL0AhWBn7MKHTqRDpEYABADGgJxbg&ae=2&sig=AOD64_37xmneT6EL2jcvAGOifAFCwRsKLg&ctype=5&q=&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ5bgDegQIAhBI&adurl=",
                  "source": "mudwtr.com",
                  "reviews": 2000,
                  "thumbnail": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/3dfd005c088bc5e4e2b7ab1c2868aa4728d69aa6f14be4eb6102830c5aa2461d.jpeg"
                }
              ],
              "local_map": {
                "link": "https://www.google.com/search?gl=us&hl=en&q=coffee&npsic=0&rflfq=1&rldoc=1&rllag=30267485,-97742560,126&tbm=lcl&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQtgN6BAgfEAc",
                "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/b58ac7a94530b87c33a3ef84f967f2f8.png",
                "gps_coordinates": {
                  "latitude": 30.267485,
                  "longitude": -97.74256
                }
              },
              "local_results": {
                "more_locations_link": "https://www.google.com/search?gl=us&hl=en&tbs=lf:1,lf_ui:9&tbm=lcl&q=coffee&rflfq=1&num=10&uule=w+CAIQICIaQXVzdGluLFRleGFzLFVuaXRlZCBTdGF0ZXM&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQjGp6BAgfEGI",
                "places": [
                  {
                    "position": 1,
                    "title": "Houndstooth Coffee",
                    "place_id": "11265938073076301333",
                    "lsig": "AB86z5Vdw6C2pJpM0xQ6JUx2KONU",
                    "place_id_search": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&lsig=AB86z5Vdw6C2pJpM0xQ6JUx2KONU&ludocid=11265938073076301333&q=coffee&tbm=lcl",
                    "rating": 4.6,
                    "reviews": 746,
                    "price": "$$",
                    "type": "Coffee shop",
                    "address": "401 Congress Ave #100c · In Frost Bank Tower",
                    "thumbnail": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/ff7f6a31fb4c327e6006e2b3ceb404a5f7a61b6a65199dc7462885107dab20d4c83002a8ec0eb1e0.jpeg",
                    "gps_coordinates": {
                      "latitude": 30.2664,
                      "longitude": -97.74278
                    }
                  },
                  {
                    "position": 2,
                    "title": "Starbucks",
                    "place_id": "10605736027611436825",
                    "lsig": "AB86z5XTJ_Io_anVBu2fU6Zaqu3b",
                    "place_id_search": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&lsig=AB86z5XTJ_Io_anVBu2fU6Zaqu3b&ludocid=10605736027611436825&q=coffee&tbm=lcl",
                    "rating": 4.1,
                    "reviews": 509,
                    "price": "$$",
                    "type": "Coffee shop",
                    "address": "600 Congress Ave",
                    "thumbnail": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/ff7f6a31fb4c327e6006e2b3ceb404a5befc37eb0b3b33e75015a537212b37488eacfc7600ad8af3.jpeg",
                    "gps_coordinates": {
                      "latitude": 30.26826,
                      "longitude": -97.74296
                    }
                  }
                ]
              },
              "knowledge_graph": {
                "title": "Coffee",
                "type": "Drink",
                "kgmid": "/m/02vqfm",
                "knowledge_graph_search_link": "https://www.google.com/search?kgmid=/m/02vqfm&hl=en-US&q=Coffee&kgs=e5a0eb9eeef80765&shndl=0&source=sh/x/kp/1&entrypoint=sh/x/kp",
                "serpapi_knowledge_graph_search_link": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en-US&kgmid=%2Fm%2F02vqfm&location=Austin%2C+Texas%2C+United+States&q=Coffee",
                "header_images": [
                  {
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8030d0f84891c9ef7fe21e7b8b27d7c20036bdb007f4c06f7d.jpeg",
                    "source": "https://en.wikipedia.org/wiki/Coffee"
                  },
                  {
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8030d0f84891c9ef7f7c3c5fd0133d77faa56cbc324a3ece25.jpeg",
                    "source": "https://www.nbcnews.com/better/lifestyle/how-tap-health-benefits-coffee-ncna1096031"
                  },
                  {
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8030d0f84891c9ef7f8cf5b470290189d19afb9eaffe17b13f.jpeg",
                    "source": "https://austin.eater.com/maps/best-coffee-austin-cafes-patio-latte-pour-over"
                  },
                  {
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8030d0f84891c9ef7f6387935aaacf98275d04f002fb06c161.jpeg",
                    "source": "https://www.cancer.org/latest-news/coffee-and-cancer-what-the-research-really-shows.html"
                  }
                ],
                "description": "Coffee is a brewed drink prepared from roasted coffee beans, the seeds of berries from certain Coffea species. From the coffee fruit, the seeds are separated to produce a stable, raw product: unroasted green coffee.",
                "source": {
                  "name": "Wikipedia",
                  "link": "https://en.wikipedia.org/wiki/Coffee"
                },
                "patron_saint": "Saint Drogo",
                "patron_saint_links": [
                  {
                    "text": "Patron saint",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=coffee+patron+saint&stick=H4sIAAAAAAAAAOPgE-LUz9U3MCorTMvVksrPttIvzsgvKklLTC6xKkgsKcrPiy9OzMwrWcQqnJyflpaaqgARVQCLAgBzUMsxPwAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ6BMoAHoECEwQAg"
                  }
                ],
                "species_of_coffee": [
                  {
                    "name": "Coffea arabica",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=Coffea+arabica&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVAjNNTJPSLbREspOt9JPzc3Pz86xS8svzyhOLUopXMcoBxXJyUpNLMvPz9JMy83Py0zOTE3PiiwtSkzNTixex8jnnp6WlJiokFiUmAWV2sDICAOGVXZhjAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhIEAU",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a807d547b329424c7b52547f1fa893b42705bf58be621d9c2cac17e5a18fdd042db.jpeg"
                  },
                  {
                    "name": "Robusta coffee",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=Robusta+coffee&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVAjPNkquyLbREspOt9JPzc3Pz86xS8svzyhOLUopXMcoBxXJyUpNLMvPz9JMy83Py0zOTE3PiiwtSkzNTixex8gXlJ5UWlyQqJOenpaWm7mBlBAAoykDxYwAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhIEAc",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a807d547b329424c7b52547f1fa893b4270cdc3268a8313cf7011d578aaf9251050.jpeg"
                  }
                ],
                "species_of_coffee_link": "https://www.google.com/search?gl=us&hl=en&q=Species+of+coffee&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1RLKTrfST83Nz8_OsUvLL88oTi1KKVzHKAcVyclKTSzLz8_STMvNz8tMzkxNz4osLUpMzU4sXsQoGQ1gK-WkKyflpaampO1gZAaPUmVRmAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQMSgAegQISBAB",
                "species_of_coffee_stick": "H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1RLKTrfST83Nz8_OsUvLL88oTi1KKVzHKAcVyclKTSzLz8_STMvNz8tMzkxNz4osLUpMzU4sXsQoGQ1gK-WkKyflpaampO1gZAaPUmVRmAAAA",
                "coffee_books": [
                  {
                    "name": "The World Atlas of Coffee: F...",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=The+World+Atlas+of+Coffee:+From+Beans+to+Brewing+-+Coffees+Explored,+Explained+and+Enjoyed&stick=H4sIAAAAAAAAAC3JQQqCQBSAYSSEWrQoOsCjZRSjQRDuMuwEQdBudN6YOvNezUjadVp2go5XRLuf7x8OpiNhRbS-37Sdj0Up4jjfmH5b1vVi1hSJKNhapkRxR510yr-CydeMwaKtmETO3PhneD5eEE7sjIJda6QH1rBnrRETODi2kKIkDy1D6rCrqITV_3vI-qthh2r5K1kRKpCkIKOaH6jeYfABYiUriKYAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhKEAU",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a804f82f30abd848a4df49f867f2b624ead490a6d28fb7d25ba.jpeg"
                  },
                  {
                    "name": "Craft Coffee: A Manual",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=Craft+Coffee:+A+Manual&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtV4tVP1zc0TKmwTDe2yC7TEslOttJPzs_Nzc-zSskvzytPLEopXsUoCBTLyUlNLsnMz9NPys_PLl7EKuZclJhWouCcn5aWmmql4Kjgm5hXmpizg5URAFoyNJBiAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhKEAc",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a804f82f30abd848a4d724635725d09f09c611c2eb40d8f939c.jpeg"
                  }
                ],
                "coffee_books_link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+books&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1RLKTrfST83Nz8_OsUvLL88oTi1KKVzEKAsVyclKTSzLz8_ST8vOzixex8jjnp6WlpiqAuTtYGQHBbXIpVAAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQMSgAegQIShAB",
                "coffee_books_stick": "H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1RLKTrfST83Nz8_OsUvLL88oTi1KKVzEKAsVyclKTSzLz8_ST8vOzixex8jjnp6WlpiqAuTtYGQHBbXIpVAAAAA",
                "people_also_search_for": [
                  {
                    "name": "Tea",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=Tea&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtV4gAxzZNzKrQEgzNTUssTK4v9UitKgktSC4oXsTKHpCbuYGUEAEjPygozAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhJEAU",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8089b8d07732f39762500dde409851a6ef38d30761dbfc211161c7207b14588ee7.jpeg"
                  },
                  {
                    "name": "Espresso",
                    "link": "https://www.google.com/search?gl=us&hl=en&q=Espresso&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtV4gAxk0vSsrUEgzNTUssTK4v9UitKgktSC4oXsXK4FhcUpRYX5-9gZQQADwmvdzgAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQxA16BAhJEAc",
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a8089b8d07732f39762500dde409851a6efb1d2c721e38a3e0c4f69575558e9f6ca.jpeg"
                  }
                ],
                "people_also_search_for_link": "https://www.google.com/search?gl=us&hl=en&q=Coffee&stick=H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1BIMzU1LLEyuL_VIrSoJLUguKF7GyOeenpaWm7mBlBABkIv_mNwAAAA&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQMSgAegQISRAB",
                "people_also_search_for_stick": "H4sIAAAAAAAAAONgFuLUz9U3MCorTMtVQjC1BIMzU1LLEyuL_VIrSoJLUguKF7GyOeenpaWm7mBlBABkIv_mNwAAAA",
                "see_results_about": [
                  {
                    "name": "Coffee bean",
                    "extensions": [
                      "A coffee bean is a seed of the Coffea plant and the source for ..."
                    ],
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a80d449011f57086f189f559fd2a72b80c1e5a9e9078bffd5915248cedad7d63311.jpeg"
                  },
                  {
                    "name": "Coffee",
                    "extensions": [
                      "Plant"
                    ],
                    "image": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/465c36209ea9860426d7785256518a80d449011f57086f189f559fd2a72b80c12e5d2aedf43d3fbc5b802f8946d87f54.jpeg"
                  }
                ],
                "list": {
                  "total_fat": [
                    "0 g",
                    "0%"
                  ],
                  "saturated_fat": [
                    "0 g",
                    "0%"
                  ],
                  "trans_fat_regulation": [
                    "0 g"
                  ],
                  "cholesterol": [
                    "0 mg",
                    "0%"
                  ],
                  "sodium": [
                    "5 mg",
                    "0%"
                  ],
                  "potassium": [
                    "116 mg",
                    "3%"
                  ],
                  "total_carbohydrate": [
                    "0 g",
                    "0%"
                  ],
                  "dietary_fiber": [
                    "0 g",
                    "0%"
                  ],
                  "sugar": [
                    "0 g"
                  ],
                  "protein": [
                    "0.3 g",
                    "0%"
                  ],
                  "caffeine": [
                    "95 mg"
                  ],
                  "vitamin_c": [
                    "0%"
                  ],
                  "calcium": [
                    "0%"
                  ],
                  "iron": [
                    "0%"
                  ],
                  "vitamin_d": [
                    "0%"
                  ],
                  "vitamin_b6": [
                    "0%"
                  ],
                  "cobalamin": [
                    "0%"
                  ],
                  "magnesium": [
                    "1%"
                  ]
                }
              },
              "discover_more_places": [
                {
                  "title": "Takeout",
                  "link": "https://www.google.com/search?gl=us&hl=en&tbm=lcl&q=takeout+food&rflfq=1&num=10&uule=w+CAQQCFISCS8DzKCZtUSGEXrVadRLRptd&lsspp=CdSKrZykpVuv&rlt=Takeout&owsq=coffee&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ9s8CegQIOBAE",
                  "images": [
                    "https://lh3.googleusercontent.com/QE3L93qm5Lz1UMlSZwPP_DREG4xig_H6_qMqcER9_vPPskpTePFHDhuILq1Cwk0=w157-h157-n"
                  ]
                },
                {
                  "title": "Delivery",
                  "link": "https://www.google.com/search?gl=us&hl=en&tbm=lcl&q=Delivery+Food&rflfq=1&num=10&uule=w+CAQQCFISCS8DzKCZtUSGEXrVadRLRptd&lsspp=&rlt=Delivery&owsq=coffee&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ9s8CegQIOBAG",
                  "images": [
                    "https://lh3.googleusercontent.com/t204sQ6xcx_aUaQET-81rKJRika8r7Q2Dr3zl3UyXNgDkeorQDpIGeR5EGLssZmW=w157-h157-n"
                  ]
                },
                {
                  "title": "Coffee and Wi-Fi",
                  "link": "https://www.google.com/search?gl=us&hl=en&tbm=lcl&q=cafe+with+wifi&rflfq=1&num=10&uule=w+CAQQCFISCS8DzKCZtUSGEXrVadRLRptd&lsspp=CTZoWvL3zxXX&rlt=Coffee+and+Wi-Fi&owsq=coffee&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ9s8CegQIOBAI",
                  "images": [
                    "https://lh5.googleusercontent.com/p/AF1QipP4YxFeZQnTsnN7nOlW6J4JKNnTeMFAnRVwPK_J=w157-h157-n-k-no"
                  ]
                }
              ],
              "related_questions": [
                {
                  "question": "What coffee does to your body?",
                  "snippet": "The Bottom Line Not only can your daily cup of joe help you feel more energized, burn fat and improve physical performance, it may also lower your risk of several conditions, such as type 2 diabetes, cancer and Alzheimer's and Parkinson's disease. In fact, coffee may even boost longevity.Aug 14, 2017",
                  "title": "13 Health Benefits of Coffee, Based on Science - Healthline",
                  "link": "https://www.healthline.com/nutrition/top-13-evidence-based-health-benefits-of-coffee",
                  "displayed_link": "https://www.healthline.com › nutrition › top-13-eviden..."
                },
                {
                  "question": "Is coffee made from poop?",
                  "snippet": "Kopi luwak is coffee made from coffee cherries that have been eaten, digested, and defecated by the Asian palm civet, a small mammal that looks like a cross between a cat and a raccoon. The beans are then cleaned and processed. In the West, kopi luwak has become known as \\"cat poop coffee.\\"Nov 9, 2018",
                  "title": "Kopi Luwak: 'World's Most Expensive Coffee' Is a Tourist Trap",
                  "link": "https://www.businessinsider.com/kopi-luwak-cat-poop-worlds-most-expensive-coffee-taste-test-2018-11",
                  "displayed_link": "https://www.businessinsider.com › kopi-luwak-cat-poop-..."
                }
              ],
              "organic_results": [
                {
                  "position": 1,
                  "title": "Coffee - Wikipedia",
                  "link": "https://en.wikipedia.org/wiki/Coffee",
                  "redirect_link": "https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://en.wikipedia.org/wiki/Coffee&ved=2ahUKEwiR5vqbm5KDAxUJSzABHetUBPsQFnoECA8QAQ",
                  "displayed_link": "https://en.wikipedia.org › wiki › Coffee",
                  "snippet": "Coffee is a brewed drink prepared from roasted coffee beans, the seeds of berries from certain Coffea species. From the coffee fruit, the seeds are ...",
                  "sitelinks": {
                    "inline": [
                      {
                        "title": "Coffee bean",
                        "link": "https://en.wikipedia.org/wiki/Coffee_bean"
                      },
                      {
                        "title": "History",
                        "link": "https://en.wikipedia.org/wiki/History_of_coffee"
                      },
                      {
                        "title": "Coffee production",
                        "link": "https://en.wikipedia.org/wiki/Coffee_production"
                      },
                      {
                        "title": "Coffee preparation",
                        "link": "https://en.wikipedia.org/wiki/Coffee_preparation"
                      }
                    ]
                  },
                  "rich_snippet": {
                    "bottom": {
                      "extensions": [
                        "Region of origin: Horn of Africa and ‎South Ara...‎",
                        "Color: Black, dark brown, light brown, beige",
                        "Introduced: 15th century"
                      ],
                      "detected_extensions": {
                        "introduced_th_century": 15
                      }
                    }
                  },
                  "about_this_result": {
                    "source": {
                      "description": "Wikipedia is a free content, multilingual online encyclopedia written and maintained by a community of volunteers through a model of open collaboration, using a wiki-based editing system. Individual contributors, also called editors, are known as Wikipedians.",
                      "source_info_link": "https://en.wikipedia.org/wiki/Wikipedia",
                      "security": "secure",
                      "icon": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/1757d19c4614c530c07ecd54bbf57867d01831e29d004ddc31b8875ca5ef437c1d9699009c53cdb8c75ee43c7c816d63.png"
                    },
                    "keywords": [
                      "coffee"
                    ],
                    "languages": [
                      "English"
                    ],
                    "regions": [
                      "the United States"
                    ]
                  },
                  "about_page_link": "https://google.com/search?q=About+https://en.wikipedia.org/wiki/Coffee&tbm=ilp&ilps=AOR-xxt3p1dy2npn9cHfxrbKVqVZCHn3Cg",
                  "about_page_serpapi_link": "https://serpapi.com/search.json?engine=google_about_this_result&ilps=AOR-xxt3p1dy2npn9cHfxrbKVqVZCHn3Cg&q=About+https://en.wikipedia.org/wiki/Coffee",
                  "cached_page_link": "https://webcache.googleusercontent.com/search?q=cache:U6oJMnF-eeUJ:https://en.wikipedia.org/wiki/Coffee+&cd=1&hl=en&ct=clnk&gl=us",
                  "related_pages_link": "https://www.google.com/search?gl=us&hl=en&q=related:https://en.wikipedia.org/wiki/Coffee+coffee"
                },
                {
                  "position": 2,
                  "title": "21 Excellent Coffee Shops in Austin",
                  "link": "https://austin.eater.com/maps/best-coffee-austin-cafes-patio-latte-pour-over",
                  "redirect_link": "https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwjes5qBsK2DAxXbKlkFHQlYBXUQFnoECA8QAQ&url=https%3A%2F%2Faustin.eater.com%2Fmaps%2Fbest-coffee-austin-cafes-patio-latte-pour-over&usg=AOvVaw0vUb5Alb8C6YUpwuOUzMNK&opi=89978449",
                  "displayed_link": "https://austin.eater.com › maps › best-coffee-austin-cafe...",
                  "date": "5 days ago",
                  "snippet": "21 Excellent Coffee Shops in Austin · 1. Barrett's Coffee · 2. Epoch Coffee · 3. Sa-Tén Coffee & Eats · 4. Houndstooth Coffee · 5. Civil Goat Coffee.",
                  "about_this_result": {
                    "source": {
                      "description": "Eater is a food website by Vox Media. It was co-founded by Lockhart Steele and Ben Leventhal in 2005, and originally focused on dining and nightlife in New York City. Eater launched a national site in 2009, and covered nearly 20 cities by 2012.",
                      "source_info_link": "https://en.wikipedia.org/wiki/Eater_(website)",
                      "security": "secure",
                      "icon": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/1757d19c4614c530c07ecd54bbf578674b65d56d05926615c9e7fa12be6f06fe1855aeabae1de5a388097b071638c318.png"
                    },
                    "keywords": [
                      "coffee"
                    ],
                    "languages": [
                      "English"
                    ]
                  },
                  "about_page_link": "https://google.com/search?q=About+https://austin.eater.com/maps/best-coffee-austin-cafes-patio-latte-pour-over&tbm=ilp&ilps=AOR-xxt1YQD-eKACl4hhka8ptbcB-c-VJQ",
                  "about_page_serpapi_link": "https://serpapi.com/search.json?engine=google_about_this_result&ilps=AOR-xxt1YQD-eKACl4hhka8ptbcB-c-VJQ&q=About+https://austin.eater.com/maps/best-coffee-austin-cafes-patio-latte-pour-over",
                  "cached_page_link": "https://webcache.googleusercontent.com/search?q=cache:uhpBGQL0eGQJ:https://austin.eater.com/maps/best-coffee-austin-cafes-patio-latte-pour-over+&cd=14&hl=en&ct=clnk&gl=us"
                },
                {
                  "position": 3,
                  "title": "Home | The Coffee Bean & Tea Leaf",
                  "link": "https://www.coffeebean.com/",
                  "redirect_link": "https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://www.coffeebean.com/&ved=2ahUKEwj3gdzSm5KDAxXdIEQIHW5OCPkQFnoECAkQAQ",
                  "displayed_link": "https://www.coffeebean.com",
                  "snippet": "Icon of a bag of coffee being shipped to you. Subscriptions. Never run out of your favorite coffees, teas and powders again with our auto-delivery subscription.",
                  "sitelinks": {
                    "inline": [
                      {
                        "title": "Store locator",
                        "link": "https://www.coffeebean.com/store-locator"
                      },
                      {
                        "title": "Coffee",
                        "link": "https://www.coffeebean.com/cafe-menu/coffee"
                      },
                      {
                        "title": "Cafe Menu",
                        "link": "https://www.coffeebean.com/cafe-menu"
                      },
                      {
                        "title": "Flavored Coffee",
                        "link": "https://store.coffeebean.com/collections/flavored-coffee"
                      }
                    ]
                  },
                  "about_this_result": {
                    "source": {
                      "description": "The Coffee Bean & Tea Leaf is an American coffee shop chain founded in 1963. Since 2019, it is a trade name of Ireland-based Super Magnificent Coffee Company Ireland Limited, itself wholly owned by Philippines-based Jollibee Foods Corporation.",
                      "source_info_link": "https://en.wikipedia.org/wiki/The_Coffee_Bean_%26_Tea_Leaf",
                      "security": "secure",
                      "icon": "https://serpapi.com/searches/61afb3ace7d08a685b3bcbb1/images/1757d19c4614c530c07ecd54bbf578672f5ff28f8f4481f4c2d8836164c49755e13bd84b4ed7686eedb810a0168c2551.png"
                    },
                    "keywords": [
                      "coffee"
                    ],
                    "related_keywords": [
                      "coffees"
                    ],
                    "languages": [
                      "English"
                    ],
                    "regions": [
                      "the United States"
                    ]
                  },
                  "about_page_link": "https://google.com/search?q=About+https://www.coffeebean.com/&tbm=ilp&ilps=AOR-xxsK_iBv-AalfdLkk76RB2nfCqGDRg",
                  "about_page_serpapi_link": "https://serpapi.com/search.json?engine=google_about_this_result&ilps=AOR-xxsK_iBv-AalfdLkk76RB2nfCqGDRg&q=About+https://www.coffeebean.com/",
                  "cached_page_link": "https://webcache.googleusercontent.com/search?q=cache:WpQxSYo2c6AJ:https://www.coffeebean.com/+&cd=15&hl=en&ct=clnk&gl=us",
                  "related_pages_link": "https://www.google.com/search?gl=us&hl=en&q=related:https://www.coffeebean.com/+coffee"
                }
              ],
              "related_searches": [
                {
                  "query": "coffee brands",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+brands&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAggEAE"
                },
                {
                  "query": "coffee beans",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+beans&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgsEAE"
                },
                {
                  "query": "coffee near me",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+near+me&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgkEAE"
                },
                {
                  "query": "coffee online",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+online&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgoEAE"
                },
                {
                  "query": "types of coffee",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Types+of+coffee&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgnEAE"
                },
                {
                  "query": "coffee maker",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+Maker&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgmEAE"
                },
                {
                  "query": "coffee recipe",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+recipe&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgpEAE"
                },
                {
                  "query": "coffee origin",
                  "link": "https://www.google.com/search?gl=us&hl=en&q=Coffee+origin&sa=X&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ1QJ6BAgiEAE"
                }
              ],
              "pagination": {
                "current": 1,
                "next": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=10&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8NMDegQIAhB2",
                "other_pages": {
                  "2": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=10&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBk",
                  "3": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=20&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBm",
                  "4": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=30&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBo",
                  "5": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=40&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBq",
                  "6": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=50&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBs",
                  "7": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=60&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBu",
                  "8": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=70&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBw",
                  "9": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=80&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhBy",
                  "10": "https://www.google.com/search?q=coffee&gl=us&hl=en&ei=0bOvYffWDvvV1sQP1OGSkAY&start=90&sa=N&ved=2ahUKEwi3g66MtNL0AhX7qpUCHdSwBGIQ8tMDegQIAhB0"
                }
              },
              "serpapi_pagination": {
                "current": 1,
                "next_link": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=10",
                "next": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=10",
                "other_pages": {
                  "2": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=10",
                  "3": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=20",
                  "4": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=30",
                  "5": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=40",
                  "6": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=50",
                  "7": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=60",
                  "8": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=70",
                  "9": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=80",
                  "10": "https://serpapi.com/search.json?device=desktop&engine=google&gl=us&google_domain=google.com&hl=en&location=Austin%2C+Texas%2C+United+States&q=coffee&start=90"
                }
              }
            }
    """;

}
