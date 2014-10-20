package com.yookos.yookore.services.impl;

import com.yookos.yookore.domain.Activity;
import com.yookos.yookore.domain.User;
import com.yookos.yookore.services.UserService;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jome on 2014/08/29.
 */

@Service
public class UserServiceImpl implements UserService {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Datastore ds;

    @Autowired
    RestAPIFacade gd;

    @Autowired
    RestCypherQueryEngine engine;

    @Override
    public User createUser(User user) {
        try (Transaction tx = gd.beginTx()) {
            Map<String, Object> props = new HashMap<>();
            Map<String, Object> params = new HashMap<>();
            props.put("username", user.getUsername());
            props.put("userid", user.getUserid());
            props.put("firstname", user.getFirstName());
            props.put("lastname", user.getLastName());
            props.put("email", user.getEmail());
            props.put("creationdate", user.getCreationdate());
            props.put("name", user.getFirstName() + " " + user.getLastName());
            props.put("userenabled", true);
            props.put("age", user.getAge());
            props.put("birthdate", user.getBirthdate());
            if (user.getGender().equals(null) || user.getGender().equals("null")) {
                props.put("gender", "unknown");
            } else {
                props.put("gender", user.getGender());
            }
            props.put("lastloggedin", user.getLastLoggedIn());
            props.put("lastprofileupdate", user.getLastProfileUpdate());


            params.put("props", props);
            engine.query("create (p:Person{props})", params);
            params.clear();
            tx.success();

            //Save in mongo
            ds.save(user);
            return user;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void updateUser(User user) {
        try (Transaction tx = gd.beginTx()) {
            Map<String, Object> props = new HashMap<>();
            Map<String, Object> params = new HashMap<>();

            props.put("userid", user.getUserid());
            props.put("firstname", user.getFirstName());
            props.put("lastname", user.getLastName());
            props.put("email", user.getEmail());
            props.put("name", user.getFirstName() + " " + user.getLastName());
            props.put("userenabled", true);
            props.put("age", user.getAge());
            props.put("birthdate", user.getBirthdate());
            props.put("gender", user.getGender());
            props.put("lastprofileupdate", user.getLastProfileUpdate());
            props.put("lastloggedin", user.getLastLoggedIn());


            params.put("props", props);
            String query = "match (p:Person{userid:{userid}}) " +
                    "set p.firstname = {firstname} " +
                    "set p.lastname = {lastname} " +
                    "set p.name = {name} " +
                    "set p.age = {age} " +
                    "set p.birthdate = {birthdate} " +
                    "set p.gender = {gender} " +
                    "set p.lastloggedin = {lastloggedin} " +
                    "set p.lastprofileupdate = {lastprofileupdate} " +
                    "return p";

            engine.query(query, props);
            props.clear();
            tx.success();

            //Update in mongo
            //ds.save(user);
            //return user;
        } catch (Exception e) {
            log.error("Update User message: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(Long userid) {

    }


}
