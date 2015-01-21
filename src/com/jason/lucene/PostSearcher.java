package com.jason.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.jason.util.Constant;
import com.jason.util.Printer;
import com.jason.vo.PostVO;

/**
 * 
 * @author Jason Liu
 *
 */
public class PostSearcher {
	
	public PostSearcher(){
		this.totalnum = 0;
	}
	
	/**
	 * 获取 index reader 
	 * @param path
	 * @return
	 */
	public IndexReader getIndexReader(String path){
		Directory dir;
		IndexReader reader = null;
		try {
			dir = FSDirectory.open(new File(path));
			reader = DirectoryReader.open(dir);
		} catch (IOException e) {	
			e.printStackTrace();
		}
        return reader;
	}
	
	public String[] processSearchText(String searchText){
		if( searchText == null || searchText ==""){
			return null;
		}
		String trimed = searchText.trim();
		
		Pattern p_multispace = Pattern.compile("\\s+\\s+|\t|\r|\n", Pattern.CASE_INSENSITIVE);  
        Matcher m_multispace = p_multispace.matcher(trimed);  
        String repalced = m_multispace.replaceAll(" ");
//        System.out.println("REP "+repalced);
		String[] res = repalced.split(" ");
		return res;
	}
	
	/**
	 * 搜索
	 * @param searchText
	 * @return
	 */
	public List<PostVO> search(String searchText,int page,int perPage,Analyzer analyzer){
		String err = "";
		if( searchText ==null || searchText == "" ){
			return null;
		}
		
		List<PostVO> res = new LinkedList<PostVO>();
		
        IndexReader ir = getIndexReader( Constant.IDX_DIR);
        IndexSearcher searcher = new IndexSearcher(ir);
		
        BooleanQuery bQuery = new BooleanQuery();
        String contentField = "content";
        String nameField = "name";
        
        QueryParser contentParser = new QueryParser(Version.LUCENE_4_10_0, contentField, analyzer);
        QueryParser nameParser = new QueryParser(Version.LUCENE_4_10_0, nameField, analyzer);
        
        Query contentQuery;
        Query nameQuery;
        
		try {
			bQuery = new BooleanQuery();
			contentQuery = contentParser.parse(searchText);
			nameQuery = nameParser.parse(searchText);
			
		    //post_title,post_content都查询
			bQuery.add(contentQuery , Occur.SHOULD);
			bQuery.add(nameQuery ,Occur.SHOULD);
//			searcher.search(query, null, num);
			TopDocs results;
			
			try {
				
				results = searcher.search( bQuery, Constant.SEARCH_PAGES *perPage);
				int numTotalHits = results.totalHits;
				this.totalnum = numTotalHits;
				if( numTotalHits>0 ){
					ScoreDoc[] hits = results.scoreDocs;
					
					System.out.println( numTotalHits + " total matching documents");
					
					/**
					 * perpage=3
					 * page 1,s 0,e 3
					 * page 2,s 3,e 6
					 */
					int start = (page-1) * perPage;
					int end = 0;
					int pages =(int) Math.ceil((double)numTotalHits/(double)perPage);
					
					if(page>pages){
						err = "索取页超过总页数";
					}else{
						if( numTotalHits <= perPage){
							end = numTotalHits;
						}else{
							if( page < pages){
								end = start+perPage;
							}else{
								int add = numTotalHits%perPage;
								System.out.println( "mod "+add );
								if( add ==0 ){
									end = start+perPage;
								}else{
									end = start +add;
								}
							}
						}
						System.out.println("start:"+start+",end:"+end);
						// 4test
//						for(int i=0;i<=numTotalHits;i++){
//					    	System.out.println(i);
//					    	Document doc = searcher.doc(hits[i].doc);
//							System.out.println( "id:"+doc.get("id")+",name:"+doc.get("name") );
//					    }
						
						for (int i = start; i < end; i++) {
							System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
							Document doc = searcher.doc(hits[i].doc);
							System.out.println( "id:"+doc.get("id")+",name:"+doc.get("name") );
							PostVO pvo = new PostVO();
							pvo.setId(doc.get("id"));
							res.add(pvo);
//							doc.get("id");
						}
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return res;
    }
	
	
	
	
	private int totalnum;
	/*
	public void doPagingSearch( IndexSearcher searcher, Query query,int page, int hitsPerPage ) {
		TopDocs results;
		try {
			results = searcher.search(query, 5 * hitsPerPage);
			int numTotalHits = results.totalHits;
			ScoreDoc[] hits = results.scoreDocs;
			this.totalnum = numTotalHits;
			System.out.println(numTotalHits + " total matching documents");
			
			int start = 0;
		    int end = Math.min(numTotalHits, hitsPerPage);
		    
		    System.out.println("start:"+start+",end:"+end);
		    
		    for (int i = start; i < end; i++) {
				System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
				Document doc = searcher.doc(hits[i].doc);
				System.out.println( "id:"+doc.get("id")+",name:"+doc.get("name") );
				
//				Document doc = searcher.doc(hits[i].doc);
//				String path = doc.get("path");
//				if (path != null) {
//					System.out.println((i + 1) + ". " + path);
//					String title = doc.get("title");
//					if (title != null) {
//						System.out.println("   Title: " + doc.get("title"));
//					}
//				} else {
//					System.out.println((i + 1) + ". "
//							+ "No path for this document");
//				}

			}
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
//		ScoreDoc[] hits = results.scoreDocs;
//		
//		Document doc = searcher.doc(hits[i].doc);
	}
	*/
	
	public String searchPost(String searchText,int page,int perPage){
		String res = "";
		List<PostVO> l = search(searchText, page,perPage,new PaodingAnalyzer());
		res = this.totalnum+"#";
		int len = l.size();
		if(len>0){
			for(int i=0;i<len ;i++){
				if(i==len-1){
					res += l.get(i).getId();
				}else{
					res += l.get(i).getId()+",";
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		PostSearcher sr = new PostSearcher();
		String searchStr = "序";
//		List<PostVO> l = sr.search(searchStr, 1,10,new PaodingAnalyzer());
		String res = sr.searchPost(searchStr,1,10);
		System.out.println("res:"+res);
		
		
		/*
		int page=3,perPage=5,numTotalHits=10;
		
		/**
		 * perpage=3
		 * page 1,s 0,e 3
		 * page 2,s 3,e 6
		 * 
		 *
		
		
		int start = (page-1) * perPage;
		int end = 0;
		int pages =(int) Math.ceil((double)numTotalHits/(double)perPage);
		String err;
		if(page>pages){
			err = "索取页超过总页数";
		}else{
			if( numTotalHits <= perPage){
				end = numTotalHits;
			}else{
				if( page < pages){
					end = start+perPage;
				}else{
					int add = numTotalHits%perPage;
					System.out.println( "mod "+add );
					if( add ==0 ){
						end = start+perPage;
					}else{
						end = start +add;
					}
				}
			}
		}
		
//		end++;
		System.out.println("start:"+start+",end:"+end);
		
		*/
//		sr.search("工具",10);
		
//		String searchStr = "  	Doctor问答数据库 世界很美丽	送到	";
//		public List<PostVO> search(String searchText,int page,int perPage,Analyzer analyzer){
		
		
//		String [] sa = sr.processSearchText(searchStr);
//		Printer.printStringArr(sa);
		
//		List<String> ls = sr.getWords(sa, new PaodingAnalyzer() );
//		Printer.printList(ls);
	}
}
