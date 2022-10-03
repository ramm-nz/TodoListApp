package util;

import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestDataBuild {

    public static RequestSpecification request;
    public static String todoItemEndPoint;
    @Step("POST payload for todoItem  is constructed")
    public Map<String, Object> addItem(String itemName){
        Map<String, Object> jsonObjectPayload = new HashMap<>();
        jsonObjectPayload.put("description", itemName);
        return jsonObjectPayload;
    }
    @Step("PUT payload for todoItem  is constructed")
    public Map<String, Object> editItem(String itemName, String id){
        Map<String, Object> jsonObjectPayload2 = new HashMap<>();
        jsonObjectPayload2.put("description", itemName);
        jsonObjectPayload2.put("id", id);
        jsonObjectPayload2.put("isCompleted", true);
        return jsonObjectPayload2;
    }
    @Step("Request call made to Base URI and set ContentType as JSON")
    public RequestSpecification requestSpecification() throws IOException {
        todoItemEndPoint =getProperty("todoItemEndpoint");
        if(request==null) {
            PrintStream apiLog = new PrintStream(new FileOutputStream("src/main/resources/logger.txt"));
            request = new RequestSpecBuilder().setBaseUri(getProperty("localURL")).
                    addFilter(RequestLoggingFilter.logRequestTo(apiLog)).
                    addFilter(ResponseLoggingFilter.logResponseTo(apiLog)).
                    setContentType(ContentType.JSON).build();
            return request;
        }
        return request;
    }
    @Step("Fetch value from global property")
    public static String getProperty(String key) throws IOException {
        Properties property = new Properties();
        FileInputStream fileInputStream = new FileInputStream("src/main/resources/global.properties");
        property.load(fileInputStream);
        return property.getProperty(key);

    }
    @Step("New item name is generated and returned")
    public static String itemNameGen(){
        int length =10;
        String characterSet= "abcdefghijklmnopqrstuvwxyz0123456789";
        return IntStream.range(0, length).map(i -> new SecureRandom().nextInt(characterSet.length())).mapToObj(randomInt -> characterSet.substring(randomInt, randomInt + 1)).collect(Collectors.joining());
    }

}
