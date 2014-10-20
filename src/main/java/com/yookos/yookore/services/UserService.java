package com.yookos.yookore.services;

import com.yookos.yookore.domain.Activity;
import com.yookos.yookore.domain.User;

import java.util.List;

/**
 * Created by jome on 2014/08/29.
 */
public interface UserService {
    User createUser(User user);

    void updateUser(User user);

    void deleteUser(Long userid);

}
