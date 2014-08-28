package com.yookos.yookore.services.impl;

import com.yookos.yookore.domain.notification.MyUser;
import com.yookos.yookore.services.MyService;

/**
 * Created by jome on 2014/08/28.
 */
public class MyServiceImpl implements MyService {
    @Override
    public int addNumbers(int a, int b) {
        return a + b;
    }

    @Override
    public double divideNumbers(double dividend, double divisor){
        if(divisor == 0){
            throw new IllegalArgumentException("The divisor cannot be zero");
        }
        return dividend / divisor;
    }

    @Override
    public MyUser getUserById(int userid) {
        return null;
    }
}
