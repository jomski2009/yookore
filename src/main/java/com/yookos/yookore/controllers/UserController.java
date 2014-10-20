package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.JiveCoreUser;
import com.yookos.yookore.domain.User;
import com.yookos.yookore.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jome on 2014/08/29.
 */

@RestController
@RequestMapping(value = "users")
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("create")
    public ResponseEntity<String> createUser(@RequestBody JiveCoreUser cur) {
        User user = new User();

        user.setAge(cur.getAge());
        user.setBirthdate(cur.getBirthdate());
        user.setCreationdate(cur.getCreationDate());
        user.setEmail(cur.getEmail());
        user.setEnabled(cur.isEnabled());
        user.setFirstName(cur.getFirstName());
        user.setLastName(cur.getLastName());
        user.setGender(cur.getGender());
        user.setUserid(cur.getUserid());
        user.setLastLoggedIn(cur.getLastLoggedIn());
        user.setLastProfileUpdate(cur.getLastProfileUpdate());
        user.setUsername(cur.getUsername());

        userService.createUser(user);
        return new ResponseEntity<String>("Created...", HttpStatus.CREATED);

    }

    @RequestMapping("user/profile/update")
    public ResponseEntity<String> updateUser(@RequestBody User user){
        userService.updateUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("{userid}/delete")
    public ResponseEntity<Void> deleteUser(@PathVariable("userid") Long userid) {
        userService.deleteUser(userid);
        return new ResponseEntity<Void>(HttpStatus.CREATED);

    }

}
