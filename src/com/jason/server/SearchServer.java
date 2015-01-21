package com.jason.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import net.sf.json.JSONObject;

import com.jason.lucene.GenerateIndex;
import com.jason.lucene.PostSearcher;
import com.jason.util.Constant;
import com.jason.vo.SocketResult;

public class SearchServer {
	private static final int PORT = 5050;
	public static void main(String[] args) {
		
//		SocketResult res_obj = null;
//		res_obj = new SocketResult("false","aaa");
//		
//		JSONObject output = JSONObject.fromObject(res_obj);
//		System.out.println(output.toString() );
		
		ServerSocket server;
		try {
			server = new ServerSocket(PORT);
			if(server!=null){
				System.out.println("Server listen on:"+PORT);
			}
			while (true) {
				Socket client;
				try {
					String err="";
					client = server.accept();
					System.out.println("client connected!\n");
					PrintWriter printer = new PrintWriter(
							client.getOutputStream());
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(client.getInputStream()));

					String oneline = reader.readLine();
					
					System.out.println(client.getInetAddress().toString() +":" +client.getPort()+"[" + oneline + "]");
					String data = "";
					if( oneline.length()>0 ){
						String[] func_param = oneline.split("#");
						if(func_param.length==2){//func with param
							String funcName = func_param[0];
							int funcNameCode = Integer.parseInt( funcName);
//							System.out.println("")
							String funcParam = func_param[1];
							if( funcParam.length()>0 ){
								switch(funcNameCode){
								case Constant.SEARCHNC:
									String[] params = funcParam.split(",");
									if(params.length==3){
										String searchText = params[0];
										int page = Integer.parseInt(params[1]);
										int perPage = Integer.parseInt(params[2]);
										System.out.println("text:"+searchText+",page:"+page+",perpage:"+perPage);
										PostSearcher sr = new PostSearcher();
										data = sr.searchPost(searchText,page,perPage);
										
									}else{
										err = "参数数量错误";
									}
									break;
								case Constant.ADDONE://ADDONE#id
									String id_str = funcParam;
									int pid = Integer.parseInt(id_str);
									GenerateIndex gi = new GenerateIndex();
									boolean addidx_res = gi.addIndex(Constant.IDX_DIR,pid);
									if(addidx_res){
										data = "success"; 
									}else{
										err = "添加"+id_str+"索引失败";
									}
									break;
								default:
									err = "未知函数";
									break;
								}
							}else{
								err = "参数错误";
							}
//						protocol:
//							FUNCNAME#
//							SEARCH#searchtext,page,perpage
							
							
						}else if(func_param.length ==1){// only function,no parameter
							String funcName = func_param[0];
							int funcNameCode = Integer.parseInt(funcName);
							switch(funcNameCode){
							case Constant.REIDX:// REIDX#0
								GenerateIndex gi = new GenerateIndex();
								boolean reidx_res = gi.GenerateAllIndex(Constant.IDX_DIR);
								if(reidx_res){
									data = "success";
								}else{
									err = "重建索引失败";
								}
								break;
							default:
								err = "未知函数";
								break;
							}
						}else{
							err = "请求格式错误";
						}
					}else{
				   		err="请求为空";
					}
					SocketResult res_obj = null;
					if(err.length()>0){
						res_obj = new SocketResult("false",err);
					}else{
						res_obj = new SocketResult("true",data);
					}
					JSONObject output = JSONObject.fromObject(res_obj);
					System.out.println("res:"+output.toString());
					printer.println( output.toString() );
					printer.flush();
					reader.close();
					printer.close();
					client.close();
					System.out.println("client leaving!\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				client = null;
			}//end of while

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server Shutdown on port:"+PORT);
		
	}

}
