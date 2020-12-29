package com.zjp.ex02;

import java.io.IOException;

/**
 * Created by stillhere on 2020/12/28.
 */
public class StaticResourceProcessor {
    public void process(Request request, Response response) {
        try {
            response.sendStaticResource();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
