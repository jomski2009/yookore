package com.yookos.yookore.services.impl;

import com.yookos.yookore.services.MyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Created by jome on 2014/08/28.
 */
public class MyServiceTest {
    MyService myService;

    @Before
    public void setup() {
        myService = new MyServiceImpl();

    }

    @After
    public void teardown() {
        myService = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void addNumbers() {
        assertEquals("2 + 2 must be 4", 4, myService.addNumbers(2, 2));
    }

    @Test
    public void dividingNumbersThrowsIllegalExceptionIfDivisorIsZero() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("The divisor cannot be zero");
        myService.divideNumbers(10,0);
    }
}
