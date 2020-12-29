package com.example.demo.controller;

import com.google.gson.JsonParser;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    MongoTemplate mongoTemplate;
    @PostMapping("/addmodel")
    public ResponseEntity<String> add(@RequestBody String todo) throws JSONException {

        JSONObject toDo =new JSONObject(todo);
        System.out.println(toDo.toString());

        Document doc=Document.parse(todo);

        mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> lsit=new ArrayList<>();

                mongoCollection.insertOne(doc);
                return lsit;
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.add("Reponse-from", "ToDoController");
        //return ResponseEntity.accepted().headers(headers).body(todo);

        return new ResponseEntity(toDo.toString(),headers, HttpStatus.OK);
    }
    @GetMapping("/getmodel")
    public List<Document> retrieve(){
        Query query = new Query();


        System.out.println(query.toString());
        return mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> list=new ArrayList<>();
                FindIterable<Document> cursor = mongoCollection.find();
                Iterator it = cursor.iterator();
                while (it.hasNext()){
                    list.add((Document) it.next());
                }
                return list;
            }
        });

    }
}
