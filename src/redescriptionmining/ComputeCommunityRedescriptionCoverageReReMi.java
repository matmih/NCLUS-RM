/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import gnu.trove.iterator.TIntIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import static jdk.internal.org.jline.utils.Colors.s;

/**
 *
 * @author Matej
 */
public class ComputeCommunityRedescriptionCoverageReReMi {
   public static void main(String args[]){
          
        ApplicationSettings appset=new ApplicationSettings();
        appset.readSettings(new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp5\\SettingsFacebook.set"));
        appset.readPreference();  
        System.out.println("Num targets: "+appset.numTargets);
        System.out.println("Num trees in RS: "+appset.numTreesinForest);
        System.out.println("Average tree depth in RS: "+appset.aTreeDepth);
        System.out.println("Allow left side rule negation: "+appset.leftNegation);
        System.out.println("Allow right side rule negation: "+appset.rightNegation);
        System.out.println("Allow left side rule disjunction: "+appset.leftDisjunction);
        System.out.println("Allow right side rule disjunction: "+appset.rightDisjunction);
        System.out.println("Types of LSTrees: "+appset.treeTypes.get(0));
        System.out.println("Types of RSTrees: "+appset.treeTypes.get(1));
        System.out.println("Use Network information: "+appset.useNC.toString());
        System.out.println("Spatial matrix: "+appset.spatialMatrix.toString());
        System.out.println("Spatial measure: "+appset.spatialMeasures.toString());
        System.out.println("JSType: "+appset.jsType);
        System.out.println("Attribute importance: "+appset.attributeImportance);
    
      DataSetCreator datJ=new DataSetCreator(appset.viewInputPaths, appset.outFolderPath,appset);   

      Mappings fid=new Mappings();
      fid.createIndex(appset.outFolderPath+"\\Jinput.arff");
      System.out.println("categorical: "+fid.catAttInd.size());
      NHMCDistanceMatrix nclMatInit=null;
      
      ArrayList<Redescription> redescriptions = new ArrayList<>();
      File input = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\FacebookRedsSplit.txt");
      BufferedReader read = null;
      Path p = Paths.get(input.getAbsolutePath());
     ArrayList<Double> jaccards = new ArrayList<>(); 
      try{
          read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
      String line = "";
      while((line = read.readLine())!=null){
          String q1 = line;
          String q2 = read.readLine();
          q1 = q1.replace("Q1: ", "").replace("[","").replace("]", "");
          q2 = q2.replace("Q2: ", "").replace("[","").replace("]", "");
          
          String tmp1[] = q1.split(" AND ");
          String tmp2[] = q2.split(" AND ");
          
          double lower = Double.POSITIVE_INFINITY, upper = Double.NEGATIVE_INFINITY;
          String att = "";
          int cnt = 1;
          HashSet<Integer> instancesW1 = new HashSet<>();
          HashSet<Integer> instancesW2 = new HashSet<>();
          for(String pp:tmp1){
              int negated = 0;
              String prt[] = pp.split("<=");
              if(prt.length == 1){
                  if(prt[0].trim().contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
                      
                  att = prt[0].trim();
              }
              if(prt.length == 2){
                  try{
                  double b = Double.parseDouble(prt[1].trim());
                  if(b>upper) upper = b;
                  lower = Double.NEGATIVE_INFINITY;
                  att = prt[0].trim();
                  }
                  catch(NumberFormatException e){
                      double b = Double.parseDouble(prt[0].trim());
                      if(b<lower) lower = b;
                      upper = Double.POSITIVE_INFINITY;
                      att = prt[1].trim();
                  }
              }
              else if(prt.length == 3){
                  double b = Double.parseDouble(prt[2].trim());
                  double c = Double.parseDouble(prt[0].trim());
                  att = prt[1].trim();
                  if(b>upper) upper = b;
                  if(c<lower) lower = c;
              }
              
              System.out.println(att+" "+lower+" "+upper);
              
              if(cnt == 1){
                  cnt = 0;
                  int atind = fid.attId.get(att);
                  for(int ii = 0;ii<datJ.numExamples;ii++){
                      if(fid.catAttInd.contains(atind)){
                           String val = datJ.getValueCategorical(atind, ii);
                          if((val.equals("1") && negated == 0) || (val.equals("0") && negated == 1))
                              instancesW1.add(ii);
                      }
                      else{
                          double val = datJ.getValue(atind,ii);
                          if(val>lower && val<=upper)
                              instancesW1.add(ii);
                      }
                  }
              }
              else{
                   int atind = fid.attId.get(att);
                   for(int ii = 0;ii<datJ.numExamples;ii++){
                      if(fid.catAttInd.contains(atind)){
                           String val = datJ.getValueCategorical(atind, ii);
                          if((!val.equals("1") && negated == 0) ||(!val.equals("0") && negated == 1))
                              instancesW1.remove(ii);
                      }
                      else{
                          if(!instancesW1.contains(ii)) continue;
                          double val = datJ.getValue(atind,ii);
                          if(val>lower && val<=upper){

                          }
                          else instancesW1.remove(ii);
                      }
                  }
              }
              
              lower = Double.POSITIVE_INFINITY; upper = Double.NEGATIVE_INFINITY;
          }
          
          cnt = 1;
          System.out.println("W2: ");
          lower = Double.POSITIVE_INFINITY; upper = Double.NEGATIVE_INFINITY;
          att = "";
          for(String pp:tmp2){
              int negated = 0;
              String prt[] = pp.split("<=");
              if(prt.length == 1){
                  if(prt[0].contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
                  att = prt[0].trim();
              }
              if(prt.length == 2){
                  try{
                  double b = Double.parseDouble(prt[1].trim());
                  if(b>upper) upper = b;
                  lower = Double.NEGATIVE_INFINITY;
                  att = prt[0].trim();
                  }
                  catch(NumberFormatException e){
                      double b = Double.parseDouble(prt[0].trim());
                      if(b<lower) lower = b;
                      upper = Double.POSITIVE_INFINITY;
                      att = prt[1].trim();
                  }
              }
              else if(prt.length == 3){
                  double b = Double.parseDouble(prt[2].trim());
                  double c = Double.parseDouble(prt[0].trim());
                  att = prt[1].trim();
                  if(b>upper) upper = b;
                  if(c<lower) lower = c;
              }
              
              System.out.println(att+" "+lower+" "+upper);
              
               if(cnt == 1){
                  cnt = 0;
                  int atind = fid.attId.get(att);
                  for(int ii = 0;ii<datJ.numExamples;ii++){
                      if(fid.catAttInd.contains(atind)){
                          String val = datJ.getValueCategorical(atind, ii);
                          if((val.equals("1") && negated == 0) || (val.equals("0") && negated == 1))
                              instancesW2.add(ii);
                      }
                      else{
                          double val = datJ.getValue(atind,ii);
                          if(val>lower && val<=upper)
                              instancesW2.add(ii);
                      }
                  }
              }
             else{
                   int atind = fid.attId.get(att);
                   for(int ii = 0;ii<datJ.numExamples;ii++){
                      if(fid.catAttInd.contains(atind)){
                          String val = datJ.getValueCategorical(atind, ii);
                          System.out.println("val: "+val);
                          if((!val.equals("1") && negated == 0) || (!val.equals("0") && negated == 1))
                              instancesW2.remove(ii);
                      }
                      else{
                          if(!instancesW2.contains(ii)) continue;
                          double val = datJ.getValue(atind,ii);
                          if(val>lower && val<=upper){

                          }
                          else instancesW2.remove(ii);
                      }
                  }
              }
              
              lower = Double.POSITIVE_INFINITY; upper = Double.NEGATIVE_INFINITY;
          }
          
          HashSet<Integer> intersection = new HashSet<Integer>();
          for(int ii:instancesW1)
              if(instancesW2.contains(ii))
                  intersection.add(ii);
          
          System.out.println(instancesW1.size()+" "+instancesW2.size()+" "+intersection.size());
          System.out.println("JS: "+(intersection.size()/(double)(instancesW1.size()+instancesW2.size()-intersection.size())));
          Redescription r = new Redescription();
          r.JS = intersection.size()/(double)(instancesW1.size()+instancesW2.size()-intersection.size());
          for(int i:intersection) r.elements.add(i);
          redescriptions.add(r);
                        read.readLine();
                        instancesW1.clear(); instancesW2.clear();
      }
      read.close();
      }
      catch(IOException e){
          e.printStackTrace();
      }
   
   
   
   File inputCircles = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Community detection\\0.circles");
        
      read = null;
        
        HashMap<String, HashSet<String>> circ = new HashMap<>();
        
        HashSet<String> allInst = new HashSet<>();
        
        try{
            p = Paths.get(inputCircles.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            String line = "";
            
             while((line = read.readLine())!=null){
                String tmp[] = line.split("\t");
                HashSet<String> t = new HashSet<>();
                circ.put(tmp[0], new HashSet<String>());
                
                for(int i=1;i<tmp.length;i++){
                    circ.get(tmp[0]).add(tmp[i]);
                }
            }
            
            read.close();
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        
        for(Redescription r:redescriptions){
             TIntIterator iit = r.elements.iterator();
                
                while(iit.hasNext()){
                    int el = iit.next();
                    el = el+1;
                    allInst.add(el+"");
                }
        }
        
        System.out.println("Described instances: "+allInst.size());
        
        HashMap<String,ArrayList<Redescription>> circRed = new HashMap<>();
        
        for(int i=0;i<redescriptions.size();i++){
            System.out.println(i+" "+"Red size: "+redescriptions.get(i).elements.size());
            Iterator<String> it = circ.keySet().iterator();
            
            while(it.hasNext()){
                String c = it.next();
                HashSet<String> inst = circ.get(c);
                
                HashSet<String> inter = new HashSet<String>();
                TIntIterator iit = redescriptions.get(i).elements.iterator();
                
                while(iit.hasNext()){
                    int el = iit.next();
                    el = el+1;
                    if(inst.contains(el+""))
                    inter.add(el+"");
                }

                   System.out.println(c+" "+((double)inter.size()/redescriptions.get(i).elements.size()));
                   double p1 = (double)inter.size()/redescriptions.get(i).elements.size();
                   if(p1>=0.6){
                       if(!circRed.containsKey(c)){
                           circRed.put(c, new ArrayList<>());
                           circRed.get(c).add(redescriptions.get(i));
                       }
                       else
                           circRed.get(c).add(redescriptions.get(i));
                   }
            }
            System.out.println();
            
            
        }
        
        
        Iterator<String> it = circRed.keySet().iterator();
        
        while(it.hasNext()){
            String c = it.next();
            ArrayList<Redescription> rs = circRed.get(c);
            System.out.println("All red: "+redescriptions.size());
            System.out.println("Num red: "+rs.size());
            HashSet<String> all = new HashSet<>();
            for(Redescription r:rs){
                TIntIterator iit = r.elements.iterator();
                
                while(iit.hasNext()){
                    int el = iit.next();
                    el = el+1;
                all.add(el+"");
            }
            }
            
            HashSet<String> inst = circ.get(c);
            HashSet<String> inter = new HashSet<>();
            
            for(String s:all){
                if(inst.contains(s))
                    inter.add(s);
            }
            
            System.out.println(c+" Acc: "+((double)inter.size()/all.size()));
            System.out.println("perc: "+((double)inter.size()/(inst.size())));
            System.out.println(c+" JS: "+((double)inter.size()/(inst.size()+all.size()-inter.size())));
            
        }
   
   } 
   
}
