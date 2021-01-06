package com.example.demo.controller;

import com.example.demo.dao.Dao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.joining;

@RestController
@RequestMapping("/api")
public class Controller {
    @Autowired
    MongoTemplate mongoTemplate;

    @PostMapping("/config")
    public ResponseEntity<String> addConfig(@RequestBody String data) throws JSONException {

        JSONObject toDo = new JSONObject(data);
        System.out.println(toDo.toString());
        Document doc = Document.parse(data);

        mongoTemplate.execute("jSONOBJECT", mongoCollection -> {
            List<org.bson.Document> lsit = new ArrayList<>();
            mongoCollection.insertOne(doc);
            return lsit;
        });
        HttpHeaders headers = new HttpHeaders();
        headers.add("Reponse-from", "ToDoController");
        return new ResponseEntity(toDo.toString(), headers, HttpStatus.OK);
    }

    @GetMapping("/config/{category}/{partner}/{product}")
    public List<Document> retrieveConfig(@PathVariable String category, @PathVariable String partner, @PathVariable String product) {
        Document doc = new Document();
        doc.put("category", category);
        doc.put("partner", partner);
        doc.put("product", product);
        return mongoTemplate.execute("jSONObject", mongoCollection -> {
            List<Document> lsit = new ArrayList<>();
            FindIterable<Document> cursor = mongoCollection.find(doc);
            Iterator it = cursor.iterator();
            while (it.hasNext()) {
                lsit.add((Document) it.next());
            }
            return lsit;
        });

    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    @PostMapping("/response")
    public String getData(@RequestBody String data) throws Exception {
        JSONObject jsonData = new JSONObject(data);
        HashMap<String, String> map;
        OkHttpClient client = new OkHttpClient();
        JSONObject apiData = jsonData.getJSONObject("apiData");
        Request.Builder request = new Request.Builder().url(apiData.getString("path"));
        if (apiData.get("method").equals("GET")) {
            ObjectMapper mapper = new ObjectMapper();
            map = (HashMap<String, String>) mapper.readValue(jsonData.get("formData").toString(), new TypeReference<Map<String, String>>() {
            });
            String combine = "=";
            String combine1 = "&";
            String combine2 = "?";
            if (apiData.getString("uriType").equals("/")) {
                combine = "/";
                combine1 = "/";
                combine2 = "";
            }
            HashMap<String, String> finalMap = map;
            String finalCombine = combine;
            String encodedURL = map.keySet().stream()
                    .map(key -> {
                                try {
                                    return key + finalCombine + encodeValue(finalMap.get(key));
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                return "";
                            }

                    )
                    .collect(joining(combine1, apiData.get("path") + combine2, ""));
            System.out.println(encodedURL);

            request = new Request.Builder().url(encodedURL);
            request = request.get();

        }
        if (apiData.get("method").equals("POST")) {

            okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.get("application/json; charset=utf-8"), jsonData.get("formData").toString());
            request = request.post(body);

        }
        JSONArray headers = apiData.getJSONArray("headers");
        for (int i = 0; i < headers.length(); i++) {
            JSONObject header =  headers.getJSONObject(i);
            if (!header.getString("header").equals("") && !header.getString("value").equals("")) {
                request = request.addHeader(header.getString("header"), header.getString("value"));
            }
        }
        Call call = client.newCall(request.build());
        Response response = call.execute();
        String resData = response.body().string();
        JSONObject dbData = new JSONObject();
        dbData.put("userData", jsonData.getJSONObject("formData"));
        dbData.put("category", jsonData.getString("category"));
        dbData.put("partner", jsonData.getString("partner"));
        dbData.put("product", jsonData.getString("partner"));
        dbData.put("result", new JSONObject(resData));
        Document doc = Document.parse(dbData.toString());
        System.out.println(doc.toString());
        mongoTemplate.execute("requests", mongoCollection -> {
            List<org.bson.Document> lsit = new ArrayList<>();
            mongoCollection.insertOne(doc);
            return lsit;
        });
        return resData;
    }
}
