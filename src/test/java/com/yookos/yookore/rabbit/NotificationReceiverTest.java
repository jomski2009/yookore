package com.yookos.yookore.rabbit;

import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by jome on 2014/08/28.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationReceiverTest {
    @Mock
    private Environment environment;

    @Mock
    private RestTemplate template;

    @Mock
    private MongoClient mongoClient;

    @InjectMocks
    private NotificationReceiver notificationReceiver = new NotificationReceiver();

    @Before
    public void setup() {
    }

    @Test
    public void testThatDependenciesAreWiredUp() {
    }


}
