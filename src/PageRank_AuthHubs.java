package edu.asu.irs13;

import java.io.File;
import java.io.PrintWriter;
import java.lang.String;



import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class PageRank_AuthHubs 

{
	HashMap<Integer, Double> Norm = new HashMap<Integer, Double>();
	HashMap<Integer, Double> naidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> Preidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> l2Tfidf = new HashMap<Integer, Double>();
	HashMap<Integer, Integer> CD = new HashMap<Integer, Integer>();
	HashMap<Integer, Double> Result1 = new HashMap<Integer, Double>();
	HashMap<Integer, Double> CD1 = new HashMap<Integer, Double>();
	HashMap<String, Double> vidf = new HashMap<String, Double>();
	
	
	
	
	public void norm() throws Exception
	{
		
		Double increments;
		//index reader intialize
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
				//getting frequency valu e
				 Norm.put(td.doc(), increments);
			}
		}
		//term frequency
		for (int key : Norm.keySet()) 
		{
			Double sqinc = 0.0;
			sqinc = Math.sqrt(Norm.get(key));
			naidf.put(key, sqinc);
		}
	 
	
	}
	public void task2() throws Exception
	{
		
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
			//System.out.println(key + " " + sqinc );
		}
		
		
		}
	//sorting the hashmap with key and values 
	public static <K, V extends Comparable<? super V>> Map<K, V> entriesSortedByValues( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        @Override
	        public int compare( Map.Entry<K, V> o2, Map.Entry<K, V> o1 )
	        {
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );
	
	    Map<K, V> sortedEntries = new LinkedHashMap<>();
	    for (Map.Entry<K, V> entry : list)
	    {
	    	sortedEntries.put( entry.getKey(), entry.getValue() );
	    }
	    return sortedEntries;
	}
	
	
	public void CDtfidf(String str) throws Exception 
	{
		//link docs
		LinkAnalysis.numDocs = 25054; 
		// link object
		LinkAnalysis link = new LinkAnalysis();
		double start3 = System.currentTimeMillis();
		//storing the query string into array
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
		//considering each term from query array
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
		//tf/idf cosine value 
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
		
		//sorted values 
	    Map<Integer,Double> newMap = new TreeMap(comp);
	    newMap.putAll(Result1);
	    
	    
	  // PrintWriter writer = new PrintWriter("C:\\data\\bb.txt", "UTF-8");
	   for(int k : newMap.keySet())
	   {
		  // writer.println(k + " " + newMap.get(k));
	   }
	  // writer.close();
	   //baseset structure 
	   double st = System.nanoTime();
	    double st1 = System.nanoTime();
	    int count = 0 ; 
	    
	    int[] bs =  new int[10];
	    
	    	 for(int k : newMap.keySet())
	 	    {
	    		 if(count < 10){
	 	    	//System.out.println(("Search Results :"+"["+k+"]" ));
	 	    	bs[count]=k;
	 	    	count++;
	    		 }
	 	    	
	 	    }
	    	 
	    	 HashSet <Integer> bs1 = new HashSet<Integer>();
	    	 //adding content to baseset
	    	 for(int h2 = 0 ; h2 <15 ; h2 ++)
	    	 {
	    		 if(!bs1.contains(bs[h2]))
	    		 {
	    		 bs1.add(bs[h2]);
	    		 }
	    		 int linkj[]  = link.getLinks(bs[h2]); 
	    		 //links
	    		 for(int l=0 ;  l<linkj.length; l++)
	    		 {
	    			 if(!bs1.contains(linkj[l]))
		    		 {
	    			 bs1.add(linkj[l]);
		    		 }
	    		 }
	    		 int ction[]  = link.getCitations(bs[h2]); 
	    		 //citation
	    		 for(int l=0 ;  l<ction.length; l++)
	    		 {
	    			 if(!bs1.contains(ction[l]))
		    		 {
	    			 bs1.add(ction[l]);
		    		 }
	    		 }
	    	 }
	    	 int baseset = bs1.size();
	    	 double en1 = System.nanoTime();
	    	 double ti1 = en1-st1;
	    	 System.out.println("Time taken for baseset calculation :" +ti1);
	    	
	    	//System.out.println(baseset);
	    	Integer[] bsarray =  new Integer[baseset];
	    	int i = 0;
	    	for ( int value : bs1)
	    	{
	    		
	    		bsarray[i] = value; 
	    		i++;
	    		//System.out.println(value);
	    	}
	    	
	    	//initializing M values to 0
	    	 double[][] M = new double[baseset][baseset];
    	 for( i=0 ; i<baseset ; i++)
    	 {
	    		 for(int j = 0 ; j<baseset ; j ++)
	    		 {
	    			 M[i][j] = 0.0;
	    		 }
	    	 }
	    	 double[][] Mt = new double[baseset][baseset];
	    	 ArrayList<Integer> bs2 = new ArrayList<Integer>(Arrays.asList(bsarray));
	    	 double st2 = System.nanoTime();
	    	 
	    	 // computing adjacency matrix
	    	 for (int k = 0; k < bs2.size(); k++)
	    	 {
	    		 int docId = bs2.get(k);
	    		 
	    		 int[] l = link.getLinks(docId);
	    		 for(int nt : bs2)
	    		 {
	    		 for (int check1 : l)
	    		 {	
	    			 int index = bs2.indexOf(check1);
	    			 if(bs2.contains(check1))	
	    			 {
	    				 M[k][index] =  1.0;
	    			 }
	    			
	    		 }
	    		 
	    		 } l = null;
	    		
	    	 }
	    	 double en2 = System.nanoTime();
	    	 double ti2 = en2 - st2 ; 
	    	 System.out.println(" Time taken for adjacency matrix:" + ti2);
	    	 //checking the values of the matrix
	    	 for(i = 0 ;i <baseset ;i ++)
	    	 {
	    		 for(int j = 0; j <baseset ; j++)
	    		 {
	    			// System.out.print(M[i][j]+ " ");
	    		 }
	    		// System.out.println();
	    	 }
	    
	    	// computing transpose 
	    	 for (int i1 = 0; i1 < baseset; i1++) 
	    	 {
	             for (int j = 0; j < baseset; j++) 
	             {
	            	 
	                  Mt[j][i1] = M[i1][j] ;
	                 
	             }
	         }
	    	 
	    	 double []  authvector = new double[baseset]; //ai-1
	    	 double [] hubvector = new double [baseset];  //hi-1
	    	 double [] authi = new double[baseset];		//ai
	    	 double [] hubi = new double[baseset];		//hi
	    	 //initializing values for ai and hi
	    	 Arrays.fill(authi, 1.0);
	    	 Arrays.fill(hubi, 1.0);
	    	 double largesterror = 0.0; //error
	    	 double threshhold = 0.000000001; //threshold values
	    	
	    	
    		 double [] sort1 = new double[baseset];
    		 double [] sort2 = new double[baseset];
    		 double authvector1 = 0.0;
    		 double authvector11 = 0.0;
    		 
    		 double st3 = System.nanoTime();
    		 //convergance
	    	 do
	    	 {
	    	        //copy values
	    	        for( i =0;i < baseset ;i++)
	    	        { 
	    	        	//  ai-1 = ai 
	    	          // hi-1 = hi 

	    	            authvector[i] = authi[i];
	    	            hubvector[i] = hubi[i];
	    	            }
	    	        
	    		 for(int i1=0;i1<baseset;i1++)
	    		 {
	    			 	//authvector11 = 0.0;
	    		        for(int j=0;j<baseset;j++){
	    		        	
	    		        double l1 = hubvector[j];
	    		        double m1 = Mt[i1][j];
	    		         authvector11 += m1*l1; 	
	    		      //   System.out.println(authvector11);
	    		        }
	    		        authi[i1] = authvector11;
	    		        authvector11 = 0.0;
	    		      }
	    		 
	    		 
	    		 for(int i1=0;i1<baseset;i1++){
	    			// authvector1 = 0.0;
	    		        for(int j=0;j<baseset;j++){
	    		        	
	    		        double l1 = authi[j];
	    		        double m = M[i1][j];
	    		         authvector1 += m*l1;
	    		         //System.out.println(authvector1);
	    		        }
	    		        hubi[i1] = authvector1;
	    		        authvector1 = 0.0; 
	    		      }
	    		
	    		 double st6 = System.nanoTime();
	    		//normalize double sum = 0.0; 
	    		 double sum = 0.0;
	    		 //normalization 
	    		 for(int i1 = 0 ; i1<baseset ; i1++)
	    		 {
	    			 //sum of hubvalues 
	    			sum += hubi[i1]*hubi[i1];
	    			
	    		 }
	    		  
	    		 double sum2 = 0.0;
	    		 for(int i1 = 0 ; i1<baseset ; i1++)
	    		 {
	    			 //sum of auth values
	    		 sum2 += authi[i1]*authi[i1];
	    		 }
	    		 for(int i1 = 0 ; i1<baseset ; i1++)
	    		 {
	    			 //normalizing hub
	    			 hubi[i1] = hubi[i1]/Math.sqrt(sum);
	    			
	    			
	    		 }
	    		 for(int i1 = 0 ; i1<baseset ; i1++)
	    		 {
	    			 //normalizing auth values
	    		 authi[i1] = authi[i1]/Math.sqrt(sum2);
	    		 }
	    		 double en6 = System.nanoTime();
	    		 double ti6 = en6 - st6;
	    		System.out.println(" Time taken for Normalizing per iteration :" + ti6);
	    		 double st5 = System.nanoTime();
	    		 double a = 0.0;
	    		 double b = 0.0; 
	    		 
	    		 for(int i1=0 ; i1<baseset; i1++)
	    		 {
	    			 
	    			sort1[i1] = Math.abs(authi[i1] - authvector[i1]); 
	    			sort2[i1] = Math.abs(hubi[i1] - hubvector[i1]);
	    		 } 
	    		 //sort
	    					
	    		Arrays.sort(sort1);
	    		Arrays.sort(sort2);
	    		//largest error calculation
	    		a= sort1[baseset-1];
	    		b= sort2[baseset-1];
	    		// System.out.println(a + "  " + b);
	    
	    		 largesterror = Double.max(a,b);
	    		double en5 = System.nanoTime();
	    		double ti5 = en5 - st5;
	    		System.out.println(" Time taken for error calculation per iternation :" + ti5);
	    		
	    	 }while (largesterror > threshhold);
	    	 
	    	 double en3 = System.nanoTime();
	    	 double ti3 = en3 - st3 ; 
	    	 System.out.println("Time taken for convergance :"+ti3);
	    	 HashMap< Integer, Double> valueofauth = new HashMap<Integer , Double>();
	    	 HashMap< Integer, Double> valueofhub = new HashMap<Integer , Double>();
	    	 HashMap< Integer, Double> valueofauthfinal = new HashMap<Integer , Double>();
	    	 HashMap< Integer, Double> valueofhubfinal = new HashMap<Integer , Double>();
	    	 double st4 = System.nanoTime();
	    	 for( i = 0; i<baseset ; i++)
	    	 { 
	    		 //storing values in a hashmap
	    		 valueofhub.put(bsarray[i],hubi[i]);
	    		 valueofauth.put(bsarray[i],authi[i]);
	    	 }
	    	 
	    	 //fetching sorted results 
	    	 valueofhubfinal = (HashMap<Integer, Double>) entriesSortedByValues(valueofhub);
	    	 valueofauthfinal = (HashMap<Integer, Double>) entriesSortedByValues(valueofauth);
	    	int x = 0;
	    	 PrintWriter writer1 = new PrintWriter("C:\\data\\hub.txt", "UTF-8");
	    	 for(int k2 : valueofhubfinal.keySet())
	    	 {
	    		 //printing results
	    		 if(x<10){
	    		 System.out.println(k2 + " " + valueofhubfinal.get(k2));
	    		// writer1.println(k2);
	    		 x++;
	    	 }}//writer.close();
	    	 System.out.println();
	    	 int y = 0;
	    	// PrintWriter writer = new PrintWriter("C:\\data\\auth.txt", "UTF-8");
	    	 for(int k2 : valueofauthfinal.keySet())
	    	 {
	    		 //printing results
	    		 if(y<10){
	    		System.out.println(k2 + " " + valueofauthfinal.get(k2));
	    		// writer1.println(k2);
	    		 y++;
	    	 }
	    		 
	    	 }writer1.close();
	    	double en4= System.nanoTime();
	    	double ti4 = en4 - st4;
	    	System.out.println(" Time taken for results in sorted order :" + ti4);
	    	// writer.close();
	    	double en = System.nanoTime();
	    	double ti = en - st;
	    	System.out.println(" Overall Time taken : " + ti);
	    	}
	public void pageRanking(String str, double w) throws Exception 
	{
		CDtfidf(str);
		LinkAnalysis.numDocs = 25054; 
		LinkAnalysis link = new LinkAnalysis();
		//initializing 
		double st = System.nanoTime();
		double c = 0.8;
		int docCount = 25054;
        int Count = 0;
        double [] pageRank = new double[docCount];
        double[] pageRanki = new double[docCount];
        double[] sort = new double[docCount];
        double sinkValue = 1.0/25054.0;
        double  threshhold = 1.0/1000000000.0;
        double error = 1.0;
        
        //fill rank vector with 1/N value 
        Arrays.fill(pageRanki, sinkValue);
        
        int [] column = new int[docCount];
      
        double[] M = new double[docCount];
        
        
        Map<Integer, Integer> nZCount = new HashMap<Integer, Integer>();  
        //find non-zero values for every row
        int count = 0;
        //finding sink node count
        for(int i = 0 ; i < docCount ; i++)
        {
        	if(link.getLinks(i).length==0)
        	{
        		count++;
        	}
        	
        }
        int[] sinkfinder =  new int[count];
        int[] citations = null ;
        int j = 0;
        //storing sink node in array
        for(int i = 0 ; i < docCount ; i++)
        {
        	
        	if(link.getLinks(i).length==0)
        	{
        	sinkfinder[j] = i;
        	j++;
        	}
        }
       
       // System.out.println(j);
        // considering each colum and links of each 
        for(int i=0; i< docCount; i++)
        {	
        	
            Count = 0;
            column = link.getLinks(i);
            Count = column.length;
            nZCount.put(i, Count);
            column = null;
        }
        	int Count1 = 0 ;
        int y = 1;	
        do
        {
        	 for(int i=0; i<docCount; i++)
             {
        		 //step RANKi-1 = RANKi  
                 pageRank[i] = pageRanki[i];
             }
        	
        	 for(int i = 0 ; i < docCount ; i++)
             {
        		 //filling M with common value of 1.0-c * 1/n 
        		 Arrays.fill( M , ((1.0-c)*sinkValue));
        		 //adding c*1/n to sink nodes
        		 for(int i2 : sinkfinder)
              	{
              		M[i2] += c*(sinkValue);
              	}
        		 //filling values for non-sink nods
             	citations = link.getCitations(i);
             	for(int i1 : citations)
             	{
             		 Count1 = link.getLinks(i1).length;
             		 M[i1] += c*((double)(1.0/Count1));
             	}
             	
        		double value = 0.0; 
        		//computing the pagerank value
        		for(int k = 0; k<docCount; k++)
        		{
        			value += M[k]*pageRank[k];
        		}
        		pageRanki[i] = value;
        		value = 0.0;
            }
        		
            double sum  = 0.0;
            //L1 norm calculation
            for( int k = 0; k<docCount; k++)
    		{
            	sum += pageRanki[k] ; 
    		}
            //System.out.println(sum);
            for( int k = 0; k<docCount; k++)
    		{
            	pageRanki[k] = pageRanki[k]/sum;
    		}
            
            for( int k = 0; k<docCount; k++)
            {
            	sort[k] = Math.abs(pageRanki[k] - pageRank[k]);
            }
           
            Arrays.sort(sort);
            
            
            error = sort[docCount-1];
          System.out.println("Iteration : " + y + " Largest Error : "  + error);
          y++; // iterations counter
        }while(error>threshhold);
        
        
        double[] normpageRank = pageRank.clone();
       
         
        Arrays.sort(normpageRank);
        double max = normpageRank[docCount-1];
        System.out.println();
        double min = normpageRank[0];
        //min max normalization
        for(int i =0 ; i<docCount ; i ++)
        {
        	pageRank[i] = (pageRank[i] - min)/(max-min);
        			
        }
        PrintWriter writer = new PrintWriter("C:\\data\\name.txt", "UTF-8");
        for(int i =0 ; i<docCount ; i++)
        {
        	//writer.println(i + "  " + pageRank[i]);
        }
       // writer.close();
        
        //Arrays.sort(pageRank);
        
        // used to get the URL for a document 
        
//        IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
//        Document d = r.document(9048);
//		String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
//		System.out.println(url.replace("%%", "/"));
		
       
        double result = 0.0;
        //Result1
        HashMap<Integer, Double> pageRankResult = new HashMap<Integer, Double>();
        //combining pagerank with similarities 
        //formula - w * (PageRank) + (1-w)* (Vector Space Similarity) 
        // 0 < w < 1
        for(int k = 0 ; k<docCount ; k++)
        {
        	//System.out.println(k + "   " + Result1.get(k));
        	if(Result1.get(k)!= null )
        	{
        	result = w*pageRank[k]+(1.0-w)*Result1.get(k);
        	pageRankResult.put(k,result);
        	//System.out.println(Result1.get(k));
        	result = 0.0;
        	}
        	
        	//System.out.println(pageRank[k]);
        	
        }
        //sorting the result 
        class MyComparator implements Comparator {

			Map map;

			public MyComparator(Map map) {
			    this.map = map;
			}

			public int compare(Object o1, Object o2) {

			    return ((Double) map.get(o2)).compareTo((Double) map.get(o1));

			}
			}
		MyComparator comp=new MyComparator(pageRankResult);
		
		
	    Map<Integer,Double> newMap = new TreeMap(comp);
	    newMap.putAll(pageRankResult);
	    int i1 =1;
	   //printing thye value 
	    System.out.println( " --- NEW PAGERANK --- OLD PAGE RANK");
        for(int ma : newMap.keySet())
        {
        	if(i1<11){
        	System.out.println( ma + " " + newMap.get(ma) + " " + pageRank[ma]);
        	 IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
        	 Document d = r.document(ma);
        	 String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
        	 System.out.println(url.replace("%%", "/"));
        	// System.out.println( " " + newMap.get(ma) + " " + pageRank[ma]);
        	writer.println(ma);
        	}
        	i1++;
        
        }
        writer.close();
        //get java runtime
        Runtime runtime = Runtime.getRuntime();
        //garbage collector
        runtime.gc();
        //calculating the used memory by total memory and free memory 
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Total Memory :" + runtime.totalMemory()/1024);
        System.out.println("Free Memory :" + runtime.freeMemory()/1024);
        System.out.println("Used memory is Kilo-bytes: " + memory/1024);
        double en = System.nanoTime();
        double ti = en-st;
        System.out.println("Total Time taken :" + ti );
	}

	public static void main(String[] args) throws Exception
	{
		Project2 a = new Project2();
		
		 System.out.println("loading...");
	     a.task2();
  		 a.norm();
  		 
		 String str = "";
		 System.out.print("query> ");
		 Scanner sc = new Scanner(System.in);
		 str = sc.nextLine();
		 str = str.toLowerCase();
		 System.out.println("0. Exit");
	     System.out.println("1. Authority and Hub");
	     System.out.println("2. PageRank");
	    
	     System.out.println("Enter your Choice");
	     //Scanner input2 = new Scanner(System.in);
	     String ch = sc.nextLine();
	     loop: while(ch!=null){
	     //     System.out.println("choice" + ch);
	     
	     switch(ch.charAt(0))
	     {
	     	case '0': break loop;
	     
	     	//case '1': a.CDtf(str);break;
	     	
	     	case '1': a.CDtfidf(str); break;
	     	case '2': System.out.println("In Progress...");
	     	System.out.println("Enter Value of W");
	     	Scanner in = new Scanner(System.in);
	     	double num = in.nextDouble();
	     	a.pageRanking(str,num);
	     	break;
	   		default: break;
		}
	     System.out.println("0. Exit");
	     System.out.println("1. Authority and hubs");
	     System.out.println("2. PageRank");
	     System.out.println("Enter your Choice");
	     //Scanner input2 = new Scanner(System.in);
	     ch = sc.nextLine();
	     
	}}
}
