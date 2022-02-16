package com.trellowithjava.service;

import com.trellowithjava.model.Card;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Trello {
    private static final String CREATE_CARD_URL = "https://api.trello.com/1/cards";

    String token;
    String key;
    OkHttpClient okHttpClient;

    public Trello(String token, String key) {
        this.key = key;
        this.token = token;
        okHttpClient = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
    }

    private String buildUrl(String url) {
        return url + "?key=" + this.key + "&token=" + this.token;
    }

    private boolean createQuery(String url, Map<String, String> map) {
        try {
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
            MultipartBody.Builder builder = bodyBuilder.setType(MultipartBody.FORM);
            for (String key : map.keySet()) {
                if(map.get(key) != null)
                    builder.addFormDataPart(key, map.get(key));
            }
            MultipartBody multipartBody = builder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", multipartBody)
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                System.out.println(response.body());
                return false;
            }
            System.out.println(response.body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }

    public boolean createCard(Card card) {
        try {
            Map<String, String> maps = convertCardToMapReflection(card);
            return createQuery(buildUrl(CREATE_CARD_URL), maps);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Map<String, String> convertCardToMapReflection(Card card) throws IllegalAccessException {
        Map<String, String> cardMap = new HashMap<String,String>();
        Field[] allFields = card.getClass().getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(card);
            if (value instanceof Date) {
                SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                cardMap.put(field.getName(), outFormat.format(value));
            } else {
                cardMap.put(field.getName(), (String) value);
            }
        }
        System.out.println(cardMap);
        return cardMap;
    }
}
