package com.itheima.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer2 {
    //编写一个服务端程序 基于Socket
    public static void main(String[] args) throws Exception{

        ServerSocket serverSocket = new ServerSocket(8888);

        while (true) {
            Socket socket = serverSocket.accept();//接收客户端连接 accept是阻塞的
            new Thread(new ServerHandler(socket) ).start();
        }
    }

    static class ServerHandler implements Runnable {

        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                //读取客户端发送过来的数据
                while (true) {
                    String line = reader.readLine();
                    System.out.println("服务端收到来自客户端的数据:"+line);

                    //服务端向客户端写回数据
                    writer.write("hello bioclient,i am bioserver \n");
                    writer.flush();
                }
            } catch (Exception e) {

            }

        }
    }
}
