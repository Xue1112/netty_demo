package com.itheima.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @description
 * @author: ts
 * @create:2021-04-02 11:48
 */
public class BioServer {

    public static void main(String[] args) {
        //由Acceptor线程负责监听客户端的连接
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("服务端启动监听.......");
            while (true) {
                //Acceptor线程接收到客户端连接请求之后为每个客户端创建一个新的线程进行业务处理
                Socket socket = serverSocket.accept();
                System.out.println("成功接收一个客户端连接:"+socket.getInetAddress());
                new Thread(new ServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class ServerHandler implements  Runnable{

    private final Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            //获取客户端的输入流
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            System.out.println("准备接收来自客户端:"+this.socket.getInetAddress()+"的数据");
            //读取客户端发送过来的数据
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                System.out.println("成功接收来自客户端的数据:"+ line);
                //进行业务处理
                //给客户端响应数据
                out.write("success! i am server \n");
                out.flush();
            }
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
        }
    }
}
