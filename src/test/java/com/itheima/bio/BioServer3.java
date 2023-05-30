package com.itheima.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class BioServer3 {
    // socket 编程
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8888);

        while (true) {
            Socket socket = serverSocket.accept();//阻塞式
            new Thread(new ServerHandler(socket)).start();
        }
    }


    private static class ServerHandler implements Runnable {

        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                // 字符
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                // 读取客户端发送过来的数据
                while (true) {
                    String line = reader.readLine();
                    log.info("服务端收到了来自客户端是数据:{}",line);

                    //像客户端写点数据
                    writer.write("hello bioclient,i am bioserver\n");
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
