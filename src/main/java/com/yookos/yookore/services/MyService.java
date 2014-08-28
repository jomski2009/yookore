package com.yookos.yookore.services;

import com.yookos.yookore.domain.notification.MyUser;

/**
 * Created by jome on 2014/08/28.
 */
public interface MyService {
    int addNumbers(int a, int b);

    double divideNumbers(double dividend, double divisor);

    MyUser getUserById(int userid);
}
