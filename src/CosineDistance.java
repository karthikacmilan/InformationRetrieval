package edu.asu.irs13;
import java.io.File;
import java.io.PrintWriter;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CosineDistance {
	
	HashMap<Integer, Double> Norm = new HashMap<Integer, Double>();
	HashMap<Integer, Double> naidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> Preidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> l2Tfidf = new HashMap<Integer, Double>();
	HashMap<Integer, Integer> CD = new HashMap<Integer, Integer>();
	HashMap<Integer, Double> Result1 = new HashMap<Integer, Double>();
	HashMap<Integer, Double> CD1 = new HashMap<Integer, Double>();
	HashMap<String, Double> vidf = new HashMap<String, Double>();
	
	String output1 = "";
	String output2 = "";
	
	
	public void norm() throws Exception
	{
		double start = System.currentTimeMillis();
		Double increments;
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));	
		TermEnum t = r.terms();
		
		
		while(t.next())
			{
			Term te = new Term("contents", t.term().text());
			TermDocs td =  r.termDocs(te);
			while(td.next())
			{
				increments=(double) 0;
				if (Norm.containsKey(td.doc()))
                {
					increments = Norm.get(td.doc());
                }
				increments += td.freq()*td.freq();
				
				 Norm.put(td.doc(), increments);
			}
		}
		for (int key : Norm.keySet()) 
		{
			Double sqinc = 0.0;
			sqinc = Math.sqrt(Norm.get(key));
			naidf.put(key, sqinc);
		}
	 double end = System.currentTimeMillis();
	 double value = end - start; 
	 System.out.println("Time taken for L2Norm TF : " + value);
	}
	public void task2() throws Exception
	{
		double start = System.currentTimeMillis(); 
		double idf = 0.0;
		double logidf = 0.0;
		double tfidf ;
		
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));	
		
		TermEnum t = r.terms();
		
		while(t.next())
			{
			Term te = new Term("contents", t.term().text());
			TermDocs td =  r.termDocs(te);
			while(td.next())
			{
				tfidf = (double) 0.0;
				if (Preidf.containsKey(td.doc()))
                {
					tfidf = Preidf.get(td.doc());
                }
			Double docount = (double)r.docFreq(t.term());
			Double maxc = (double)r.maxDoc();
			idf = Math.log((maxc)/(docount));
			vidf.put(t.term().text(),idf);
			logidf = td.freq()* idf;
			
			tfidf += logidf * logidf; 
			
			Preidf.put(td.doc(),tfidf);
		}				
		
		}
		for (int key : Preidf.keySet()) 
		{
			Double sqinc = 0.0;
			sqinc = Math.sqrt(Preidf.get(key));
			l2Tfidf.put(key, sqinc);			
		}
		double end = System.currentTimeMillis();
		double value = end - start; 
		System.out.println("Time taken for L2norm TFIDF : " + value);
		}
	
	public String CDtf(String str) throws Exception 
	{
		int count = 0;
		double start3 = System.currentTimeMillis();
		String[]  querryarray = str.split(" ");
		for (int i = 0; i < querryarray.length; i++)
		{
			querryarray[i] = querryarray[i].trim();
		}
		 Map<String, Integer> map = new HashMap<>();
		
		 for (String w : querryarray) 
		 {
			 Integer n = map.get(w);
		        n = (n == null) ? 1 : ++n;
		        map.put(w, n); 
		 }
		 
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		int tffre = 0;
		int h = 0;
		double modq = 0.0;
		
		for(String words : querryarray)
		{
			Term te = new Term("contents", words);
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
				if(CD.containsKey(td.doc()))						
				{
					tffre = CD.get(td.doc());
					tffre = tffre + td.freq();
					CD.put(td.doc(), tffre);
				}
				else{
					CD.put(td.doc(), td.freq());
				}
			}
		}
		
		
		for (String k : map.keySet())
		{
			int j = map.get(k);
			 h += j * j;
		}
		modq = Math.sqrt(h);
		
		for(int did : CD.keySet()){
			double v = CD.get(did) / (modq * naidf.get(did));
			Result1.put(did,v);
		}	
		double start1 =  System.nanoTime();  
		
		class MyComparator implements Comparator {

			Map map;

			public MyComparator(Map map) {
			    this.map = map;
			}

			public int compare(Object o1, Object o2) {

			    return ((Double) map.get(o2)).compareTo((Double) map.get(o1));
			}
			}
		MyComparator comp=new MyComparator(Result1);
		double end1 = System.nanoTime();  
		double time = (end1 - start1);
		
	    Map<Integer,Double> newMap = new TreeMap(comp);
	    newMap.putAll(Result1);
	    
	    //writing to file 
	    //PrintWriter writer = new PrintWriter("C:\\data\\name.txt", "UTF-8");
	    String lineseparator=System.getProperty("line.separator");
	    String k1 = ""; 
	    for(int k : newMap.keySet())
	    {
	    	if(count < 10){
	    	System.out.println(("Search Results :"+"["+k+"]" ));
	    	k1 = Integer.toString(k) ; 
	    	output1 += k1 + lineseparator;
	    	
	    	
	    	count++;
	    	}
	    }
	    
	    //writer.close();
	    double end3 = System.currentTimeMillis();
	    double value = end3 - start3; 
	    System.out.println("Time to sort results with tf :" +time+ " nano seconds ");
	    System.out.println("Tine taken for getting the results tf : " +value);
	    System.out.println("Number of Results :" +count);
	    System.out.println("Output1 is printing " +output1);
	    
	    return output1;
	}
	public String CDtfidf(String str) throws Exception 
	{
		
		String[]  querryarray = str.split(" ");
		
		
		
		for (int i = 0; i < querryarray.length; i++)
		{
			querryarray[i] = querryarray[i].trim();
		}
		 Map<String, Integer> map = new HashMap<>();
		
		 for (String w : querryarray) 
		 {
			 Integer n = map.get(w);
		        n = (n == null) ? 1 : ++n;
		        map.put(w, n); 
		 }
		 
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		double tffre = 0.0;
		int h = 0;
		double modq = 0.0;
		
		for(String words : querryarray)
		{
			Term te = new Term("contents", words);
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
				if(CD1.containsKey(td.doc()))						
				{
					tffre = CD1.get(td.doc());
					tffre = tffre + (double) td.freq()* vidf.get(words);
					CD1.put(td.doc(), tffre);
				}
				else{
					
					CD1.put(td.doc(), (double)td.freq()* vidf.get(words));
				}
			}
		}
		
		
		for (String k : map.keySet())
		{
			int j = map.get(k);
			 h += j * j;
		}
		modq = Math.sqrt(h);
		
		for(int did : CD1.keySet()){
			double v = CD1.get(did) / ((double)modq * l2Tfidf.get(did));
			Result1.put(did,v);
		}		
		double start2 = System.nanoTime();
		class MyComparator implements Comparator {

			Map map;

			public MyComparator(Map map) {
			    this.map = map;
			}

			public int compare(Object o1, Object o2) {

			    return ((Double) map.get(o2)).compareTo((Double) map.get(o1));

			}
			}
		MyComparator comp=new MyComparator(Result1);
		double end2 = System.nanoTime();
		double time = end2 - start2 ; 
		ArrayList<Integer> numbers = new ArrayList<Integer>();   
		Random randomGenerator = new Random();
		int k = 22;
		while (numbers.size() < 19) {

		    int random = randomGenerator .nextInt(20);
		    if (!numbers.contains(random)) {
		        numbers.add(random);
		    }
		}
	    Map<Integer,Double> newMap = new TreeMap(comp);
	    newMap.putAll(Result1);
	   
	    int count = 0 ; 
	    String output8 = "";
	    String url ="";
	    String text="";
	    String url1 = "";
	    String lineseparator=System.getProperty("line.separator");
	    for(int k1 : newMap.keySet())
	    {	
	    	if(count<10){
	    		
		    	
	    	Document d = r.document(k1);
	   		url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
	   		url1 = url.replace("%%", "/");
	   		String u = "C:/Users/KarthikMila/Downloads/result3/" + url;
	   		String i1 = new String(Files.readAllBytes(Paths.get(u)));
	   		org.jsoup.nodes.Document doc = Jsoup.parse(i1);
	   		String htmlString = doc.text();
	   		
	   		Pattern pattern = Pattern.compile(str+",(.*) " );
	   		
	   		Matcher matcher = pattern.matcher(htmlString);
	   		if (matcher.find())
	   		{
	   			text = matcher.group(1);
	   		}
	   		//System.out.println(numbers.size());
	   		//text = text.substring(1);
	   		if(count % 2 ==0)
	   		output8 += url1 + lineseparator  + " ... " + text1[numbers.get(count)] + str + " ... " + text1[numbers.get(count+1)] + lineseparator + lineseparator;
	   		else 
	   			output8 += url1 + lineseparator  + " ... " + text1[numbers.get(count)] +  text1[numbers.get(count+1)] + lineseparator + lineseparator;
	   		System.out.println(output8);
	    	count++;
	    	
	    	}
	    	
	    }
	    System.out.println("------------------------------------------------------");
	  
	    return output8;
	}
	public String CDtfrange(String str) throws Exception 
	{
		int count = 0;
		
		double start3 = System.currentTimeMillis();
		String[]  querryarray = str.split(" ");
		for (int i = 0; i < querryarray.length; i++)
		{
			querryarray[i] = querryarray[i].trim();
		}
		 Map<String, Integer> map = new HashMap<>();
		
		 for (String w : querryarray) 
		 {
			 Integer n = map.get(w);
		        n = (n == null) ? 1 : ++n;
		        map.put(w, n); 
		 }
		 
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		int tffre = 0;
		int h = 0;
		double modq = 0.0;
		int st = 0;
		for(String words : querryarray)
		{
			Term te = new Term("contents", words);
			TermDocs td = r.termDocs(te);
			while(td.next())
			{
				if(td.doc() < 25000){
				if(CD.containsKey(td.doc()))						
				{
					tffre = CD.get(td.doc());
					tffre = tffre + td.freq();
					CD.put(td.doc(), tffre);
				}
				else{
					CD.put(td.doc(), td.freq());
				}
				}
				
			}
		}
		
		
		for (String k : map.keySet())
		{
			int j = map.get(k);
			 h += j * j;
		}
		modq = Math.sqrt(h);
		
		for(int did : CD.keySet()){
			double v = CD.get(did) / (modq * naidf.get(did));
			Result1.put(did,v);
		}	
		double start1 =  System.nanoTime();  
		
		class MyComparator implements Comparator {

			Map map;

			public MyComparator(Map map) {
			    this.map = map;
			}

			public int compare(Object o1, Object o2) {

			    return ((Double) map.get(o2)).compareTo((Double) map.get(o1));
			}
			}
		MyComparator comp=new MyComparator(Result1);
		double end1 = System.nanoTime();  
		double time = (end1 - start1);
		
	    Map<Integer,Double> newMap = new TreeMap(comp);
	    newMap.putAll(Result1);
	    
	    //writing to file 
	    //PrintWriter writer = new PrintWriter("C:\\data\\name.txt", "UTF-8");
	    String output8 = "";
	    String url ="";
	    String url1 = "";
	    String lineseparator=System.getProperty("line.separator");
	  
	    		
	    
	    String text = "";
	    for(int k : newMap.keySet())
	    {	
	    	if(count<10){
	    		
		    	
	    		Document d = r.document(k);
	   		 url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
	   		 url1 = url.replace("%%", "/");
	   		org.jsoup.nodes.Document doc = Jsoup.parse(url1);
	   		String htmlString = doc.toString();
	   		//String htmlString = "my grade is good ashgshagshagsjbbs shajhsajhsa hsajhsjahs jhsjahsjhas";
   		Pattern pattern = Pattern.compile(str);
	   		Matcher matcher = pattern.matcher(htmlString);
	   		if (matcher.find())
	   		{
	   			text = matcher.group(1);
	   		}
	   		//text = text.substring(1, 15);
	   		//output8 += url1 + lineseparator+ text +"..." +lineseparator;
	   		//output8 += url1 + lineseparator + htmlString +"..." +lineseparator;
	   		output8 = htmlString;
	    	count++;
	    	//text ="";
	    	//htmlString = "";
	    	}
	    	
	    }
	    //writer.close();
	    double end3 = System.currentTimeMillis();
	    double value = end3 - start3; 
//	    System.out.println("Time to sort results with tf :" +time+ " nano seconds ");
//	    System.out.println("Tine taken for getting the results tf : " +value);
//	    System.out.println("Number of Results :" +count);
	    return output8; 
	}
	public static void main(String[] args) throws Exception
	{
		Project1 a = new Project1();
		 System.out.println("loading...");
	     a.task2();
  		 a.norm();
		 String str = "";
		 System.out.print("query> ");
		 Scanner sc = new Scanner(System.in);
		 str = sc.nextLine();
		 System.out.println("0. Exit");
	     System.out.println("1. Cosine Distance (TF)");
	     System.out.println("2. Cosine Distance (TF/IDF)");
	     System.out.println("3. Search querry from a range");
	     System.out.println("Enter your Choice");
	     //Scanner input2 = new Scanner(System.in);
	     String ch = sc.nextLine();
	     loop: while(ch!=null){
	     //     System.out.println("choice" + ch);
	     
	     switch(ch.charAt(0))
	     {
	     	case '0': break loop;
	     
	     	case '1': a.CDtf(str);break;
	     	
	     	case '2': a.CDtfidf(str); break;
	     	
	     	case '3': 
	     		
	     		a.CDtfrange(str);break;
	     	
	   		default: break;
		}
	     System.out.println("0. Exit");
	     System.out.println("1. Cosine Distance (TF)");
	     System.out.println("2. Cosine Distance (TF/IDF)");
	     System.out.println("Enter your Choice");
	     //Scanner input2 = new Scanner(System.in);
	     ch = sc.nextLine();
	     
	}}}


