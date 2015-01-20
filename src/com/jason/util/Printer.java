package com.jason.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;

import com.jason.vo.PostVO;

public class Printer {
	public static void printList(List l){
		int i= 0;
		System.out.println("List:");
		int len = l.size();
		for (Object o : l) {
			if(i== len-1 ){
				System.out.print(o);
			}else{
				System.out.print(o+",");
			}
            
            i++;
        }
	}
	
	public static void printStringArr(String [] sa){
		System.out.println("StringArr:");
		for(int i=0;i<sa.length;i++){
			if(i==sa.length-1){
				System.out.println(sa[i]);
			}else{
				System.out.print(sa[i]+",");
			}
		}
		
	}
	public static void printHashMap(HashMap hm){
		Iterator iter = hm.entrySet().iterator();
		int size = hm.size();
		int i=0;
		System.out.println("HashMap:");
		while (iter.hasNext()) {
			
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			System.out.println("["+key+","+val+"]");
//			if(i==size-1){
//				
//			}else{
//				System.out.print("["+key+","+val+"],");
//			}
			i++;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
