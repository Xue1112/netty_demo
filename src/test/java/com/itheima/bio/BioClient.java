package com.itheima.bio;

import java.io.*;
import java.net.Socket;

/**
 * @description
 * @author: ts
 * @create:2021-04-02 11:48
 */
public class BioClient {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket("127.0.0.1",8888);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("准备向服务端写数据!");
            //向服务端写数据
            out.write("hello server , i am client ! \n");//注意别丢 \n 因为服务端是readLine
            out.flush();
            //接收来自服务端的数据
            String line = in.readLine();
            System.out.println("成功接收到来自服务端的数据:"+line);
        } catch (IOException e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
