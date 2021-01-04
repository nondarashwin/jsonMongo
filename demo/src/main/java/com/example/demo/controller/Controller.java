package com.example.demo.controller;

import com.example.demo.repository.ToDoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.joining;

class DBObjectToStringConverter implements Converter<DBObject, String> {
    public String convert(DBObject source) {
        return source == null ? null : source.toString();
    }
}

@RestController
@RequestMapping("/api")
public class Controller {
    @Autowired
    ToDoRepository toDoRepository;
    @Autowired
    MongoTemplate mongoTemplate;


    RestTemplate restTemplate;

    @PostMapping("/addmodel")
    public ResponseEntity<String> addToDo(@RequestBody String todo) throws JSONException {
        //System.out.println(toDo.charAt(476));
        JsonParser parser = new JsonParser();
        JSONObject toDo = new JSONObject(todo);
        System.out.println(toDo.toString());
        //JsonObject todo = parser.parse(todo1.toString()).getAsJsonObject();
        Document doc = Document.parse(todo);

        //System.out.println(doc.toJson());
        //doc.toJson();
        mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> lsit = new ArrayList<>();
                //FindIterable<Document> cursor = mongoCollection.find();
                //Iterator it = cursor.iterator();
                //while (it.hasNext()){
                //  lsit.add((Document) it.next());
                //}
                mongoCollection.insertOne(doc);
                return lsit;
            }
        });
        //mongoTemplate.save(toDo);
        //mongoTemplate.save(doc);
        //toDoRepository.save(todo);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Reponse-from", "ToDoController");
        //return ResponseEntity.accepted().headers(headers).body(todo);

        return new ResponseEntity(toDo.toString(), headers, HttpStatus.OK);
    }

    @GetMapping("/getmodel")
    public List<Document> retrieveToDo() {
        Query query = new Query();
        //query.addCriteria(Criteria.where("nameValuePairs.partner").is(name));

        System.out.println(query.toString());
        return mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> lsit = new ArrayList<>();
                FindIterable<Document> cursor = mongoCollection.find();
                Iterator it = cursor.iterator();
                while (it.hasNext()) {
                    lsit.add((Document) it.next());
                }
                return lsit;
            }
        });

    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    @PostMapping("/postdata")
    public String getData(@RequestBody String data) throws Exception {
        //JsonParser parser = new JsonParser();
        JSONObject jsonData = new JSONObject(data);
        HashMap<String, String> map;
        Document doc = Document.parse(data);
        mongoTemplate.execute("requests", mongoCollection -> {
            List<Document> lsit = new ArrayList<>();
            mongoCollection.insertOne(doc);
            return lsit;
        });
        OkHttpClient client = new OkHttpClient();
        JSONObject apiData = jsonData.getJSONObject("apiData");
        Request.Builder request = new Request.Builder().url(apiData.getString("path"));
        if (apiData.get("method").equals("GET")) {
            ObjectMapper mapper = new ObjectMapper();
            map = (HashMap<String, String>) mapper.readValue(jsonData.get("formData").toString(), new TypeReference<Map<String, String>>() {
            });
            String combine="=";
            String combine1="&";
            String combine2="?";
            if(apiData.getString("uriType").equals("/")){
                combine="/";
                combine1="/";
                combine2="";
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
                request=request.get();

        }
        if (apiData.get("method").equals("POST")) {

            okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.get("application/json; charset=utf-8"), jsonData.get("formData").toString());
            request=request.post(body);

        }
        JSONArray headers=apiData.getJSONArray("headers");
        for(int i=0;i<headers.length();i++){
            JSONObject header= (JSONObject) headers.get(i);
            request=request.addHeader(header.getString("header"),header.getString("value"));
        }
        Call call = client.newCall(request.build());
        Response response = call.execute();
        return response.body().string();
    }

}
