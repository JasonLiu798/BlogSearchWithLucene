package com.jason.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class PackedAnalyzer {

	public static List<String> getWords(String[] str,Analyzer analyzer){  
	    List<String> result = new ArrayList<String>();  
	    TokenStream stream = null;
	    for(int i=0;i<str.length;i++){
	    	try {  
		        stream = analyzer.tokenStream("content", new StringReader(str[i]));  
		        CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);  
		        stream.reset();
		        while(stream.incrementToken()){  
		            result.add(attr.toString());
		        }
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }finally{  
		        if(stream != null){  
		            try {  
		                stream.close();  
		            } catch (IOException e) {  
		                e.printStackTrace();  
		            }  
		        }  
		    }
	    }
	    return result;  
	}
	
	/**
	 * 获取分词
	 * @param str
	 * @param analyzer
	 * @return
	 */
	public List<String> getWords(String str,Analyzer analyzer){  
	    List<String> result = new ArrayList<String>();  
	    TokenStream stream = null;
	    try {  
	        stream = analyzer.tokenStream("content", new StringReader(str));  
	        CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);  
	        stream.reset();
	        while(stream.incrementToken()){  
	            result.add(attr.toString());
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }finally{  
	        if(stream != null){  
	            try {  
	                stream.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }  
	    return result;  
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
