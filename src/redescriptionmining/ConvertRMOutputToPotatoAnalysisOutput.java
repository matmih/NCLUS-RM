/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import static redescriptionmining.SettingsReader.ENCODING;

/**
 *
 * @author matej
 */
public class ConvertRMOutputToPotatoAnalysisOutput {
    public static void main(String args[]){
        
         ApplicationSettings appset=new ApplicationSettings();
        appset.readSettings(new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\SettingsPotato.set"));
        System.out.println("Num targets: "+appset.numTargets);
        System.out.println("Num trees in RS: "+appset.numTreesinForest);
        System.out.println("Average tree depth in RS: "+appset.aTreeDepth);
        System.out.println("Allow left side rule negation: "+appset.leftNegation);
        System.out.println("Allow right side rule negation: "+appset.rightNegation);
        System.out.println("Allow left side rule disjunction: "+appset.leftDisjunction);
        System.out.println("Allow right side rule disjunction: "+appset.rightDisjunction);
        
        Mappings fid=new Mappings();
        DataSetCreator dat=new DataSetCreator(appset.viewInputPaths.get(0)/*appset.outFolderPath+"\\input1.arff"*/,appset.viewInputPaths.get(1)/*appset.outFolderPath+"\\input2.arff"*/ , appset.outFolderPath);
        fid.createIndex(appset.outFolderPath+"\\Jinput.arff");
            
        HashMap<String,String> cnameToOrigName = new HashMap<>();
        HashMap<String,String> bincodeNameTODescription = new HashMap<>();
        
        File inputNames = new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\bGSE_data_header_transposed");
        File inputorigNames = new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\bGSE_data_header_transposedOrig");
        File inputBins = new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\stu_Agilent_4x44k_2017-03-14_mapping.txt");
        
            File input=new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\redescriptionsIterativePotatoRF.rr1.rr");
            BufferedReader reader;
            
            String dataInput="";//add path
            //DataSetCreator dat=new DataSetCreator(dataInput);
           // ArrayList<RedescriptionReReMi> red=new ArrayList<>();
            RedescriptionSet rs=new RedescriptionSet(); 
        String file="";
        Mappings map=new Mappings();
        
        ArrayList<String> crmN = new ArrayList<>();
        ArrayList<String> OrigcrmN = new ArrayList<>();
        
        try {
      Path path =Paths.get(inputNames.getAbsolutePath());
      System.out.println("Path: "+inputNames.getAbsolutePath());
      reader = Files.newBufferedReader(path,ENCODING);
      String line = null;
      int count=0;

      while ((line = reader.readLine()) != null){
            crmN.add(line.trim());
        }
    
      reader.close();
         }
         catch(Exception e){
             e.printStackTrace();
         }
    
        try {
      Path path =Paths.get(inputorigNames.getAbsolutePath());
      System.out.println("Path: "+inputorigNames.getAbsolutePath());
      reader = Files.newBufferedReader(path,ENCODING);
      String line = null;
      int count=0;

      while ((line = reader.readLine()) != null){
            OrigcrmN.add(line.trim());
        }
    
      reader.close();
         }
         catch(Exception e){
             e.printStackTrace();
         }
        
        for(int i=0;i<OrigcrmN.size();i++)
            cnameToOrigName.put(crmN.get(i), OrigcrmN.get(i));
        
        try {
      Path path =Paths.get(inputBins.getAbsolutePath());
      System.out.println("Path: "+inputBins.getAbsolutePath());
      reader = Files.newBufferedReader(path,ENCODING);
      String line = null;
      int count=0;

      while ((line = reader.readLine()) != null){
          
           String tmp[] = line.split("\t");
           if(tmp.length<4)
               continue;
           String first = tmp[0]+" "+tmp[1]+"\t"+tmp[2];
           String second = tmp[3];
           bincodeNameTODescription.put(first,second);//modify 
        }
    
      reader.close();
         }
         catch(Exception e){
             e.printStackTrace();
         } 
        
          try {
      Path path =Paths.get(input.getAbsolutePath());
      System.out.println("Path: "+input.getAbsolutePath());
      reader = Files.newBufferedReader(path,ENCODING);
      String line = null;
      int count=0;
      
      int ElemCount=0;
      int AttrCount=0;
      
        System.out.println("Number of attributes: "+(dat.schema.getNbAttributes()-1));
        System.out.println("W2 index: "+dat.W2indexs.get(0));
      
      //RedescriptionReReMi r=null;
       Redescription r=null;
       int covered=0, coveredUnion=0;
      while ((line = reader.readLine()) != null){
          if(line.contains("W1R: ")){
               r=new Redescription(dat);
               r.ruleStrings.add(line.substring(5));
          }
          else if(line.contains("W2R: ")){
              String rule = line.substring(5);
              
              String attrs[] = rule.split("AND");
              String ruleNew="";
              
              ArrayList<String> s = new ArrayList<>();
              
              for(int i=0;i<attrs.length;i++){
                  String t[]=attrs[i].split(">");
                  System.out.println(t[0].trim());
                  String orig = cnameToOrigName.get(t[0].trim());
                  System.out.println(orig);
                  String origT[] = orig.split("_");
                  orig=origT[0];
                  for(int z=1;z<origT.length-1;z++)
                  orig = orig+"_"+origT[z];
                   s.add(orig.trim());
                  orig+=" >"+t[1].trim();
                  ruleNew+=orig;
                  if(i+1<attrs.length)
                     ruleNew+=" AND ";
              }
              
              ruleNew+="\n\n";
              String atDes="";
              String atCode="";
              for(int i=0;i<s.size();i++){
                   atDes="";
                   atCode="";
                 Iterator<String> it= bincodeNameTODescription.keySet().iterator();
                 //System.out.println("s: "+s.get(i));
                 while(it.hasNext()){
                     String str = it.next();
                     String dec = bincodeNameTODescription.get(str);
                     if(str.contains(s.get(i))){
                          //System.out.println("str: "+str);
                         // System.out.println("dec: "+dec);
                           String t1[] = str.split("\t");
                          atCode+=t1[0].trim();
                          if(it.hasNext())
                              atCode+="| ";
                          atDes+=dec.trim();
                          if(it.hasNext())
                              atDes+="| ";
                              
                     }
                     else continue;
                         
                 }
                 
                 //System.out.println("atCode: "+atCode);
                  //System.out.println("atDes: "+atDes);
                 ruleNew+=s.get(i)+": "+atCode+"\t"+atDes+"\n";
                 
              }
              System.out.println(ruleNew);
              r.ruleStrings.add(ruleNew);
          }
          else if(line.contains("JS: ")){
              r.JS= Double.parseDouble(line.substring(4));
          }
          else if(line.contains("p-value: ")){
              r.pVal=Double.parseDouble(line.substring(9));
          }
          else if(line.contains("Support intersection: "))
              continue;
          else if(line.contains("Union elements: ")){
              coveredUnion=1;
          }
          else if(line.contains("Covered examples (intersection):"))
              covered=1;
          else if(line.contains("\""))
              if(covered==1){
                  //line=line.replace("\"", "");
                  String tmp[]=line.split(" ");
                  for(int i=0;i<tmp.length;i++)
                      r.elements.add(fid.exampleId.get(tmp[i]));
                  covered=0;
                  rs.redescriptions.add(r);
              }  
              else if(coveredUnion==1){
                  String tmp[]=line.split(" ");
                  System.out.println(line);
                  System.out.println(tmp);
                  for(int i=0;i<tmp.length;i++)
                      r.elementsUnion.add(fid.exampleId.get(tmp[i]));
                  coveredUnion=0;
              }
        }
    
      reader.close();
         
         File output = new File("C:\\Users\\matej\\Downloads\\Potatio disease analysis\\podaci\\redescriptionsIterativePotatoRFFinal.rr1");
      
          rs.writeToFilePotato(output.getAbsolutePath(), dat, fid, appset);
          
          }
         catch(Exception e){
             e.printStackTrace();
         }
        
    }
}
