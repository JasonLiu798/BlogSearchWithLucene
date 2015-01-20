package com.jason.lucene;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jason.util.Constant;
import com.jason.util.DBUtil;
import com.jason.util.Printer;
import com.jason.vo.PostVO;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class GenerateIndex {
	
	private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式  
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式  
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式  
    //private static final String regEx_space = "\\s+\\s+|\t|\r|\n";//多个空格，回车，换行符，转为单个空格
    private static final String regEx_space = "\t|\r|\n";//回车，换行符，转为单个空格
    private static final String regEx_multispace = "\\s+\\s+";//多个空格，转为单个
//    private BasicDataSource basicDataSource;
    
    //索引目录
    
    
	public String removeHtmlTag(String htmlStr){
		if(htmlStr ==null ){
			return null;
		}
		if( htmlStr.length() <=0){
			return "";
		}
		Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);  
        Matcher m_script = p_script.matcher(htmlStr);  
        htmlStr = m_script.replaceAll(""); // 过滤script标签  
  
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);  
        Matcher m_style = p_style.matcher(htmlStr);  
        htmlStr = m_style.replaceAll(""); // 过滤style标签  
        
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);  
        Matcher m_html = p_html.matcher(htmlStr);  
        htmlStr = m_html.replaceAll(""); // 过滤html标签  
  
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);  
        Matcher m_space = p_space.matcher(htmlStr);  
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
        
        Pattern p_multispace = Pattern.compile(regEx_multispace, Pattern.CASE_INSENSITIVE);  
        Matcher m_multispace = p_multispace.matcher(htmlStr);  
        htmlStr = m_multispace.replaceAll(" "); // 过滤空格回车标签
        
        //String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""); 
        htmlStr = htmlStr.replaceAll("&nbsp;", "").replaceAll("\\&[a-zA-Z]{1,10};", "");
        return htmlStr.trim(); // 返回文本字符串 
	}
	
	public List<PostVO> getAllContentFromDatabase(){
//		HashMap<Integer,String> res = new HashMap<Integer,String>();
		List<PostVO> res = new LinkedList<PostVO>();
		Connection conn =null;
		PreparedStatement st = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select ID,post_title,post_content from posts";
//			PreparedStatement prest = conn.prepareStatement(sql);
//			prest.setInt(1, id);
			st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
//			rs = prest.executeQuery();
			while (rs.next()) {
				PostVO tmp = new PostVO();
				tmp.setId(rs.getInt("ID")+"");
				tmp.setName(rs.getString("post_title"));
				tmp.setContent( this.removeHtmlTag(rs.getString("post_content") ) );
				res.add(tmp);
				
//                res.put( rs.getInt("ID"),tmp );
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
		return res;
	}
	
	/**
	 * 获取对象
	 * @param id
	 * @return
	 */
	public PostVO getPostVO(int id){
//		HashMap<Integer,String> hm = new HashMap<Integer,String>();
//		String res = "";
		Connection conn = null;
		PostVO res = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select ID,post_title,post_content from posts where ID=? limit 1";
			PreparedStatement prest = conn.prepareStatement(sql);
			prest.setInt(1, id);
			ResultSet rs = prest.executeQuery();
			while (rs.next()) {
				res = new PostVO();
				res.setId(rs.getInt("ID")+"");
				res.setName(rs.getString("post_title"));
				res.setContent( this.removeHtmlTag(rs.getString("post_content") ) );
//                res = rs.getString("post_content");
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//		if(res!=null && res.length()>0){
//			res = this.removeHtmlTag(res);
//		}
		return res;
	}
	
	/**
	 * 获取index writer
	 * @param indexPath
	 * @param create
	 * @return
	 * @throws IOException
	 */
	private IndexWriter getIndexWriter(String indexPath,boolean create) throws IOException {
		IndexWriter writer = null;
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			Directory dir = FSDirectory.open(new File(indexPath));
			
			Analyzer analyzer = new PaodingAnalyzer();//new StandardAnalyzer(Version.LUCENE_4_10_0);
			
			IndexWriterConfig iwc = new IndexWriterConfig(
					Version.LUCENE_4_10_0, analyzer);
//			boolean create = true;
			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			writer = new IndexWriter(dir, iwc);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}
	
	
	
	
	
	private Document createDoc(PostVO vo)  {  
        Document doc = new Document();  
        //就像有某个商品，查询结果列表要展示商品的名称，ID，和跳转链接地址，所以从数据库取出name,id,url字段  
          
        doc.add(new StringField("id", vo.getId(),  Store.YES));
        doc.add(new TextField("content", vo.getContent(), Store.YES));
        doc.add(new TextField("name", vo.getName(), Store.YES));
        
        return doc;  
    }
	
	private void addDoc(IndexWriter indexWriter, List<PostVO> resultList) throws IOException {  
        for (PostVO vo : resultList) {  
            Document doc = createDoc(vo);  
            indexWriter.addDocument(doc);
        }
    }
	
	/**
	 * 添加一条索引
	 * @param post_id
	 */
	public boolean addIndex(String path,int post_id){
		boolean res = false;
		try {
			Date start = new Date();
			IndexWriter iw = getIndexWriter( path ,false);
			PostVO p = null;
			p = getPostVO(post_id);
			if(p!=null){
				Document doc = createDoc(p); 
				iw.addDocument(doc);
				res = true;
			}
			iw.close();
			Date end = new Date();
			System.out.println("Generata one index:"+ (end.getTime() - start.getTime())
					+ " total milliseconds");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * 生成所有索引
	 */
	public boolean GenerateAllIndex(String path){
		boolean res = false;
		try {
			Date start = new Date();
			IndexWriter iw = getIndexWriter(path,true);
			List<PostVO> pl = null;
			pl = getAllContentFromDatabase();
			if(pl!=null){
				addDoc(iw,pl);
				res = true;
			}else{
				res = false;
			}
			iw.close();
			Date end = new Date();
			System.out.println("Generata One Index:"+ (end.getTime() - start.getTime()) + " total milliseconds");
		} catch (IOException e) {
			e.printStackTrace();
			res = false;
		}
		return res;
	}
	
	
	
	
	
	
	
	
	/**
	 * Test
	 * @param args
	 */
	public static void main(String[] args) {
		
		GenerateIndex gi = new GenerateIndex();
		gi.GenerateAllIndex(Constant.IDX_DIR);
		
		
//		List l = gi.getAllContentFromDatabase();
//		Printer.printList(l);
		
//		
		
//		PostVO p = gi.getPostVO(32);
//		 
//		List<String> lists = gi.getWords(p.getContent() , new PaodingAnalyzer() );  
//		for (String s : lists) {
//		    System.out.println(s);  
//		}
//		
		
//		
		
		//String str = gi.getContentFromDatabase(33);
//		HashMap hm;
//		try {
//			hm = gi.getAllContentFromDatabase();
//			TestPrint.printHashMap(hm);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("STR:"+str);
	}
}
