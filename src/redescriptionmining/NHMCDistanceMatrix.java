/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.javatuples.Triplet;
import static redescriptionmining.SettingsReader.ENCODING;
/**
 *
 * @author matej
 */
public class NHMCDistanceMatrix {
    int distanceMatrix[][];
    int numElem=0;
    HashMap<String,Double> m_distancesS = new HashMap<>();// = new TObjectDoubleHashMap();
    HashMap<String,Double> m_distancesN = new HashMap<>();
    HashMap<Integer,HashSet<Integer>> connectivity = new HashMap<>();
    HashMap<Integer,HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>>> connectivityMultiplex=new HashMap<Integer,HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>>>();
    HashMap<Integer,Double> globalNodeInfo = new HashMap<>();//store normalized node global network measures 
    int networkType=0, numLayers=1, inputType=0;
    //element1, layID1, layID2, elem2, weight
    HashMap<Integer,HashMap<Integer, HashMap<Integer, HashMap<Integer,Double>>>> optim = new HashMap<>();
            //load a graf into this optimised structure -> for testing purpose only
    
     NHMCDistanceMatrix(){
         
     }
     
    NHMCDistanceMatrix(int numElements, ApplicationSettings appset){
        distanceMatrix=new int[numElements][numElements];
        numElem=numElements;
        
        for(int i=0;i<numElements;i++)
            for(int j=0;j<numElements;j++)
                distanceMatrix[i][j]=appset.maxDistance;
        
        for(int i=0;i<numElements;i++)
            distanceMatrix[i][i]=0;
        
    }
    
    void loadDistanceMatrix(ApplicationSettings appset){
        //PrintWriter pw = new PrintWriter(new FileWriter("distancesDistance.txt"));
            
            int test=1;
            HashSet<Integer> layCount = new HashSet<>();
            
		m_distancesS.clear(); m_distancesN.clear();
                double epsilon = 10e-9;
		double maxdist=0; double minDistLine=Double.POSITIVE_INFINITY; double maxOnMinDistLine=0; double iEqual=Double.POSITIVE_INFINITY;
		String filename =  appset.outFolderPath+"\\distances.csv"; //input.getAbsolutePath();
                
                System.out.println("Distance file path: ");
                System.out.println(filename);
                
                try{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		double bandwidth = appset.Bandwith; double w;
		if (bandwidth!=100.0){	
                    String s;
			while ((s= br.readLine()) != null){
				//String s = br.readLine();
                                //System.out.println("distances file: "+" "+s);
				StringTokenizer st = new StringTokenizer(s,",");
				Long ii=Long.parseLong(st.nextToken());
				Long jj=Long.parseLong(st.nextToken());
				double d = Double.parseDouble(st.nextToken());
				if (iEqual!=ii) {m_distancesN.put((ii+"#"+ii), 0.0); iEqual=ii; // pw.println(ii+" "+ii+" "+0.0);
				}
				if (ii>jj) {m_distancesN.put((jj+"#"+ii), d); //pw.println(jj+" "+ii+" "+d);
				}
				else {m_distancesN.put((ii+"#"+jj), d); //pw.println(ii+" "+jj+" "+d);
				}			
			}

			for (double d:m_distancesN.values()){
			if (d>maxdist) maxdist=d;
			if (d!=0 && d<minDistLine) minDistLine=d;}
			if(minDistLine>maxOnMinDistLine) maxOnMinDistLine=minDistLine;
			double b=bandwidth*maxdist; //toCorrect!
			if(maxOnMinDistLine!=Double.POSITIVE_INFINITY && b<maxOnMinDistLine) b=maxOnMinDistLine;
			int spatialMatrix=0; //schema.getSettings().getSpatialMatrix();	
                        
                        
                        if(appset.spatialMatrix.get(0).toLowerCase().equals("euclidian"))
                            spatialMatrix=1;
                        else if(appset.spatialMatrix.get(0).toLowerCase().equals("modified"))
                            spatialMatrix=2;
                        else if(appset.spatialMatrix.get(0).toLowerCase().equals("gausian"))
                            spatialMatrix=3;
                        
                       // System.out.println("spatialMatrix: "+spatialMatrix);
			for (Map.Entry<String,Double> entry:m_distancesN.entrySet()){
				String i=entry.getKey(); double d=entry.getValue();

				if (d>=b) w=0; 
				else {if (d==0) w=1; 
				else{ 
					switch (spatialMatrix) {
				    	case 0:  if(d<b) w=0; else w=1;   break;  //binary 
				        case 1:  w=1-d/b; break;  //euclidian
				        case 2:  w=Math.pow(((1-(d*d)/(b*b))*(1-(d*d)/(b*b))),2); break; //modified
				        case 3:  w=Math.exp(-(d*d)/(b*b)); break;  //gausian
				        default: w=0; break;
				    	}				    				    
					}
                               // System.out.println("Reading distance: "+i+" "+w);
				m_distancesS.put((i),w); //write to hasp map non-zero only, not in file
				//System.out.println((i)+" "+w);
				}
			}
		}//add aditional else for new kind of distance file
		else{
                    boolean allEqual = true;
                    HashSet<Double> vals = new HashSet<>();
                    HashSet<Integer> el = new HashSet<>();
                    //first read the dataset and memorize maximal value for each pair of layers.
                    //second traverse the dataset again and normalize

                    String s;
                    
                    double maxValue=0.0;
                    HashMap<Integer,HashMap<Integer,Double>> layerMaxValues = new HashMap<Integer,HashMap<Integer,Double>>();
                    //first line -> 0-distance, 1-similarity

                    int lineCount=0, simType = 0;
                    
                    while((s=br.readLine())!=null){
                         StringTokenizer st = new StringTokenizer(s,","); 
                         
                         if(lineCount==0){
                             
                             if(st.countTokens()==1){
                                 simType = Integer.parseInt(st.nextToken().trim());
                                    lineCount=1;
                                  continue;
                             }
                             else if(st.countTokens() == 2){
                                 simType = Integer.parseInt(st.nextToken().trim());
                                 appset.directed = Integer.parseInt(st.nextToken().trim());
                                 lineCount=1;
                                 continue;
                             }
                             lineCount=1;
                         }
                         
                                if(st.countTokens()<4){
				String ii=st.nextToken().toLowerCase();
				String jj=st.nextToken().toLowerCase();
                                double d = Double.parseDouble(st.nextToken());
                                
                                if(d>maxValue)
                                    maxValue=d;
                                
                                }
                                else{
                                    String ii=st.nextToken().toLowerCase();
                                    String lay1=st.nextToken().toLowerCase();
                                    String jj=st.nextToken().toLowerCase();
                                    String lay2=st.nextToken().toLowerCase();
                                    String value=st.nextToken().toLowerCase();
                                     inputType=1;
                                    int i = Integer.parseInt(ii.trim());
                                    int j = Integer.parseInt(jj.trim());
                                    int lay1i = Integer.parseInt(lay1.trim());
                                    int lay2i = Integer.parseInt(lay2.trim());
                                    double valueD = Double.parseDouble(value);
                                    
                                    if(allEqual == true){
                                        vals.add(valueD);
                                        if(vals.size()>1)
                                            allEqual = false;
                                    }
                                    
                                    if(!layerMaxValues.containsKey(lay1i)){
                                        HashMap<Integer,Double> tmp = new HashMap<Integer,Double>();
                                        tmp.put(lay2i, valueD);
                                        layerMaxValues.put(lay1i, tmp);
                                    }
                                    else{
                                        HashMap<Integer,Double> tmp = layerMaxValues.get(lay1i);
                                        if(!tmp.containsKey(lay2i)){
                                            tmp.put(lay2i, valueD);
                                        }
                                        else{
                                            double v = tmp.get(lay2i);
                                            if(valueD>v)
                                                tmp.put(lay2i, valueD);
                                        }
                                    }   
                                }    
                    }
                    
                    br.close();

                     br = new BufferedReader(new FileReader(filename));
                    lineCount = 0;
			while ((s= br.readLine()) != null){

				StringTokenizer st = new StringTokenizer(s,","); 
                                
                                if(lineCount==0){
                             
                             if(st.countTokens()==1){
                                    lineCount=1;
                                  continue;
                             }
                             else if(st.countTokens() == 2){
                                 simType = Integer.parseInt(st.nextToken().trim());
                                 appset.directed = Integer.parseInt(st.nextToken().trim());
                                 lineCount=1;
                                 continue;
                             }
                             lineCount=1;
                         }
                         
                                
                                if(st.countTokens()<4){
				String ii=st.nextToken().toLowerCase();
				String jj=st.nextToken().toLowerCase();
                                
                                el.add(Integer.parseInt(ii)); el.add(Integer.parseInt(jj));
                                
                                HashSet<Integer> tmp=null;
                                if(!connectivity.containsKey(Integer.parseInt(ii))){
                                    tmp=new HashSet<Integer>();
                                    tmp.add(Integer.parseInt(jj));
                                    connectivity.put(Integer.parseInt(ii), tmp);
                                }
                                else{
                                    int ind=Integer.parseInt(ii);
                                    tmp=connectivity.get(ind);
                                    tmp.add(Integer.parseInt(jj));
                                    connectivity.put(ind, tmp);
                                }
                                
				double d = Double.parseDouble(st.nextToken());
                                
                               // System.out.println("distance: "+d);
                                
                                if(simType==0)
                                    d=d/maxValue*100;
                                else if(simType == 1)
                                    d=100-d/maxValue*100;
                             //   System.out.println("transformed: "+d);
                                
				int spatialMatrix=0;
                        
                        
                                 if(appset.spatialMatrix.get(0).toLowerCase().equals("euclidian"))
                                         spatialMatrix=1;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("modified"))
                                         spatialMatrix=2;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("gausian"))
                                         spatialMatrix=3;
                                
                                if (d>=100) w=epsilon; 
                                    else {if (d==0) w=1; 
                                
                                else{
				switch (spatialMatrix) {
				    case 0:  if (d==0) w=1; else w=0;  break;  //binary 
				    case 1:  if (d==0) w=1; else w=1-d/100; break;  //euclidian
                                    case 2:  if (d==0) w=1; else w=Math.pow(((1-(d*d)/(100*100))*(1-(d*d)/(100*100))),2); break;
                                    case 3:  if (d==0) w=1; else w=Math.exp(-(d*d)/(100*100)); break;
				    default: w=0; break;
				 }
                                }
                                }
                               // System.out.println("weight: "+w);
				//if (ii>jj) m_distancesS.put((jj+"#"+ii), w); else m_distancesS.put((ii+"#"+jj), w); 
				m_distancesS.put((ii+"#"+jj), w); 
				//m_distancesS.put((jj+"#"+ii), w); 
                               // System.out.println((ii+"#"+jj)+" "+w);
                                //check if graph is directed
                                
                               /* Iterator<Integer> it = connectivity.keySet().iterator();
                                
                                while(it.hasNext()){
                                    int el  = it.next();
                                    for(int i:connectivity.get(el)){
                                        if(!connectivity.containsKey(i)){
                                            appset.directed = 1;
                                            break;
                                        }
                                        else if(!connectivity.get(i).contains(el)){
                                            appset.directed = 1;
                                            break;
                                        }
                                    }
                                    if(appset.directed == 1)
                                        break;
                                }*/
                                
                                }
                                else{
                                  //  System.out.println("Loading type1 network: ");

                                    String ii=st.nextToken().toLowerCase();
                                    String lay1=st.nextToken().toLowerCase();
                                    String jj=st.nextToken().toLowerCase();
                                    String lay2=st.nextToken().toLowerCase();
                                    String value=st.nextToken().toLowerCase();
                                    
                                      el.add(Integer.parseInt(ii)); el.add(Integer.parseInt(jj));
                                    
                                 inputType=1;
                                    int l1=Integer.parseInt(lay1);
                                    int l2=Integer.parseInt(lay2);
                                    
                                     layCount.add(l1); layCount.add(l2);
                                    
                                   /* if(l1>numLayers)
                                        numLayers=l1;
                                    
                                    if(l2>numLayers)
                                        numLayers=l2;*/
                                    
                                    if(l1!=l2)
                                        networkType=1;
                                    
                                HashSet<Integer> tmp=null;
                                if(!connectivityMultiplex.containsKey(Integer.parseInt(ii))){
                                    tmp=new HashSet<Integer>();
                                    tmp.add(Integer.parseInt(jj));
                                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> con=new HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>>();
                                    ArrayList<Triplet<Integer,Integer,Double>> ta=new ArrayList<Triplet<Integer,Integer,Double>>();
      
                                    double d=Double.parseDouble(value);
                                    
                                    //for opt, all layer L1
                                    HashMap<Integer,HashMap<Integer, HashMap<Integer,Double>>> conopt = new HashMap<>();
                                    //all layer L2
                                    HashMap<Integer,HashMap<Integer,Double>> alllayer2 = new HashMap<>();
                                    //all element el2
                                    HashMap<Integer,Double> el2 = new HashMap<>();
                                    
                                    
                                    HashMap<Integer,Double> l2Max = layerMaxValues.get(l1);
                                    
                                      if(simType==0)
                                              d=d/l2Max.get(l2)*100;
                                       else if(simType == 1)
                                                d=100-d/l2Max.get(l2)*100;
                               //   System.out.println("l2: "+l2+" "+"l2Max: "+l2Max.get(l2));  
                                if(allEqual == true){
                                    d = 0.0000001;
                                }
                                      
                                    int spatialMatrix=0;
                       
                                 if(appset.spatialMatrix.get(0).toLowerCase().equals("euclidian"))
                                         spatialMatrix=1;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("modified"))
                                         spatialMatrix=2;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("gausian"))
                                         spatialMatrix=3;
                                 
                                if (d>=100) w=0; 
				else {if (d==0) w=1; 
                                
                                else{
				switch (spatialMatrix) {
				    case 0:  if (d==0) w=1; else w=0;  break;  //binary 
				    case 1:  if (d==0) w=1; else w=1-d/100; break;  //euclidian
                                    case 2:  if (d==0) w=1; else w=Math.pow(((1-(d*d)/(100*100))*(1-(d*d)/(100*100))),2); break;
                                    case 3:  if (d==0) w=1; else w=Math.exp(-(d*d)/(100*100)); break;
				    default: w=0; break;
				 }
                                }
                                }
                                    Triplet<Integer,Integer,Double> tt= Triplet.with(Integer.parseInt(jj), Integer.parseInt(lay2), w);
                                    ta.add(tt);
                                    con.put(Integer.parseInt(lay1), ta);
                                    connectivityMultiplex.put(Integer.parseInt(ii), con);
                                    //connectivity.put(Integer.parseInt(ii), tmp);
                                    
                                    //for opt
                                    el2.put(Integer.parseInt(jj), w);
                                    alllayer2.put(Integer.parseInt(lay2), el2);
                                    conopt.put(Integer.parseInt(lay1), alllayer2);
                                    optim.put(Integer.parseInt(ii), conopt);
                                    
                                }
                                else{
                                    int ind=Integer.parseInt(ii);
                                 //   tmp=connectivity.get(ind);
                                   // tmp.add(Integer.parseInt(jj));
                                    
                                     double d=Double.parseDouble(value);
                                    
                                     int spatialMatrix=0;
                        
                        
                                 if(appset.spatialMatrix.get(0).toLowerCase().equals("euclidian"))
                                         spatialMatrix=1;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("modified"))
                                         spatialMatrix=2;
                                 else if(appset.spatialMatrix.get(0).toLowerCase().equals("gausian"))
                                         spatialMatrix=3;
                                 
                                // System.out.println("Value: "+d);
                                  HashMap<Integer,Double> l2Max = layerMaxValues.get(l1);

                                   if(simType==0)
                                              d=d/l2Max.get(l2)*100;
                                       else if(simType == 1)
                                                d=100-d/l2Max.get(l2)*100;
                                   // System.out.println("l2: "+l2+" "+"l2Max: "+l2Max.get(l2));
                                if(allEqual == true){
                                    d = 0.0000001;
                                }
                                 
                                if (d>=100) w=0; 
				else {if (d==0) w=1; 
                                
                                else{
				switch (spatialMatrix) {
				    case 0:  if (d==0) w=1; else w=0;  break;  //binary 
				    case 1:  if (d==0) w=1; else w=1-d/100; break;  //euclidian
                                    case 2:  if (d==0) w=1; else w=Math.pow(((1-(d*d)/(100*100))*(1-(d*d)/(100*100))),2); break;
                                    case 3:  if (d==0) w=1; else w=Math.exp(-(d*d)/(100*100)); break;
				    default: w=0; break;
				 }
                                }
                                }
                                    
                               // System.out.println("weight: "+w);
                                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> con=connectivityMultiplex.get(ind);
                                    if(con.containsKey(Integer.parseInt(lay1))){
                                        ArrayList<Triplet<Integer,Integer,Double>> ta=con.get(Integer.parseInt(lay1));
                                        Triplet<Integer,Integer,Double> tt= Triplet.with(Integer.parseInt(jj), Integer.parseInt(lay2), w);
                                        ta.add(tt);
                                    }
                                    else{
                                        con.put(Integer.parseInt(lay1), new ArrayList<Triplet<Integer,Integer,Double>>());
                                        Triplet<Integer,Integer,Double> tt= Triplet.with(Integer.parseInt(jj), Integer.parseInt(lay2), w);
                                        con.get(Integer.parseInt(lay1)).add(tt);
                                    }
                                    //connectivity.put(ind, tmp);
                                
                                //for opt
                                HashMap<Integer,HashMap<Integer,HashMap<Integer,Double>>> conopt = optim.get(ind);
                                if(conopt.containsKey(Integer.parseInt(lay1))){
                                    HashMap<Integer,HashMap<Integer,Double>> l2opt = conopt.get(Integer.parseInt(lay1));
                                    
                                    if(l2opt.containsKey(Integer.parseInt(lay2))){
                                        HashMap<Integer,Double> el2opt = l2opt.get(Integer.parseInt(lay2));
                                        el2opt.put(Integer.parseInt(jj), w);
                                        
                                    }
                                    else{
                                        l2opt.put(Integer.parseInt(lay2), new HashMap<>());
                                    HashMap<Integer,Double> el2opt = l2opt.get(Integer.parseInt(lay2));
                                    el2opt.put(Integer.parseInt(jj), w);
                                    }
                                    
                                }
                                else{
                                    conopt.put(Integer.parseInt(lay1), new HashMap<>());
                                    HashMap<Integer,HashMap<Integer,Double>> l2opt = conopt.get(Integer.parseInt(lay1));
                                    l2opt.put(Integer.parseInt(lay2), new HashMap<>());
                                    HashMap<Integer,Double> el2opt = l2opt.get(Integer.parseInt(lay2));
                                    el2opt.put(Integer.parseInt(jj), w);
                                    
                                }
                                
                                }
                                
                               /* Iterator<Integer> it = connectivityMultiplex.keySet().iterator();
                                while(it.hasNext()){
                                    int el = it.next();
                                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> t = connectivityMultiplex.get(el);
                                    
                                    Iterator<Integer> itl = t.keySet().iterator();
                                    
                                    while(itl.hasNext()){
                                        int l = itl.next();
                                        ArrayList<Triplet<Integer,Integer,Double>> a = t.get(l);
                                        for(int z=0;z<a.size();z++){
                                            int el2 = a.get(z).getValue0();
                                            if(!connectivityMultiplex.containsKey(el2)){
                                                appset.directed = 1;
                                                break;
                                            }
                                            else if(!connectivityMultiplex.get(el2).containsKey(l)){
                                                appset.directed = 1;
                                                break;
                                            }
                                            else{
                                                ArrayList<Triplet<Integer,Integer,Double>> a1 = connectivityMultiplex.get(el2).get(l);
                                                for(int f = 0; f<a1.size(); f++ )
                                                    if(a1.get(f).getValue0() == el){
                                                        appset.directed = 1;
                                                        break;
                                                    }
                                            }
                                            if(appset.directed == 1)
                                                break;
                                        }
                                        if(appset.directed == 1)
                                            break;
                                }
                                    if(appset.directed == 1)
                                            break;
                                }  */    
			}
                                numElem = el.size();
		}
		br.close();	
	}
                numLayers = layCount.size();
      }
                catch(IOException e){
                    e.printStackTrace();
                }
  }
    
    void computeGlobalTriangles(int graphSequence, ApplicationSettings appset){
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0;
                    Iterator<Integer> it1 = this.connectivity.keySet().iterator();

                    while(it1.hasNext()){
                        int elem1 = it1.next();
                        
                        if(elem == elem1)
                            continue;
                        
                         Iterator<Integer> it2 = this.connectivity.keySet().iterator();
                         
                         while(it2.hasNext()){
                             int elem2 = it2.next();
                         
                             if(elem2 == elem || elem2 == elem1)
                                 continue;

                            sum1+=Math.pow(m_distancesS.get(elem+"#"+elem1)*m_distancesS.get(elem+"#"+elem2)*m_distancesS.get(elem1+"#"+elem2),1.0/3);
                        
                    }
                    //usedNodes.add(elem);
                }
                  sum+=sum1/(((this.connectivity.keySet().size()-1)*(this.connectivity.keySet().size()-2))); 
                  this.globalNodeInfo.put(elem, sum);
                  sum = 0.0;
                }
                
            }
            else if(inputType==1){//multiplex
                System.out.println("Computing global triangles...");
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
                double count = 0;
                while(it.hasNext()){
                    int elem = it.next();
                    
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    //System.out.println("Num edg: "+edg.size());
                                    for(int i1=0;i1<edg.size();i1++){
                                       // System.out.println("numE: "+edg.size()+" , "+" curr: "+i1);
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                      
                                        int elem2=t.getValue0();

                                        int layId1 = t.getValue1();
                                        if(appset.graphSequence == 1 && layId1!=layId)
                                            continue;
                                         if(appset.graphSequence == 0 && layId>layId1 && appset.directed == 0)
                                                 continue;
                                         
                                         if(!connectivityMultiplex.containsKey(elem2))
                                             continue;
                                       HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn1 = connectivityMultiplex.get(elem2);
                                  ArrayList<Triplet<Integer,Integer,Double>> edg1=conn1.get(layId1);
                                  
                                  HashMap<Integer,HashMap<Integer,Double>> layInfo = this.optim.get(elem2).get(layId1);
                                  
                                     // System.out.println("edge1 size: "+edg1.size());
                                          int start = 0;
                                   if(appset.directed == 0){       
                                     for(int j1=start;j1<edg.size();j1++){
                                          Triplet<Integer,Integer,Double> t1=edg.get(j1);
                                           /*if(layId == 1 && layId1 == 1)
                                          System.out.println("Elem3: "+t1);*/
                                            int elem3 =  t1.getValue0();
                                            
                                             if(!connectivityMultiplex.containsKey(elem3))
                                                 continue;

                                            int found = 0, cTmp=0;
                                            
                                            if(appset.graphSequence == 1 && !layInfo.containsKey(layId))
                                                        continue;
                                            
                                            
                                            int l3 = t1.getValue1();
                                            
                                            if(!layInfo.containsKey(l3))
                                                continue;
                                            
                                            if((l3 == layId && elem3 == elem) || (l3 == layId1 && elem3 == elem2))
                                                 continue; 
                                            
                                           if(layInfo.get(l3).containsKey(elem3))
                                               found = 1;
                                            
                                            

                                            // for(int c=0;c<edg1.size();c++){
                                                 
                                              //      if(appset.graphSequence == 1 && edg1.get(c).getValue1()!=layId)
                                            //            continue;
                                             /*if(layId == 1 && layId1 == 1)
                                                 System.out.println(edg1.get(c));*/
                                           //  if((edg1.get(c).getValue1() == layId && elem3 == elem) || (edg1.get(c).getValue1() == layId1 && elem3 == elem2))
                                           //      continue;
                                             
                                            // if(edg1.get(c).getValue0() == elem3 && t1.getValue1().compareTo(edg1.get(c).getValue1())==0){
                                                // System.out.println("("+elem+"-"+layId+" "+elem2+"-"+layId1+" "+elem3+"-"+edg1.get(c).getValue1()+")");
                                           //      cTmp = c; found = 1; break;
                                         //    }
                                       //  }
                                             
                                            // System.out.println("GS: "+graphSequence);
                                             
                                              if(found == 0)
                                             continue;
                                         if(graphSequence == 0)
                                             //sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3);
                                            //sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2)/2.0)*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*2);//dodaj weightove
                                            sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*layInfo.get(l3).get(elem3),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2)/2.0)*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*2);//dodaj weightove

                                         else if(graphSequence == 1)
                                            // sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3);
                                           // sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers));//dodaj weightove
                                            sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*layInfo.get(l3).get(elem3),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2)/2.0)*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*2);//dodaj weightove


                                     }
                                   }
                                   else{ //if directed, add code here
                                        for(int j1=start;j1<edg1.size();j1++){
                                          Triplet<Integer,Integer,Double> t1=edg1.get(j1);
                                           /*if(layId == 1 && layId1 == 1)
                                          System.out.println("Elem3: "+t1);*/
                                            int elem3 =  t1.getValue0();
                                            
                                             if(!connectivityMultiplex.containsKey(elem3))
                                                 continue;
                                             
                                            int lay13 = t1.getValue1();
                                            int found = 0, cTmp=0;
                                            
                                            if(((lay13 == layId) && elem3 == elem) || ((lay13 == layId1) && elem3 == elem2))
                                                continue;
                                            
                                             HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn2 = connectivityMultiplex.get(elem3);
                                             ArrayList<Triplet<Integer,Integer,Double>> edg2=conn2.get(lay13);

                                             for(int c=0;c<edg2.size();c++){
                                                 
                                                    if(appset.graphSequence == 1 && edg2.get(c).getValue1()!=layId)
                                                        continue;
                                             /*if(layId == 1 && layId1 == 1)
                                                 System.out.println(edg1.get(c));*/
                                             /*if((edg2.get(c).getValue1() == layId && elem3 == elem) || (edg2.get(c).getValue1() == layId1 && elem3 == elem2))
                                                 continue;*/
                                             
                                             if(edg2.get(c).getValue0() == elem && edg2.get(c).getValue1()==layId){
                                                // System.out.println("("+elem+"-"+layId+" "+elem2+"-"+layId1+" "+elem3+"-"+edg1.get(c).getValue1()+")");
                                                 cTmp = c; found = 1; break;
                                             }
                                         }
                                             
                                            // System.out.println("GS: "+graphSequence);
                                             
                                              if(found == 0)
                                             continue;
                                         if(graphSequence == 0)
                                             //sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3);
                                            sum1+=Math.pow(edg.get(i1).getValue2()*edg1.get(j1).getValue2()*edg2.get(cTmp).getValue2(),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2)/2.0)*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*4);//dodaj weightove
                                         else if(graphSequence == 1)
                                            // sum1+=Math.pow(edg.get(i1).getValue2()*edg.get(j1).getValue2()*edg1.get(cTmp).getValue2(),1.0/3);
                                            sum1+=Math.pow(edg.get(i1).getValue2()*edg1.get(j1).getValue2()*edg2.get(cTmp).getValue2(),1.0/3)/((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1))*numLayers));//dodaj weightove

                                     }
                                   }
                                    }  
                                }
                                //System.out.println("Denom: "+((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2)/2.0)*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*2));
                               // System.out.println("Denom1: "+((((this.connectivityMultiplex.keySet().size()-2)*(this.connectivityMultiplex.keySet().size()-1)/2)*numLayers+/*(numLayers*(numLayers-1))*(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size())*/+(numLayers*(numLayers-1)*(numLayers-2))*((this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()-2)/2)+/*(this.connectivityMultiplex.keySet().size()-1)^2*((numLayers-1)*(numLayers-2)/2)+*/(numLayers*(numLayers-1))*(numLayers-2)/6+(this.connectivityMultiplex.keySet().size()-1)*(this.connectivityMultiplex.keySet().size()*(numLayers*(numLayers-1)))/*(numLayers*(numLayers-1)/2*(numLayers-2)*(this.connectivityMultiplex.keySet().size()-1))*/)*2));

                                System.out.println("Node over!");
                                System.out.println(100*(++count)/this.connectivityMultiplex.keySet().size()+"%");
                          this.globalNodeInfo.put(elem, sum1);
                         // System.out.println("sum1: "+sum1);
                }
            }
            
            
             Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }    
            
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                /*Iterator<Integer>*/ it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
    
    
     void computeGlobalDegree(int graphSequence, ApplicationSettings appset){
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0;
                    Iterator<Integer> it1 = this.connectivity.get(elem).iterator();

                    while(it1.hasNext()){
                        int elem1 = it1.next();
                        
                       sum1+= m_distancesS.get(elem+"#"+elem1);
                }
                  sum+=sum1/(this.connectivity.keySet().size()); 
                  this.globalNodeInfo.put(elem, sum);
                  sum = 0.0;
                }
                
            }
            else if(inputType==1){//multiplex
              ArrayList<Double> outScores = new ArrayList<>(Collections.nCopies(numElem, 0.0));
               // System.out.println("Computing global degree...");
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
               // System.out.println("Directed: "+appset.directed);
                while(it.hasNext()){
                    int elem = it.next();
                    //System.out.println("Elem: "+elem);
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                       // System.out.print(" Element: "+edg.get(i1).getValue0()+" Value: "+edg.get(i1).getValue2());
                                        
                                       if(graphSequence == 0){
                                            sum1+=edg.get(i1).getValue2()/((/*this.connectivityMultiplex.keySet().size()*/numElem-1)*this.numLayers*this.numLayers+(this.numLayers*(this.numLayers-1))/2);//dodaj weightove
                                            outScores.set(edg.get(i1).getValue0(), outScores.get(edg.get(i1).getValue0())+edg.get(i1).getValue2()/((/*this.connectivityMultiplex.keySet().size()*/numElem-1)*this.numLayers*this.numLayers+(this.numLayers*(this.numLayers-1))/2));
                                       }
                                         else if(graphSequence == 1){
                                            sum1+=edg.get(i1).getValue2()/(/*this.connectivityMultiplex.keySet().size()*/numElem*this.numLayers);//dodaj weightove
                                             outScores.set(edg.get(i1).getValue0(), outScores.get(edg.get(i1).getValue0())+edg.get(i1).getValue2()/(/*this.connectivityMultiplex.keySet().size()*/numElem*this.numLayers));
                                         }

                                     }
                                    //System.out.println();
                                        
                                    }  
                                
                          this.globalNodeInfo.put(elem, sum1);
                         // System.out.println("sum1: "+sum1);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int elem = it.next();
                    this.globalNodeInfo.put(elem, (this.globalNodeInfo.get(elem)+outScores.get(elem))/2.0);
                }
                
                
                 it = this.globalNodeInfo.keySet().iterator();
                
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }    
                
            }
            
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
    
      void computeGlobalNeighbourhoodDegree(int graphSequence, ApplicationSettings appset){
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                    System.out.println("Elem: "+elem);
                   double sum1 = 0.0;
                    Iterator<Integer> it1 = this.connectivity.get(elem).iterator();

                    while(it1.hasNext()){
                        int elem1 = it1.next();
                        
                        Iterator<Integer> it2 = this.connectivity.get(elem1).iterator();
                        
                        double sum2 = 0.0;
                        
                        while(it2.hasNext()){
                            int elem2 = it2.next();
                            
                            sum2+= m_distancesS.get(elem1+"#"+elem2)/(this.connectivity.keySet().size());
                            
                        }
                        
                        sum1+=m_distancesS.get(elem+"#"+elem1)*sum2/this.connectivity.keySet().size();     
                }
                  sum=sum1; 
                  this.globalNodeInfo.put(elem, sum);
                }
                
            }
            else if(inputType==1){//multiplex
                //System.out.println("Computing global degree...");
                
                ArrayList<Double> degOut = new ArrayList<>(Collections.nCopies(numElem, 0.0));
                ArrayList<Double> degIn = new ArrayList<>(Collections.nCopies(numElem, 0.0));
                
                
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
                
                   while(it.hasNext()){
                    int elem = it.next();
                   // System.out.println("Element: "+elem);
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                     // System.out.print(" Element: "+edg.get(i1).getValue0()+" Value: "+edg.get(i1).getValue2());
                                        int elem2=t.getValue0();//get the degree of this neighbour
                                        double sum2 = 0.0;
                                        
                                        if(graphSequence == 0){
                                            sum1+=edg.get(i1).getValue2()/((numElem-1)*this.numLayers*this.numLayers+(this.numLayers*(this.numLayers-1))/2);//dodaj weightove
                                            degIn.set(edg.get(i1).getValue0(), degIn.get(edg.get(i1).getValue0())+edg.get(i1).getValue2()/((numElem-1)*this.numLayers*this.numLayers+(this.numLayers*(this.numLayers-1))/2));
                                        }
                                         else if(graphSequence == 1){
                                            sum1+=edg.get(i1).getValue2()/(numElem*this.numLayers);//dodaj weightove   
                                            degIn.set(edg.get(i1).getValue0(), degIn.get(edg.get(i1).getValue0())+edg.get(i1).getValue2()/(numElem*this.numLayers));
                                         }
                }
                                    // System.out.println();
            }
                              //  System.out.println("Sum1: "+sum1);
                                degOut.set(elem, sum1);
                             //this.globalNodeInfo.put(elem, sum1);
                }
                   
                   //System.out.println("DegIn");
                  // for(int i=0;i<degIn.size();i++)
                  //     System.out.print(" "+degIn.get(i));
                  // System.out.println();
                   
                   ArrayList<Double> avnDGIn = new ArrayList<>(Collections.nCopies(numElem, 0.0));
                   
                   it = this.connectivityMultiplex.keySet().iterator();
                
                   while(it.hasNext()){
                    int elem = it.next();
                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                     // System.out.print(" Element: "+edg.get(i1).getValue0()+" Value: "+edg.get(i1).getValue2());
                                       sum1+=edg.get(i1).getValue2()*degIn.get(edg.get(i1).getValue0())/degOut.get(elem);
                                      // System.out.println(" s1: "+sum1);
                                       avnDGIn.set(edg.get(i1).getValue0(), avnDGIn.get(edg.get(i1).getValue0())+edg.get(i1).getValue2()*degOut.get(elem)/degIn.get(edg.get(i1).getValue0()));
                                    }
                                }
                    this.globalNodeInfo.put(elem, sum1);
                    
                   }
                   
                     it = this.connectivityMultiplex.keySet().iterator();
                    while(it.hasNext()){
                            int elem = it.next();
                            /*System.out.println("Elem: "+elem);
                            System.out.println("scoreOut: "+this.globalNodeInfo.get(elem));
                            System.out.println("scoreIn: "+avnDGIn.get(elem));*/
                            this.globalNodeInfo.put(elem, (this.globalNodeInfo.get(elem)+avnDGIn.get(elem))/(2.0*numElem));
                    }
                   
                /* it = this.connectivityMultiplex.keySet().iterator();
                while(it.hasNext()){
                    int elem = it.next();
                    System.out.println("Element: "+elem);
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                      System.out.print(" Element: "+edg.get(i1).getValue0()+" Value: "+edg.get(i1).getValue2());
                                        int elem2=t.getValue0();//get the degree of this neighbour
                                        double sum2 = 0.0;
                                        
                                         HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn1 = connectivityMultiplex.get(elem2);
                   
                                Iterator<Integer> itN1=conn1.keySet().iterator();
                                
                                while(itN1.hasNext()){
                                    
                                    int layId1=itN1.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg1=conn1.get(layId1);
                                    
                                    for(int i11=0;i11<edg1.size();i11++){
                                        Triplet<Integer,Integer,Double> t1=edg1.get(i11);
                                      
                                        int elem3=t1.getValue0();                             
                                        
                                       if(graphSequence == 0)
                                            sum2+=edg1.get(i11).getValue2()/((this.connectivityMultiplex.keySet().size()-1)*this.numLayers*this.numLayers+(this.numLayers*(this.numLayers-1))/2);//dodaj weightove
                                         else if(graphSequence == 1)
                                            sum2+=edg1.get(i11).getValue2()/(this.connectivityMultiplex.keySet().size()*this.numLayers);//dodaj weightove

                                      }
                                    } //degree of a neighbour elem2 
                                
                                
                              sum1+=edg.get(i1).getValue2()*sum2/this.connectivityMultiplex.keySet().size();
                         System.out.print(" AvDeg: "+sum2+ " ");
                         // System.out.println("sum1: "+sum1);
                }
                                     System.out.println();
            }
                                System.out.println("Sum1: "+sum1);
                             this.globalNodeInfo.put(elem, sum1/degOut.get(elem));
                }*/
            }
            
             Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }    
            
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }          
    }
         
       void computeGlobalZScore(int graphSequence, ApplicationSettings appset){
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            int numEl = 0;
            
            if(inputType == 0)
                numEl = this.connectivity.keySet().size();
            else if(inputType == 1)
                numEl = this.connectivityMultiplex.keySet().size();
            
            double degs[] = new double[numEl];
            int idAr[] = new int[numEl];
            int in = 0;
            
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0;
                    Iterator<Integer> it1 = this.connectivity.get(elem).iterator();

                    while(it1.hasNext()){
                        int elem1 = it1.next();
                        
                        Iterator<Integer> it2 = this.connectivity.get(elem1).iterator();
                        
                        double sum2 = 0.0;
                        
                        while(it2.hasNext()){
                            int elem2 = it2.next();
                            
                            sum2+= m_distancesS.get(elem1+"#"+elem2);
                            
                        }
                        
                        degs[in++]=sum2;     
                }
                 // this.globalNodeInfo.put(elem, degs[in-1]);
                  idAr[in-1] = elem;
                } 
            }
            else if(inputType==1){//multiplex
                System.out.println("Computing global degree...");
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                    
                     HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> conn = connectivityMultiplex.get(elem);
                   
                                Iterator<Integer> itN=conn.keySet().iterator();
                                double sum1 = 0.0;
                                while(itN.hasNext()){
                                    int layId=itN.next();
                                    ArrayList<Triplet<Integer,Integer,Double>> edg=conn.get(layId);
                                    
                                    for(int i1=0;i1<edg.size();i1++){
                                        Triplet<Integer,Integer,Double> t=edg.get(i1);
                                      
                                        int elem2=t.getValue0();//get the degree of this neighbour
                                       sum1+=t.getValue2();                                     
                }
                                   
            }
                                degs[in++]=sum1;
                                idAr[in-1] = elem;
                            // this.globalNodeInfo.put(elem, sum1);
                }
            }
            
            
             double avgD = 0.0;
                
                for(int i=0;i<in;i++)
                    avgD+=degs[i];
                avgD/=in;
                
                double std = 0.0;
                StandardDeviation s = new StandardDeviation(false);//populacijski std jer racunamo za sve cvorove u mrezi (za uzorak maknuti false)
                std=s.evaluate(degs);
                double indicator = 0.0;
                 for(int i=0;i<in;i++){
                   degs[i] = ((degs[i]-avgD)/std);
                   if(degs[i]>=2.5)
                       indicator = degs[i];
                   /*else if(degs[i]>=0)
                       indicator = degs[i]/2.5;*/
                   else indicator = 0;
                    this.globalNodeInfo.put(idAr[i], indicator);
                }
                 
                  Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }    
            
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
       
       
       void computeGlobalCentrality(int graphSequence, ApplicationSettings appset){
           System.out.println("Computing centrality");
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            int numEl = 0;
            
            Graph<Vertex,String> g = new SparseMultigraph<Vertex,String>();
            
            Dijkstra d = new Dijkstra(g);

                 g=d.generateGraph(this);
                
                
            if(inputType==0)
                numEl = this.numElem;//this.connectivity.keySet().size();
            else if(inputType == 1)
                numEl = this.numElem;//this.connectivityMultiplex.keySet().size();
            
            
            System.out.println("Map opt: "+this.optim.keySet().size());
            
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0;
                    Iterator<Integer> it1 = this.connectivity.get(elem).iterator();

                 
                     d.computeAllShortestPaths(elem, this);//make a function CPAP with two layers as input

                    
                    Collection<Vertex> vertices = g.getVertices();
		Vertex v;
		int i = 1;
 
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem)
                            continue;
                        sum1+=(1.0-v.sourceDistance);
                }
                    
                    this.globalNodeInfo.put(elem, (g.getVertexCount()-1)/sum1);             
                } 
            }
            else if(inputType==1){//multiplex
                System.out.println("Computing global centrality mult...");
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                    double sum1 = 0.0;
                    
                 if(appset.pathStrict == false){
                    d.computeAllShortestPaths(elem, this);//make new function for CASP with two parameters for two selected layers
                 
                    Collection<Vertex> vertices = g.getVertices();
                    Vertex v;
 
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem)
                            continue;
                        if(v.sourceDistance!=Double.POSITIVE_INFINITY)
                            sum1+=v.sourceDistance;
                        else sum1+=numEl;
                }
                
                 if(sum1!=0.0)
                   this.globalNodeInfo.put(elem, ((double)(/*g.getVertexCount()*/numEl-1.0))/sum1); 
                else if(sum1 == 0 && vertices.size()!=0)//there exist a very short path
                     this.globalNodeInfo.put(elem, ((double)(/*g.getVertexCount()*/numEl-1.0))/0.000000001);//add very small constant weight -> very large score
                else if(sum1 == 0 && vertices.size() == 0)//no path
                     this.globalNodeInfo.put(elem, 0.0);// zero score since no path
                   
                   for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY; 
                         v1.setPrev(null);
                   }
                
                 }
                 else{
                        int NL = this.numLayers;
                        int disconected = 1, allLayers = 1;
                        
                        for(int i=0;i<NL;i++){
                            for(int j=0;j<NL;j++){
                                if(appset.graphSequence == 1 && i!=j)
                                    continue;
                                        
                                 d.computeAllShortestPaths(elem, this,i,j);//make new function for CASP with two parameters for two selected layers
                 //System.out.println("L:  ("+i+","+j+")");
                    Collection<Vertex> vertices = g.getVertices();
                    if(vertices.size()!=0)
                        disconected = 0;
                    else allLayers = 0;
                    Vertex v;
 
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem)
                            continue;
                        if(v.sourceDistance!=Double.POSITIVE_INFINITY)
                            sum1+=v.sourceDistance;
                        else sum1+=numEl;
                       // System.out.println("S1: "+sum1);
                        System.out.println(elem+" "+v.getId()+" "+v.sourceDistance);
                    
                }
                
                for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY; 
                         v1.setPrev(null);
                }
                            }
                         }
                        
                      if(appset.graphSequence == 1)
                          sum1/=NL;
                      else{
                             sum1/=(NL*NL);
                      }
                     
                     //Collection<Vertex> vertices = g.getVertices();//nije skroz dobro
                     
                      if(sum1!=0.0)
                   this.globalNodeInfo.put(elem, ((double)(/*g.getVertexCount()*/numEl-1.0))/sum1); 
                else if(sum1 == 0 && disconected==0 && allLayers == 1)//there exist a very short path
                     this.globalNodeInfo.put(elem, ((double)(/*g.getVertexCount()*/numEl-1.0))/0.000000001);//add very small constant weight -> very large score
                else if(sum1 == 0 && disconected == 1)//no path
                     this.globalNodeInfo.put(elem, 0.0);// zero score since no path
                 }                             
                } 
                
                 it = this.globalNodeInfo.keySet().iterator();
                
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }               
            }
            

            
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
    
  //add function to compute betweeness centrality 
       
       void computeBetweenessCentrality(int graphSequence, ApplicationSettings appset){//change
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            int numEl = 0;
            
            Graph<Vertex,String> g = new SparseMultigraph<Vertex,String>();
            
            Dijkstra d = new Dijkstra(g);

                 g=d.generateGraph(this);
                // System.out.println("Input graph: ");
                // System.out.println(g.toString());
                
            if(inputType==0)
                numEl = this.numElem;//this.connectivity.keySet().size();
            else if(inputType == 1)
                numEl = this.numElem;//this.connectivityMultiplex.keySet().size();
                      
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0;
                   
                    Iterator<Integer> it1 = this.connectivity.keySet().iterator();

                    while(it1.hasNext()){
                    
                     int elem1 = it1.next();
                        
                    Iterator<Integer> it2 = this.connectivity.keySet().iterator();
                 
 
                     d.computeAllShortestPaths(elem1, this);//make a function CPAP with two layers as input
                                        
                    Collection<Vertex> vertices = g.getVertices();
		Vertex v=null;
		int i = 1;
 
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem1 || v.getId() == elem)
                            continue;
                        
                         Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
                         sum1 += allShortestPaths.size();
                        
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                         
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                             for(int zz=0;zz<s.size();zz++)
                                 if(s.get(zz).getId() == elem){
                                     sum2++;
                                     break;
                                 }
                         }
                }
                if(sum1!=0.0)
                sum3+=sum2/sum1;
                sum1 = 0.0; sum2 = 0.0;
               }
                    if(appset.directed == 0)
                            sum3 = sum3/((numEl-1)*(numEl-2));
                    else sum3 = sum3/(2*(numEl-1)*(numEl-2));
                     this.globalNodeInfo.put(elem, sum3);
              }
           }
            else if(inputType==1){//multiplex - modify this part
                System.out.println("Computing global BC...");
                 Iterator<Integer> it = this.connectivityMultiplex.keySet().iterator();
                double proc = 0.0;
                while(it.hasNext()){//zamijeniti petlje
                    int elem = it.next();
                    Vertex elemV = null;
                    
                    for(Vertex v:g.getVertices()){
                        if(v.getId() == elem){
                            elemV = v;
                            break;
                        }
                    }
                    double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0;
                    
                    Iterator<Integer> it1 = this.connectivityMultiplex.keySet().iterator();
                    
                    while(it1.hasNext()){
                    
                        int elem1 = it1.next();
                        
                 if(appset.pathStrict == false){
                //      d = new Dijkstra(g);

                     if(elem1 == elem)
                         continue;
                // g=d.generateGraph(this);
                    d.computeAllShortestPaths(elem1, this);//make new function for CASP with two parameters for two selected layers
                 System.out.println("Shortest paths computed!");
                    Collection<Vertex> vertices = g.getVertices();
                    Vertex v;
 
                int cc = 0;    
                    
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem || v.getId() == elem1)
                            continue;
                       // System.out.println("Distance to " + v.getId() + ": "
			//		+ v.sourceDistance);
                        System.out.println("Source: "+elem1);
                        System.out.println("Target: "+v);
                       Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
                         sum1 += allShortestPaths.size();
                        System.out.println("SP size: "+allShortestPaths.size());
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                       //  if(elem1 == 4 && v.getId() == 2)
                       //      System.out.println("Paths for 4: ");
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                             for(int zz=0;zz<s.size();zz++){
                                 if(elem1 == 4 && v.getId() == 2){
                                 System.out.print(s.get(zz).getId()+" ");
                                 }
                                 if(s.get(zz).getId() == elem){
                                     sum2++;
                                     break;
                                 }
                             }
                             if(elem1 == 4 && v.getId() == 2)
                             System.out.println();
                         }
                  // if(elem1 == 4 && v.getId() == 2)        
               //  System.out.println(elem+" "+elem1+" "+v.getId()+" "+sum2+" "+sum1);
                  if(sum1!=0.0)
                     sum3+=sum2/sum1;
                 sum1 = 0.0; sum2 = 0.0;
                
                 System.out.println("Progress: "+(cc++)+" "+vertices.size());
                 
                }
                         
                 System.out.println("Counting complete!");
                 
                // System.out.println("sum3: "+elem+" "+sum3);

                   for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY;
                        v1.setPrev(null);
                        
                   }
                
                 }
                 else{//modify
                        int NL = this.numLayers;
                        int disconected = 1, allLayers = 1;
                       
                        for(int k=0;k<NL;k++){
                        for(int i=0;i<NL;i++){
                            for(int j=0;j<NL;j++){
                                if(appset.graphSequence == 1 && (i!=j || i!=k || j!=k))
                                    continue;
                                else if(appset.graphSequence == 0 && (i!=k && j!=k))
                                    continue;
                                       if(elem == elem1)
                                           continue;
                                 d.computeAllShortestPaths(elem1, this,i,j);//make new function for CASP with two parameters for two selected layers
                 //System.out.println("L:  ("+i+","+j+")");
                    Collection<Vertex> vertices = g.getVertices();
                    if(vertices.size()!=0)
                        disconected = 0;
                    else allLayers = 0;
                    Vertex v;

		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem || v.getId() == elem1)
                            continue;
                        
                        //elem->v
                         Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(elemV, v, this, i, k, j);//should be corrected
                        // System.out.println("int: "+elemV+" tar:"+v.getId()+" st:"+elem1);
                        // System.out.println("Layers: "+(i+","+k+","+j));
                         sum1 += allShortestPaths.size();
                       
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                         
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                            // for(int zz=0;zz<s.size();zz++)
                              //   System.out.print(s.get(zz).getId()+" ");
                           //  System.out.println();
                             for(int zz=0;zz<s.size();zz++)
                                 if(s.get(zz).getId() == elem){
                                     sum2++;
                                     break;
                                 }
                         }
                        
                        //sum1+=v.sourceDistance;
                      // System.out.println("S1: "+sum1);
                      // System.out.println("S2: "+sum2);
                          if(sum1!=0.0)
                     sum3+=sum2/sum1;//needs to be changed
               //  System.out.println(elemV+" "+elem+" "+elem1+" "+sum2+" "+sum1);
                 sum1 = 0.0; sum2 = 0.0;
                }
                
                
                   
                for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY; 
                         v1.setPrev(null);
                }
                            }
                         }
                       }
 
                 }                             
                }
               if(appset.pathStrict == true) {    
                if(appset.graphSequence == 1){
                       if(appset.directed == 0)
                          sum3 = sum3/((numEl-1)*(numEl-2)*this.numLayers);
                       else  sum3 = sum3/(2*(numEl-1)*(numEl-2)*this.numLayers);
                }
                else{
                    if(appset.directed == 0)
                     sum3 = sum3/((numEl-1)*(numEl-2)*this.numLayers*this.numLayers);
                    else  sum3 = sum3/(2*(numEl-1)*(numEl-2)*this.numLayers*this.numLayers);
                }
                 this.globalNodeInfo.put(elem, sum3);//only a part of the sum
               }
               else{
                   if(appset.directed == 0)
                     sum3 = sum3/((numEl-1)*(numEl-2));
                   else  sum3 = sum3/(2*(numEl-1)*(numEl-2));
                 this.globalNodeInfo.put(elem, sum3);//only a part of the sum  
                }
                 proc+=1.0;
                // if(proc%100 == 0)
                     System.out.println(((proc/numEl)*100)+"%");
               }
             
                it = this.globalNodeInfo.keySet().iterator();
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }  
                
            }
 
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
       
       
          void computeBetweenessCentralityOpt(int graphSequence, ApplicationSettings appset){//change
          double density = 0.0, sum=0.0;
            HashSet<Integer> usedNodes = new HashSet<>();
            int numEl = 0;
            
            Graph<Vertex,String> g = new SparseMultigraph<Vertex,String>();
            
            Dijkstra d = new Dijkstra(g);

                 g=d.generateGraph(this);
                // System.out.println("Input graph: ");
                // System.out.println(g.toString());
                
            if(inputType==0)
                numEl = this.numElem;//this.connectivity.keySet().size();
            else if(inputType == 1)
                numEl = this.numElem;//this.connectivityMultiplex.keySet().size();
                      
            if(inputType==0){ //only one network
                
                Iterator<Integer> it = this.connectivity.keySet().iterator();
                
                while(it.hasNext()){
                    int elem = it.next();
                   double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0;
                   
                    Iterator<Integer> it1 = this.connectivity.keySet().iterator();

                    while(it1.hasNext()){
                    
                     int elem1 = it1.next();
                        
                    Iterator<Integer> it2 = this.connectivity.keySet().iterator();
                 
 
                     d.computeAllShortestPaths(elem1, this);//make a function CPAP with two layers as input
                                        
                    Collection<Vertex> vertices = g.getVertices();
		Vertex v=null;
		int i = 1;
 
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem1 || v.getId() == elem)
                            continue;
                        
                         Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
                         sum1 += allShortestPaths.size();
                        
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                         
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                             for(int zz=0;zz<s.size();zz++)
                                 if(s.get(zz).getId() == elem){
                                     sum2++;
                                     break;
                                 }
                         }
                }
                if(sum1!=0.0)
                sum3+=sum2/sum1;
                sum1 = 0.0; sum2 = 0.0;
               }
                    if(appset.directed == 0)
                            sum3 = sum3/((numEl-1)*(numEl-2));
                    else sum3 = sum3/(2*(numEl-1)*(numEl-2));
                     this.globalNodeInfo.put(elem, sum3);
              }
           }
            else if(inputType==1){//multiplex - modify this part
                System.out.println("Computing global BC...");
                int countEl = 0;
                    Iterator<Integer> it1 = this.connectivityMultiplex.keySet().iterator();
                    
                    while(it1.hasNext()){
                    
                        int elem1 = it1.next();
                        
                 if(appset.pathStrict == false){
                //      d = new Dijkstra(g);

                // g=d.generateGraph(this);
                    d.computeAllShortestPaths(elem1, this);//make new function for CASP with two parameters for two selected layers
                 System.out.println("Shortest paths computed!");
                 int test = 1;
                 if(test == 1) return;
                    Collection<Vertex> vertices = g.getVertices();
                    Vertex v;
 
                int cc = 0;    
                    
                double sum1 = 0.0, sum2 = 0.0;
                
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem1)
                            continue;
                       // System.out.println("Distance to " + v.getId() + ": "
			//		+ v.sourceDistance);
                        System.out.println("num shortest paths: "+v.allShortestPathsNode.size());
                        for(int zz=0;zz<v.allShortestPathsNode.size();zz++){
                            for(int zz1=0;zz1<v.allShortestPathsNode.get(zz).size();zz1++)
                                System.out.print(v.allShortestPathsNode.get(zz).get(zz1).getId()+" ");
                            System.out.println();
                        }
                        System.out.println("Source: "+elem1);
                        System.out.println("Target: "+v);
                       Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
                         sum1 += allShortestPaths.size();
                        System.out.println("SP size: "+allShortestPaths.size());
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                       //  if(elem1 == 4 && v.getId() == 2)
                       //      System.out.println("Paths for 4: ");
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                             for(int zz=0;zz<s.size();zz++){
                                 if(!this.globalNodeInfo.containsKey(s.get(zz).getId()))
                                     this.globalNodeInfo.put(s.get(zz).getId(), 0.0);
                                         
                                      if(sum1!=0.0){
                                            this.globalNodeInfo.put(s.get(zz).getId(),this.globalNodeInfo.get(s.get(zz).getId())+1.0/sum1);
                                          }
                             }
                         }
                 
                 sum1 = 0.0; sum2 = 0.0;
                
                 System.out.println("Progress: "+(cc++)+" "+vertices.size());
                 
                }
                         
                 System.out.println("Counting complete!");
                 
                // System.out.println("sum3: "+elem+" "+sum3);

                   for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY;
                        v1.setPrev(null);
                        
                   }
                
                 }
                 else{//modify
                        int NL = this.numLayers;
                        int disconected = 1, allLayers = 1;
                       
                        for(int k=0;k<NL;k++){
                        for(int i=0;i<NL;i++){
                            for(int j=0;j<NL;j++){
                                if(appset.graphSequence == 1 && (i!=j || i!=k || j!=k))
                                    continue;
                                else if(appset.graphSequence == 0 && (i!=k && j!=k))
                                    continue;

                                 d.computeAllShortestPaths(elem1, this,i,j);//make new function for CASP with two parameters for two selected layers
                 //System.out.println("L:  ("+i+","+j+")");
                    Collection<Vertex> vertices = g.getVertices();
                    if(vertices.size()!=0)
                        disconected = 0;
                    else allLayers = 0;
                    Vertex v;

                    int in = 0;
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			v = (Vertex) iterator.next();
                        if(v.getId() == elem1)
                            continue;
                        
                        double sum1 = 0.0;
                        //elem->v
                    for(Iterator<Vertex> iterator1 = vertices.iterator();iterator1.hasNext();){
                       
                        Vertex elemV = (Vertex) iterator1.next();
                         if(elemV.getId() == v.getId() || elemV.getId() == elem1) continue;
                         //System.out.println("SP prije:");
                         Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(elemV, v, this, i, k, j);//should be corrected
                       //  System.out.println("SP poslije:");
                        // System.out.println("int: "+elemV+" tar:"+v.getId()+" st:"+elem1);
                        // System.out.println("Layers: "+(i+","+k+","+j));
                         sum1 += allShortestPaths.size();
                       
                         Iterator<List<Vertex>> t = allShortestPaths.iterator();
                            double sum2 = 0.0;
                         while(t.hasNext()){
                             List<Vertex> s = t.next();
                            // for(int zz=0;zz<s.size();zz++)
                              //   System.out.print(s.get(zz).getId()+" ");
                           //  System.out.println();
                        
                             for(int zz=0;zz<s.size();zz++)
                                 if(s.get(zz).getId() == elemV.getId()){
                                     sum2++;
                                     break;
                                 }
                         }

                         if(!this.globalNodeInfo.containsKey(elemV.getId()))
                             this.globalNodeInfo.put(elemV.getId(), 0.0);
                         
                          if(sum1!=0.0)
                                 this.globalNodeInfo.put(elemV.getId(),this.globalNodeInfo.get(elemV.getId())+(sum2/sum1));//needs to be changed
                // System.out.println(elemV+" "+v.getId()+" "+elem1);
                 sum1 = 0.0; sum2 = 0.0;
                }
                            }
                 for(Vertex v1:vertices){
                        v1.sourceDistance = Double.POSITIVE_INFINITY; 
                         v1.setPrev(null);
                }
                         }
                       }
 
                 }                             
                }
                 countEl++;
                 System.out.println("Processed: "+countEl);
               }
            
             
                  Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                  double proc = 0.0;
                    while(it.hasNext()){
                        int elem = it.next();
                    
               if(appset.pathStrict == true) {    
                if(appset.graphSequence == 1){
                   
                        
                       if(appset.directed == 0)
                         this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/((numEl-1)*(numEl-2)*this.numLayers));
                       else  this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/(2*(numEl-1)*(numEl-2)*this.numLayers));
                    }
                else{
                    if(appset.directed == 0)
                     this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/((numEl-1)*(numEl-2)*this.numLayers*this.numLayers));
                    else  this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/(2*(numEl-1)*(numEl-2)*this.numLayers*this.numLayers));
                }
               }
               else{
                   if(appset.directed == 0)
                     this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/((numEl-1)*(numEl-2)));
                   else  this.globalNodeInfo.put(elem, this.globalNodeInfo.get(elem)/(2*(numEl-1)*(numEl-2)));
                }
                 proc+=1.0;
                // if(proc%100 == 0)
                     System.out.println(((proc/numEl)*100)+"%");
               }
             
                it = this.globalNodeInfo.keySet().iterator();
                 double max = -1.0;
                 while(it.hasNext()){
                     int e = it.next();
                     if(this.globalNodeInfo.get(e)>max && !Double.isInfinite(this.globalNodeInfo.get(e)))
                         max = this.globalNodeInfo.get(e);
                }
                
                it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int el = it.next();
                    System.out.println(el+" "+this.globalNodeInfo.get(el));
                    this.globalNodeInfo.put(el, this.globalNodeInfo.get(el)/max);
                }  
                
            }
 
            try{
                String p = appset.outFolderPath+"\\"+"globalInfo.txt";

                FileWriter fw = new FileWriter(p);
                
                Iterator<Integer> it = this.globalNodeInfo.keySet().iterator();
                while(it.hasNext()){
                    int n = it.next();
                    
                    fw.write(n+"\t"+this.globalNodeInfo.get(n)+"\n");
                    
                }
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
    }
    
    
    
    
      
      void loadGlobalInfo(ApplicationSettings appset){
          BufferedReader read = null;
          
          try{
              read = new BufferedReader(new FileReader(appset.outFolderPath+"\\"+"globalInfo.txt"));
              
              String line = "";
              
              while((line = read.readLine())!=null){
                  String tmp[] = line.split("\t");
                  this.globalNodeInfo.put(Integer.parseInt(tmp[0].trim()), Double.parseDouble(tmp[1].trim()));
              }
              read.close();
          }
          catch(IOException e){
              e.printStackTrace();
          }
          
      }
    
    double computeAverageDensity(ApplicationSettings appset){
        double res = 0.0;
        HashSet<Integer> traversed = new HashSet<>();
        
        double bandwidth = appset.Bandwith; double w;
		if (bandwidth!=100.0){	
                
                }
                else{
                    if(inputType==0){
                        
                        Iterator<Integer> it = connectivity.keySet().iterator();
                        
                        while(it.hasNext()){
                            int k = it.next();
                            
                            Iterator<Integer> it1 = connectivity.keySet().iterator();
                            
                            while(it1.hasNext()){
                                int k1 = it1.next();
                                if(k==k1 || traversed.contains(k1))
                                    continue;
                                res+=m_distancesS.get(k+"#"+k1);
                            }
                            traversed.add(k);
                        }
                        
                        return 2.0*res/(connectivity.keySet().size()*(connectivity.keySet().size()+1));
                        //connectivity
                        //m_distancesS.put((ii+"#"+jj), w); 
                    }
                    else{
                        
                        Iterator<Integer> it = connectivityMultiplex.keySet().iterator();
                        
                        while(it.hasNext()){
                            int k=it.next();
                            HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> con=connectivityMultiplex.get(k);
                            
                            Iterator<Integer> it2 = con.keySet().iterator();
                            
                            while(it2.hasNext()){
                                int l=it2.next();
                                
                                ArrayList<Triplet<Integer,Integer,Double>> tmp = con.get(l);
                                
                                for(int kA=0;kA<tmp.size();kA++){
                                    if(tmp.get(kA).getValue0()!=k && !traversed.contains(tmp.get(kA).getValue0()))
                                    res+=tmp.get(kA).getValue2();
                                }
                                
                            }
                            
                            traversed.add(k);
                            
                        }
                        
                        System.out.println("num layers: "+numLayers);
                        System.out.println("num nodes: "+connectivityMultiplex.keySet().size());
                        System.out.println("res: "+res);
                        
                        if(networkType==0)
                              return 2.0*res/(connectivityMultiplex.keySet().size()*(connectivityMultiplex.keySet().size()+1)*numLayers);
                        else{ 
                            if(appset.directed == 0)
                                return 2.0*res/(connectivityMultiplex.keySet().size()*(connectivityMultiplex.keySet().size()+1)*numLayers+2*connectivityMultiplex.keySet().size()^(numLayers));//implement this option
                            else
                                return res/(connectivityMultiplex.keySet().size()*(connectivityMultiplex.keySet().size()+1)*numLayers+2*connectivityMultiplex.keySet().size()^(numLayers));//implement this option
                        }
                    }
                }
        
        return res;
    }
    
    void computeDistanceMatrix(RuleReader rr, Mappings map, int MaxDistance, int numElements){
       for(int i=0;i<numElements-1;i++){
           for(int j=i+1;j<numElements;j++){
                   double sum=0.0;
                   int coocurence=0, finalSum=0;
                   for(int k=rr.newRuleIndex;k<rr.rules.size();k++){
                       if(rr.rules.get(k).elements.contains(/*map.idExample.get(i)*/i) && rr.rules.get(k).elements.contains(/*map.idExample.get(j)*/j)){
                           coocurence++; sum+=(1-((double)(rr.rules.get(k).elements.size()-2))/numElements);
                       }
                   }
                   if(coocurence!=0)
                   finalSum=(int)(MaxDistance-(sum/coocurence)*MaxDistance);
                   else
                       finalSum=100;
                   distanceMatrix[map.exampleId.get(map.idExample.get(i))][map.exampleId.get(map.idExample.get(j))]=finalSum;
                   distanceMatrix[map.exampleId.get(map.idExample.get(j))][map.exampleId.get(map.idExample.get(i))]=finalSum;
           }
       }
    }
    
    void computeDistanceMatrix(ArrayList<Redescription> redescriptions, Mappings map, int MaxDistance, int numElements, int[] oldRindex){
       for(int i=0;i<numElements-1;i++){
           for(int j=i+1;j<numElements;j++){
                   double sum=0.0;
                   int coocurence=0, finalSum=0;
                   for(int k=oldRindex[0];k<redescriptions.size();k++){
                       if(redescriptions.get(k).elements.contains(/*map.idExample.get(i)*/i) && redescriptions.get(k).elements.contains(/*map.idExample.get(j)*/j)){
                           coocurence++; sum+=(1-((double)(redescriptions.get(k).elements.size()-2))/numElements);
                       }
                   }
                   finalSum=(int)(MaxDistance-(sum/coocurence)*MaxDistance);
                   distanceMatrix[map.exampleId.get(map.idExample.get(i))][map.exampleId.get(map.idExample.get(j))]=finalSum;
                   distanceMatrix[map.exampleId.get(map.idExample.get(j))][map.exampleId.get(map.idExample.get(i))]=finalSum;
           }
       }
    }
    
    void loadDistance(File input, Mappings map){
        
        try {
      Path path =Paths.get(input.getAbsolutePath());
      System.out.println("Path: "+input.getAbsolutePath());
      BufferedReader reader;
      String file="";
      reader = Files.newBufferedReader(path,ENCODING);
      String line = null;
      distanceMatrix=new int[map.exampleId.keySet().size()][map.exampleId.keySet().size()];
      int rowInd=0;
      while ((line = reader.readLine()) != null) {
               String tmp[]=line.split(" ");
               for(int j=0;j<tmp.length;j++)
                   distanceMatrix[rowInd][j]=Integer.parseInt(tmp[j].trim());
               rowInd++;
        
    }
      reader.close();
         }
         catch(IOException io){
             io.printStackTrace();
         }
    }
    
    void reset(ApplicationSettings appset){
        
        for(int i=0;i<numElem;i++)
            for(int j=0;j<numElem;j++)
                distanceMatrix[i][j]=appset.maxDistance;
        
        for(int i=0;i<numElem;i++)
            distanceMatrix[i][i]=0;
    }
    
    void writeToFile(File output, Mappings map, ApplicationSettings appset){
  
         /*try{
         PrintWriter out = new PrintWriter(output.getAbsolutePath());
         for(int i=0;i<numElem;i++)
             for(int j=0;j<numElem;j++){
                /* String ex=map.idExample.get(i);
                 String ex1=map.idExample.get(j);
                 String tmp[]=ex.split("\\\"");
                 String tmp1[]=ex1.split("\\\"");
                 int value=Integer.parseInt(tmp[1]),value1=Integer.parseInt(tmp1[1]);*/
        // out.write(value+","+value1+","+distanceMatrix[i][j]+"\n");
        /*  out.write(i+","+j+","+distanceMatrix[i][j]+"\n");
             }
         out.close();
         }
         catch(FileNotFoundException ex){
             ex.printStackTrace();
         }
         */ //write sparse
           try{
         PrintWriter out = new PrintWriter(output.getAbsolutePath());
         for(int i=0;i<numElem;i++)
             for(int j=0;j<numElem;j++){
                /* String ex=map.idExample.get(i);
                 String ex1=map.idExample.get(j);
                 String tmp[]=ex.split("\\\"");
                 String tmp1[]=ex1.split("\\\"");
                 int value=Integer.parseInt(tmp[1]),value1=Integer.parseInt(tmp1[1]);*/
        // out.write(value+","+value1+","+distanceMatrix[i][j]+"\n");
                 if(distanceMatrix[i][j]!=appset.maxDistance)
          out.write(i+","+j+","+distanceMatrix[i][j]+"\n");
                 else continue;
             }
         out.close();
         }
         catch(FileNotFoundException ex){
             ex.printStackTrace();
         }
         
         //return set;
    }
    
    void resetFile(File output){
        try{
         PrintWriter out = new PrintWriter(output.getAbsolutePath());
         out.close();
         }
         catch(FileNotFoundException ex){
             ex.printStackTrace();
         }
    }
    
}

