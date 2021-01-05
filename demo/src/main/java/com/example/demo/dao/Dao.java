package com.example.demo.dao;

import com.mongodb.client.FindIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.bson.Document;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@org.springframework.stereotype.Service
public class Dao {
    @Autowired
    MongoTemplate mongoTemplate;
public void insert(String dbName, Document doc){

    mongoTemplate.execute(dbName, mongoCollection -> {
        List<org.bson.Document> lsit = new ArrayList<>();
        mongoCollection.insertOne(doc);
        return lsit;
    });
}
public List<Document> findAll(String dbName){
   return mongoTemplate.execute(dbName, mongoCollection -> {
        List<Document> lsit = new ArrayList<>();
        FindIterable<Document> cursor = mongoCollection.find();
        Iterator it = cursor.iterator();
        while (it.hasNext()) {
            lsit.add((Document) it.next());
        }
        return lsit;
    });
}
}
