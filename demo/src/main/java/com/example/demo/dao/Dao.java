package com.example.demo.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.bson.Document;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Dao {
    @Autowired
    MongoTemplate mongoTemplate;

    public Dao() {
        //MongoClient mongo = new MongoClient(  , 27017 );
        //this.mongoTemplate = new MongoTemplate();
    }

    public void insert(String dbName, Document doc){
    System.out.println(doc.toString());
System.out.println(dbName);
    mongoTemplate.save(doc,dbName);
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
public List<Document> find(String dbName,Document doc){
    return mongoTemplate.execute(dbName, mongoCollection -> {
        List<Document> lsit = new ArrayList<>();
        FindIterable<Document> cursor = mongoCollection.find(doc);
        Iterator it = cursor.iterator();
        while (it.hasNext()) {
            lsit.add((Document) it.next());
        }
        return lsit;
    });
}
}
