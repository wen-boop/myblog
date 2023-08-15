package com.gxtb.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/11 17:42
 */
@Configuration
public class RabbitMqConfig {
    public static String EXCHANGE_NAME="exActivity";

    @Bean("exActivity")
    public Exchange exchange(){
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(true).build();
    }

    //队列
    @Bean("attendActivity")
    public Queue queue(){
        return QueueBuilder.durable("attendActivity").build();
    }

    @Bean
    public Binding binding(@Qualifier("attendActivity") Queue queue, @Qualifier("exActivity") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("activity").noargs();
    }
}
