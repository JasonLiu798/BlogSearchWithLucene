package com.jason.server;

/**
 * Created by liujianlong on 15/1/21.
 */

import com.jason.util.Constant;

import java.net.ServerSocket;

import java.io.*;
import java.util.*;
import java.net.*;

public class ReceiveServer {


    //receiveServer的构造器
    public ReceiveServer() {

    }

    public void startServer(){
        ServerSocket rServer;//ServerSocket的实例
        Socket request; //用户请求的套接字
        Thread receiveThread;
        try {
            rServer = new ServerSocket( Constant.LISTEN_PORT );
            if(rServer!=null){
                System.out.println("Server listen on:"+ Constant.LISTEN_PORT );

                while (true) { //等待用户请求
                    request = rServer.accept();//接收客户机连接请求
                    receiveThread = new ProcessThread(request);//生成serverThread的实例
                    receiveThread.start();//启动serverThread线程
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) {
        ReceiveServer rs = new ReceiveServer();
        rs.startServer();
    } //end of main

} //end of class
