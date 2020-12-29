package com.example.demo.controller;

import com.example.demo.repository.ToDoRepository;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    @PostMapping("/addmodel")
    public ResponseEntity<String> addToDo(@RequestBody String todo) throws JSONException {
        //System.out.println(toDo.charAt(476));
        JsonParser parser = new JsonParser();
        JSONObject toDo =new JSONObject(todo);
        System.out.println(toDo.toString());
        //JsonObject todo = parser.parse(todo1.toString()).getAsJsonObject();
        Document doc=Document.parse(todo);
        //System.out.println(doc.toJson());
        //doc.toJson();
        mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> lsit=new ArrayList<>();
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

        return new ResponseEntity(toDo.toString(),headers, HttpStatus.OK);
    }
    @GetMapping("/getmodel")
    public List<Document> retrieveToDo(){
        Query query = new Query();
        //query.addCriteria(Criteria.where("nameValuePairs.partner").is(name));

        System.out.println(query.toString());
        return mongoTemplate.execute("jSONObject", new CollectionCallback<List<Document>>() {

            @Override
            public List<Document> doInCollection(MongoCollection<Document> mongoCollection) throws MongoException, DataAccessException {
                List<Document> lsit=new ArrayList<>();
                FindIterable<Document> cursor = mongoCollection.find();
                Iterator it = cursor.iterator();
                while (it.hasNext()){
                    lsit.add((Document) it.next());
                }
                return lsit;
            }
        });

    }
}
