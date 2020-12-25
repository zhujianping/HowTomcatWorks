package com.zjp.ex01;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by stillhere on 2020/12/25.
 */
public class Request {
    private InputStream input;
    private String uri;


    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buff = new byte[2048];
        try {
            i = input.read(buff);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char)buff[j]);
        }
        uri = parseUri(request.toString());

    }

    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(" ");
        if (index1 != -1) {
            index2 = requestString.indexOf(" ",index1 + 1);
            if (index2 > index1) {
                return requestString.substring(index1 + 1, index2);
            }
        }
        return null;
    }

    public String getUri() {
        return uri;
    }
}
