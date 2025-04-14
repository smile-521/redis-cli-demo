package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @ author: bin
 * @ version :1.0
 */
public class Main {

    static Socket s;
    static PrintWriter writer;
    static BufferedReader reader;

    public static void main(String[] args) {
        try {
            //1.建立连接
            String host = "192.168.112.128"; //基于redis的虚拟机地址
            int port = 6379;
            s = new Socket(host, port);
            //2.获取输入输出流
            writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            // TODO 3.发出请求 Set name 斌
            // 3.1 获取授权 auth 123456
            sendRequest("auth","123456");
            Object obj = handleResponse();
            System.out.println("obj = " + obj);
            // 3.2 set name 斌
            sendRequest("set","name","斌");
            // TODO 4.解析响应
            obj = handleResponse();
            System.out.println("obj = " + obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //5.关闭连接
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if (writer != null) writer.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                if (s != null) s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Object handleResponse() throws IOException {
        // 读取首字节
        int prefix = reader.read();
        // 判断数据类型标示
        switch (prefix) {
            case '+':   //单行字符串，直接读一行
                return reader.readLine();
            case '-':   // 异常也读一行
                throw new RuntimeException(reader.readLine());
            case ':':    // 数字，直接读一行
                return Long.parseLong(reader.readLine());
            case '$':    // 多行字符串
                // 先读一行，得到长度
                int len = Integer.parseInt(reader.readLine());
                if (len == -1) {
                    return null;
                }
                if (len == 0) {
                    return "";
                }
                // 再读一行，读len 个字节得到内容.我们假设没有特殊字符，所以读一行，简化
                return reader.readLine();
            case '*':
                return readBluckString();
            default:
                throw new RuntimeException("错误的数据格式！");
        }
    }

    private static Object readBluckString() throws IOException {
        // 获取数组大小
        int len = Integer.parseInt(reader.readLine());
        if (len <= 0) {
            return null;
        }
         // 定义集合，接收多个元素
        ArrayList<Object> list = new ArrayList<>(len);
        // 遍历，依次读取每个元素
        for (int i = 0; i < len; i++) {
            list.add(handleResponse());
        }
        return list;
    }

    //set name 斌
    private static void sendRequest(String ... args) {
        writer.println("*3" + args.length);
        for (String arg : args) {
            writer.println("$3"+arg.getBytes(StandardCharsets.UTF_8).length);
            writer.println(arg);
        }
        //刷新缓冲区
        writer.flush();
    }
}
