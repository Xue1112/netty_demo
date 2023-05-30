package com.itheima.netty;

import com.alibaba.fastjson.JSON;
import com.itheima.netty.pojo.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @description
 * @author: ts
 * @create:2021-06-03 15:13
 */
public class JsonTest {

    public static void main(String[] args) {

    /*UserInfo userInfo = new UserInfo(1,"aaa",18,"nan","aa");
        byte[] bytes = JSON.toJSONBytes(userInfo );

        System.out.println(bytes);

        Object parse = JSON.parse(bytes);
        UserInfo object = JSON.parseObject(bytes, UserInfo.class);
        System.out.println(object);*/

        PriorityQueue<Integer> queue = new PriorityQueue<>((x,y) ->{
            return y -x;
        });

    }
}
