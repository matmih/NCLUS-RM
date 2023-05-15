/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Matej
 */
public class ComputeReReMiNetworkMeasuers {
    public static void main(String args[]){
       
        ApplicationSettings appset=new ApplicationSettings();
       // appset.readSettings(new File(args[0]));
        appset.readSettings(new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp5\\Settings.set"));
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
      //File input = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\FacebookReds.txt");
      File input = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\CountryRedsLayered.txt");
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
                   if(prt[0].trim().contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
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
                   if(prt[0].trim().contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
                  double b = Double.parseDouble(prt[2].trim());
                  double c = Double.parseDouble(prt[0].trim());
                  att = prt[1].trim();
                  if(b>upper) upper = b;
                  if(c<lower) lower = c;
              }
              
              System.out.println("Negated: "+negated);
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
                           if(Double.isInfinite(val))
                                  continue;
                          if(negated == 0){
                        //  if(val>lower && val<=upper){ //for ReReMi
                          if(val>=lower && val<=upper){ //for Layered and Split
                              System.out.println("ADDING: "+ii);
                              instancesW1.add(ii);
                          }
                          }
                          else{
                              if(val == Double.POSITIVE_INFINITY)
                                  continue;
                              //if(val<=lower || val>upper)//for ReReMi
                              if(val<lower || val>upper)//for Layered and Split
                              instancesW1.add(ii);
                          }
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
                          if(val == Double.POSITIVE_INFINITY){
                                  instancesW1.remove(ii);
                                  continue;
                              }
                          if(negated == 0){
                              
                          /*if(val>lower && val<=upper){
                              
                          }*/
                          if(val>=lower && val<=upper){
                              
                          }
                          else  instancesW1.remove(ii);
                          }
                          else{
                              /*if(val<=lower || val>upper){

                          }*/
                               if(val<lower || val>upper){

                          }
                          else  instancesW1.remove(ii);
                          }
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
                   if(prt[0].contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
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
                   if(prt[0].contains("NOT")){
                      negated = 1;
                      prt[0] = prt[0].replace("NOT", "");
                  }
                  // System.out.println(prt[0]+" "+prt[1]+" "+prt[2]);
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
                          if(val ==Double.POSITIVE_INFINITY)
                              continue;
                             if(negated == 0){
                          /*if(val>lower && val<=upper)
                              instancesW2.add(ii);
                          }*/
                             if(val>=lower && val<=upper)
                              instancesW2.add(ii);
                          }
                          else{
                             // if(val<=lower || val>upper)
                             if(val<lower || val>upper)
                              instancesW2.add(ii);
                          }
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
                         /* if(!instancesW2.contains(ii)) continue;
                          double val = datJ.getValue(atind,ii);
                          if(val>lower && val<=upper){

                          }
                          else instancesW2.remove(ii);*/
                          
                         if(!instancesW1.contains(ii)) continue;
                          double val = datJ.getValue(atind,ii);
                          if(val == Double.POSITIVE_INFINITY){
                                  instancesW2.remove(ii);
                                  continue;
                              }
                          if(negated == 0){
                              
                          /*if(val>lower && val<=upper){
                              
                          }*/
                          if(val>=lower && val<=upper){
                              
                          }
                          else  instancesW2.remove(ii);
                          }
                          else{
                           /*   if(val<=lower || val>upper){

                          }*/
                          if(val<lower || val>upper){

                          }
                          else  instancesW2.remove(ii);
                          }
                          
                          
                      }
                  }
              }
              System.out.println("instW2: "+instancesW2.size());
              lower = Double.POSITIVE_INFINITY; upper = Double.NEGATIVE_INFINITY;
          }
          
          HashSet<Integer> intersection = new HashSet<Integer>();
          for(int ii:instancesW1){
              if(instancesW2.contains(ii))
                  intersection.add(ii);
              else System.out.println("NOT CONTAINED: "+ii);
          }
          
          System.out.println("All W1:");
           for(int ii:instancesW1){
               System.out.print(ii+" ");
           }
           
           System.out.println("All W2:");
           for(int ii:instancesW2){
               System.out.print(ii+" ");
           } 
          
          System.out.println("\n"+instancesW1.size()+" "+instancesW2.size()+" "+intersection.size());
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

int test = 0;

if(test == 1)
    return;
      
String measures[] = {"AverageDegreeGlobal","AverageNeighbourhoodDegreeGlobal","AverageTrianglesGlobal","GlobalBetweenessCentralityScore","GlobalCentralityScore","GlobalHubScore"};
ArrayList<ArrayList<Double>> measuresStats = new ArrayList<>();
for(String s:measures){
    appset.spatialMeasures.set(0, s);
    appset.spatialMeasures.set(1, s);
      if(appset.distanceFilePaths.size()==0 && appset.useNetworkAsBackground==true || appset.computeDensity == true){
            nclMatInit = new NHMCDistanceMatrix();
            nclMatInit.loadDistanceMatrix(appset);
            nclMatInit.numElem = datJ.numExamples;
            if(appset.spatialMeasures.get(0).equals("AverageTrianglesGlobal"))
                   nclMatInit.computeGlobalTriangles(appset.graphSequence, appset);//add other global measures, tested! (2x the score when non-directed, not important)
            else if(appset.spatialMeasures.get(0).equals("AverageDegreeGlobal"))
                    nclMatInit.computeGlobalDegree(appset.graphSequence, appset);// tested!
            else if(appset.spatialMeasures.get(0).equals("AverageNeighbourhoodDegreeGlobal"))
                    nclMatInit.computeGlobalNeighbourhoodDegree(appset.graphSequence, appset);//tested!
            else if(appset.spatialMeasures.get(0).equals("GlobalHubScore"))  
                    nclMatInit.computeGlobalZScore(appset.graphSequence, appset);//tested!
            else if(appset.spatialMeasures.get(0).equals("GlobalCentralityScore"))//tested!
                nclMatInit.computeGlobalCentrality(appset.graphSequence,appset);
            else if(appset.spatialMeasures.get(0).equals("GlobalBetweenessCentralityScore"))//tested!
                nclMatInit.computeBetweenessCentralityOpt(appset.graphSequence,appset); 
    }
      
      //load reds
      //compute measures for supports
      measuresStats.add(new ArrayList<Double>());
      
      
      for(int i=0;i<redescriptions.size();i++){
          double val = redescriptions.get(i).computeGlobalMeasure(nclMatInit, appset, i);
          measuresStats.get(measuresStats.size()-1).add(val);
      }
     
      if(measuresStats.size() == 3){
          measuresStats.add(new ArrayList<>());
            for(int i=0;i<redescriptions.size();i++){
          double val = redescriptions.get(i).computeNetworkDensity(nclMatInit, appset);
          measuresStats.get(measuresStats.size()-1).add(val);
      }
      }
          //redescriptions.get(i).computeNetworkDensity(nclMatInit, appset);
   
   }

System.out.println(redescriptions.size()+" "+measuresStats.size());
for(int i=0;i<measuresStats.size();i++)
    System.out.print(measuresStats.get(i).size()+" ");
System.out.println();

try{
   // FileWriter fw = new FileWriter("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\NetworkStatsFacebook.txt");
    FileWriter fw = new FileWriter("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\NetworkStatsCountryLayered.txt");
    
    for(int i=0;i<measuresStats.size();i++){
        for(int j = 0; j<measuresStats.get(i).size();j++){
            System.out.print(measuresStats.get(i).get(j)+" ");
            fw.write(measuresStats.get(i).get(j)+" ");
        }
        System.out.println();
        fw.write("\n");
    }
    fw.close();
    
    //fw = new FileWriter("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\NetworkStatsFacebookJaccard.txt");
    fw = new FileWriter("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\NetworkStatsCountryJaccardLayered.txt");
    for(int i=0;i<redescriptions.size();i++)
        fw.write(redescriptions.get(i).JS+" ");
    fw.close();
}
catch(IOException e){
    e.printStackTrace();
}

 }
}
