package com.jason.server;



import com.jason.lucene.GenerateIndex;
import com.jason.lucene.PostSearcher;
import com.jason.util.Constant;
import com.jason.util.DBUtil;
import com.jason.vo.SocketResult;

import net.sf.json.JSONObject;

import java.io.*;
import java.net.Socket;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;


/**
 * Created by liujianlong on 15/1/21.
 */
public class SearchThread  extends Thread {
	private static Logger logger = Logger.getLogger(SearchThread.class);
	private DataSource basicDataSource;
	
    Socket clientRequest;
    //用户连接的通信套接字
    BufferedReader input; //输入流
    PrintWriter output; //输出流

    public SearchThread(Socket s) {
    	this.basicDataSource = DBUtil.getDataSource();
    	
        this.clientRequest = s;
        System.out.println(s.getInetAddress().toString() +":" +s.getPort()+"connected.");
        InputStreamReader reader;
        OutputStreamWriter writer;
        try { //初始化输入、输出流
            reader = new InputStreamReader(clientRequest.getInputStream());
            writer = new OutputStreamWriter(clientRequest.getOutputStream());
            input = new BufferedReader(reader);
            output = new PrintWriter(writer, true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * thread run
     */
    public void run() {
        String str = null;
        
        System.out.println("Thread "+Thread.currentThread().getName() + " Started.");
        
        boolean done = false;
        while (!done) {
            try {
                str = input.readLine(); // RAW Message from client
            } catch (IOException e) {
                done = true;
                System.out.println(e.getMessage());
            }
            if(str==null ) {
                done=true;//未空则 结束
            }else{
                String[] func_param = str.trim().toUpperCase().split("#");
                String data = null;
                String err = "";
                /**
                 * func with parameter
                 */
                if(func_param.length==2){
                    String funcName = func_param[0];
                    int funcNameCode = Integer.parseInt( funcName);
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
                                GenerateIndex gi = new GenerateIndex(this.basicDataSource);
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
                        err = "未传递参数";
                    }
                }
                /**
                 * only function,no parameter
                 */
                else if(func_param.length ==1)
                {
                    String funcName = func_param[0];
                    int funcNameCode = Integer.parseInt(funcName);
                    switch(funcNameCode){
                        case Constant.REIDX:// REIDX#0
                            GenerateIndex gi = new GenerateIndex(this.basicDataSource);
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
                SocketResult res_obj;
                if(err.length()>0){
                    res_obj = new SocketResult("false",err);
                }else{
                    res_obj = new SocketResult("true",data);
                }
                JSONObject res_json = JSONObject.fromObject(res_obj);
                System.out.println("res:"+res_json.toString());
                output.println( res_json.toString() );
            }
        }//end of while
        try{
            input.close();
            output.close();
            clientRequest.close(); //关闭套接字
            System.out.println("Thread "+Thread.currentThread().getName() + " End.");
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        
    }
}
