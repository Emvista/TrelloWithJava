package com.trellowithjava.service;

import com.google.gson.Gson;
import com.trellowithjava.model.Card;
import com.trellowithjava.model.Label;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class Trello {
    private static final String CREATE_CARD_URL = "https://api.trello.com/1/cards";
    private static final String CREATE_LABEL_URL = "https://api.trello.com/1/labels";
    private static final String GET_LABELS_ON_BOARD_URL = "https://api.trello.com/1/boards/{id}/labels";

    String token;
    String key;
    OkHttpClient okHttpClient;

    public Trello(String token, String key) {
        this.key = key;
        this.token = token;
        okHttpClient = new OkHttpClient().newBuilder()
                .build();
    }

    private String buildUrl(String url) {
        return url + "?key=" + this.key + "&token=" + this.token;
    }

    public boolean createCard(Card card) {
        try {
            Map<String, String> maps = convertTrelloObjectToMapReflection(card);
            return createPostQuery(buildUrl(CREATE_CARD_URL), maps);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createLabel(Label label) {
        try {
            Map<String, String> maps = convertTrelloObjectToMapReflection(label);
            return createPostQuery(buildUrl(CREATE_LABEL_URL), maps);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Label> getLabelsByBoardId(String idBoard) {
        String url = GET_LABELS_ON_BOARD_URL.replace("{id}", idBoard);
        Gson gson = new Gson();
        String json = createGetQuery(buildUrl(url));
        if (json == null)
            return new ArrayList<>();
        List<Label> labels = gson.fromJson(json, ArrayList.class);
        return labels;
    }

    Label fromObject(Object o) {
        return (Label) o;
    }

    private String createGetQuery(String url) {
        try {
            Request request = new Request.Builder()
                .url(url)
                .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            if (response.code() != 200) {
                System.out.println("error code : " + response.code());
                System.out.println(response.body().string());
                return null;
            }
            String bodyString = response.body().string();
            response.close();
            return bodyString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createPostQuery(String url, Map<String, String> map) {
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
                return false;
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }

    private static Map<String, String> convertTrelloObjectToMapReflection(Object object) throws IllegalAccessException {
        Map<String, String> cardMap = new HashMap<String,String>();
        Field[] allFields = object.getClass().getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(object);
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
