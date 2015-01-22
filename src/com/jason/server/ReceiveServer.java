package com.jason.server;

import com.jason.util.Constant;

import java.net.ServerSocket;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;

/**
 * class receive server
 * 
 * @author Jason Liu
 *
 */
public class ReceiveServer {
//	private 
//    private 
    private static final int POOLSIZE = 100;
    ExecutorService pool;
    //receiveServer的构造器
    public ReceiveServer() {
    	this.pool = Executors.newCachedThreadPool();//(POOLSIZE);
    }
    
    
    
    public void startServer(){
    	Thread receiveThread;
    	ServerSocket rServer;//ServerSocket的实例
    	Socket request; //用户请求的套接字
    	
        try {
            rServer = new ServerSocket( Constant.LISTEN_PORT );
            if(rServer!=null){
                System.out.println("Server listen on:"+ Constant.LISTEN_PORT );

                while (true) { //等待用户请求
                    request = rServer.accept();//接收客户机连接请求
                    receiveThread = new SearchThread(request);//生成serverThread的实例
                    this.pool.execute(receiveThread);
//                    receiveThread.start();//启动serverThread线程
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
