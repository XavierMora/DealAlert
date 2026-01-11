package com.games_price_tracker.api.steam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
public class AppDetailsSteamTest {
	private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Autowired
    AppDetailsSteamTest(ObjectMapper objectMapper, ResourceLoader resourceLoader){
        this.objectMapper=objectMapper;
        this.resourceLoader=resourceLoader;
    }

	@Test
	void shouldParseAppDetailsWithPrice() throws IOException {
        JsonNode dataJson = getDataJson(true); 
        AppDetailsSteam appdetails = objectMapper.convertValue(dataJson, AppDetailsSteam.class);

        assertEquals(1799, appdetails.getInitialPrice());
        assertEquals(1439, appdetails.getFinalPrice());
	}

    @Test
	void shouldParseAppDetailsWithoutPrice() throws IOException {
        JsonNode dataJson = getDataJson(false); 
        AppDetailsSteam appdetails = objectMapper.convertValue(dataJson, AppDetailsSteam.class);

        assertEquals(0, appdetails.getInitialPrice());
        assertEquals(0, appdetails.getFinalPrice());
	}

    private JsonNode getDataJson(boolean withPrice) throws IOException{
        InputStream file = resourceLoader.getResource("classpath:test-appdetails-response.json").getInputStream();
        
        String index = withPrice ? "0" : "1";
        return objectMapper.readTree(file).get(index);
    }
}
