
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
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
public class KMeansClustering 
{
	HashMap<Integer, Double> Norm = new HashMap<Integer, Double>();
	HashMap<Integer, Double> naidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> Preidf = new HashMap<Integer, Double>();
	HashMap<Integer, Double> l2Tfidf = new HashMap<Integer, Double>();
	HashMap<Integer, Integer> CD = new HashMap<Integer, Integer>();
	HashMap<Integer, Double> Result1 = new HashMap<Integer, Double>();
	HashMap<Integer, Double> CD1 = new HashMap<Integer, Double>();
	HashMap<String, Double> vidf = new HashMap<String, Double>();
	HashMap<Integer, HashMap<String, Double>> MainHash = new HashMap<Integer, HashMap<String, Double>>();
	HashMap<String, Double> supportHash = new HashMap<String, Double>();
	
	int count = 0 ; 
    
    int[] bs =  new int[50];
	
	
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
				//getting frequency value
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
			supportHash.put(t.term().text(), tfidf);
			MainHash.put(td.doc(), supportHash);
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
		//query 
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
	    
	    //
	    for(int k : newMap.keySet())
 	    {
    		 if(count < 50)
    		 {
 	    	//tf-idf results in bs array
 	    	bs[count]=k;
 	    	count++;
    		 }
    		 }
	}
	
	public void calc(String str) throws Exception 
	{
		//initialize values
		double start = System.nanoTime();
		int N = 50; 
		int k = 7 ; 
		boolean converged = false;
		int[] cluster = new int[N];
		double val = 0.0;
		int[] clusterCount = new int[k];
		double[][] sim = new double[k][N];
		double[] cenNorm = new double[k];
	    HashMap<Integer, HashMap<String, Double>> DocwordHashmap =  new HashMap<Integer, HashMap<String, Double>>();
	    HashMap<Integer, HashMap<String, Double>> CenwordHashmap =  new HashMap<Integer, HashMap<String, Double>>();
	    int[] cluster11 = new int[50];
		 int[] cluster12 = new int[50];
	    for(int i = 0 ; i < N ; i++)
	    {
	    	DocwordHashmap.put(bs[i], new HashMap<String, Double>()); //dynamically create hashmap based on N
	    }
	    
	    //Random rand = new Random(); 
	    
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		TermEnum term = r.terms();
		//double idfvalue = 0.0 ;
		while(term.next())
	 	{
	    	Term te = new Term("contents", term.term().text());
	 		TermDocs td =  r.termDocs(te);
	 		while(td.next())
			{
	 			if(DocwordHashmap.containsKey(td.doc()))
	 			{
	 				DocwordHashmap.get(td.doc()).put(term.term().text(), (double)td.freq());
	 				
	 			}
	 		
			}
	 	}
	//	for( int i = 0 ; i<N ; i++)
//          {
//                     Arrays.fill(cvalue, 0.0);
//                     for( int k = 0 ; k < 50 ; k ++)
//                     {   //System.out.println("test4");
//                         for(String words : clustercom.get(i).keySet())
//                         {   //System.out.println("test2");
//                             if(st32.get(bs[k]).containsKey(words))
//                             {   //System.out.println("test3");
//                                 cvalue[k] += st32.get(bs[k]).get(words) * clustercom.get(centroid[i]).get(words);
//                                 //System.out.println(cvalue[k]);
//                             }
//                         }
		
		
		
		//arraylist  to store random numbers 
		ArrayList<Integer> numbers = new ArrayList<Integer>();   
		Random randomGenerator = new Random(); //random function
		while (numbers.size() < k) {

		    int random = randomGenerator .nextInt(50);
		    if (!numbers.contains(random)) { //avoid duplicate values
		        numbers.add(random); // add values to arraylist
		    }
		}
		//
//	       {
//	       for(String k : st32.get(centroid[in]).keySet())
//	       {  
//	           cluster1.put(k, st32.get(centroid[in]).get(k));
//	       }
		//int nm[] = {20952,23565,22816}; //checking the answer 
		//System.out.println(numbers.get(2)); // checking
		for(int i = 0 ; i <k ; i++)
		{
			
	    	
		    //add the clustervalue and < word , tf value > hashmap in hashmap
			CenwordHashmap.put(i, DocwordHashmap.get(bs[numbers.get(i)]));
			//CenwordHashmap.put(i, DocwordHashmap.get(nm[i])); // checking by fixing the centroid manually
			//CenwordHashmap.put(i, DocwordHashmap.get(nm[i+1]));
			//CenwordHashmap.put(i, DocwordHashmap.get(nm[0]));
			//calculating norm L2
			
		    for(Map.Entry<String,Double> entry: CenwordHashmap.get(i).entrySet())
		    {
		    	val = entry.getValue();
		    	cenNorm[i] += Math.pow(val, 2); 
		    }
		    
		    cenNorm[i] = Math.sqrt(cenNorm[i]);
		}
		int count = 0 ; 
		
		//checking condition
		while(!converged)
		{
			System.out.println("Interation :" +count);
			count++;
			converged = true;
			
			for(int i = 0; i < k; i++)
			{
				//checking through thge centroid hashmap
				for(Map.Entry<String, Double> entry : CenwordHashmap.get(i).entrySet())
				{
					// calculaitng the tf-idf similaroty 
					for(int j = 0; j < N; j++)
					{
						
						//cluster1.put(k, st32.get(centroid[in]).get(k));
						if(DocwordHashmap.get(bs[j]).containsKey(entry.getKey()))
							sim[i][j] += entry.getValue() * DocwordHashmap.get(bs[j]).get(entry.getKey());
					}
				}
				
				for(int j = 0; j < N; j++)
				{
					//cluster1.put(k, st32.get(centroid[in]).get(k));
					sim[i][j] /= (cenNorm[i] * naidf.get(bs[j]));
				}
			}
			for(int a = 0 ; a < k ; a ++)
			{
				for ( int b  = 0 ; b < N ; b ++)
				{
					//System.out.println(sim[a][b]);
				}
			}
			int [] cluste = clusterCount.clone();
			//clearing the value of cluster Count
			for(int i = 0; i < k; i++)
			{
				clusterCount[i] = 0;
			}
			
			int size = cluste.length;
//			for (int key : Norm.keySet()) 
//			{
//				Double sqinc = 0.0;
//				sqinc = Math.sqrt(Norm.get(key));
//				naidf.put(key, sqinc);
//			}
			
			
			
			//assigning the document to the cluster with max value
			for(int i = 0 ; i < N ; i++)
			{
				double max = 0.0;
				int ind = 0;
				for(int j = 0 ; j < k ; j ++)
				{
					if(max < sim[j][i])
					{
						max = sim[j][i];
						ind = j;
					}
				}
				//checking converge condition
				//if previous and presnet are same = coverged. 
				if(cluster[i] != ind)
					converged = false;
				cluster[i] = ind;		
				clusterCount[ind]++;
			}
			
			for(int i = 0; i < k; i++)
			{
				
				CenwordHashmap.put(i, new HashMap<String, Double>()); //dynamically creating hashmaps based on the k value 
				cenNorm[i] = 0;
			}
			double calc = 0.0; 
			for(int  i = 0; i < N; i++)
			{
				for(Map.Entry<String,Double> entry: DocwordHashmap.get(bs[i]).entrySet())
				{
					//change the centroid hashmap with the doc and <word , tf - idf > values divided by the number of docs
					if(CenwordHashmap.get(cluster[i]).containsKey(entry.getKey()))
					{
						double temp = CenwordHashmap.get(cluster[i]).get(entry.getKey());
						temp += entry.getValue() / clusterCount[cluster[i]];
						CenwordHashmap.get(cluster[i]).put(entry.getKey(), temp);
						//System.out.println("1");
					}
					
					else
					{
						calc = entry.getValue() / clusterCount[cluster[i]];
						CenwordHashmap.get(cluster[i]).put(entry.getKey(), calc );
						//System.out.println("2");
					}
				}
			}
			double valu1 = 0.0;
			for(int i = 0; i < k; i++)
				//normalize 
			{
				for(Map.Entry<String,Double> entry: CenwordHashmap.get(i).entrySet())
				{
					valu1 = entry.getValue();
					cenNorm[i] += Math.pow(valu1, 2);
				}
				Double[] value = new Double[i];
				//value[i] = Math.sqrt(cenNorm[i]);
				cenNorm[i] = Math.sqrt(cenNorm[i]);;
			}
		}
		//System.out.println("test");		
		
		for(int i = 0; i < k; i++)
		{
			//printing the values 
			int count1 = 0;
			System.out.println();
			System.out.println("Cluster" + i);
			for(int j = 0; j < N; j++)
			{
				
				if(cluster[j] == i)
				{
					
					System.out.println(bs[j]);
//					Document d = r.document(bs[j]);
//			   		String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
//			   		String url1 = url.replace("%%", "/");
//			   		System.out.println(url1);
					count1++;
				}
				if(count1 == 3)
					break;
			}
			
		}
		//time calculation
		double end = System.nanoTime();
		double time = end - start ; 
		System.out.println("Time taken :" +time);
	}
	
			 
	 public static void main(String[] args) throws Exception
	    		{
	    			Part3 a = new Part3();
	    			Project1 b = new Project1();
	    			 System.out.println("loading...");
	    		     a.task2();
	    	  		 a.norm();
	    	  		 
	    			 String str = "";
	    			 System.out.print("query> ");
	    			 Scanner sc = new Scanner(System.in);
	    			 str = sc.nextLine();
	    			 str = str.toLowerCase();
	    			 System.out.println("0. Exit");
	    		     System.out.println("1. k - means");
	    		     System.out.println("Enter your Choice");
	    		     //Scanner input2 = new Scanner(System.in);
	    		     String ch = sc.nextLine();
	    		     loop: while(ch!=null){
	    		     //     System.out.println("choice" + ch);
	    		     
	    		     switch(ch.charAt(0))
	    		     {
	    		     	case '0': break loop;
	    		     
	    		     	//case '1': a.CDtf(str);break;
	    		     	
	    		     	case '1': a.CDtfidf(str);
	    		     	a.calc(str); break;
	    		     	
	    		     	
	    		     	default: break;}
	    		     	 System.out.println("0. Exit");
		    		     System.out.println("1. K - means ");
		    		     System.out.println("Enter your Choice");
		    		     ch = sc.nextLine();
		    		     
		    		     
	    		 	}}
}
