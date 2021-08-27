package com.hey.util;

import com.hey.cache.client.RedisCacheExtend;
import io.vertx.core.Future;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public final class GenerationUtils {
    private static RedisCacheExtend redis;

    public static String generateUUID() {return UUID.randomUUID().toString();}

    public static void setRedis(RedisCacheExtend redisCacheExtend){redis = redisCacheExtend;}

    public static String generateRandomId(){
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd-"));
        Random rand = new Random();
        int num = (rand.nextInt()&Integer.MAX_VALUE)%100000000;
        return date + num;
    }

    public static Future<String> generateId(String s){
        Future<String> future = Future.future();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Future<String> getCounter = redis.getCounter(s+"_"+date);
        getCounter.setHandler(stringAsyncResult -> {
            if (stringAsyncResult.succeeded()){
                String number = stringAsyncResult.result();
                while (number.length() < 8){
                    number = "0" + number;
                }
                future.complete(date + "-" + number);
            }
            else
                future.fail(stringAsyncResult.cause());
        });
        return future;
    }
}
