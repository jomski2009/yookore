package com.yookos.yookore.rabbit;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

/**
 * Created by jome on 2014/08/28.
 */
public class CustomMessageListener implements MessageListener, ChannelAwareMessageListener {
    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

    }
}
