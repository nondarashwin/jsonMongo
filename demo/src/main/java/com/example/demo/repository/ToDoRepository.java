package com.example.demo.repository;


import org.json.JSONObject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToDoRepository extends MongoRepository<JSONObject, Integer> {


}
