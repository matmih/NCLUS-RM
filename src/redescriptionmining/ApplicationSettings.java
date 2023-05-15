/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author matej
 */
public class ApplicationSettings {
        final static Charset ENCODING = StandardCharsets.UTF_8;

        int minSupport,maxSupport, numIterations, numnewRAttr, numRetRed, numTreesinForest, aTreeDepth, numTargets, clusteringMemory, Bandwith, maxDistance,numRandomRestarts, attributeImportance, containsView, numSupplementTrees, redesSetSizeType, numInitial,jsType/*, typeOfLSTrees, typeOfRSTrees*/, graphSequence, directed;
        boolean minimizeRules, unguidedExpansion, allowSERed, useJoin, leftNegation, rightNegation, leftDisjunction, rightDisjunction, computeDMfromRules, networkInit, useNetworkAsBackground, useSplitTesting, computeDensity, pathStrict;
        double minJS, minAddRedJS, maxPval, JSImpWeight, PValImpWeight, AttDivImpWeight, ElemDivImpWeight, RuleSizeImpWeight, Alpha, percentageForTrain;
       public String javaPath, clusPath, outFolderPath, outputName,initClusteringFileName, preferenceFilePath;
        ArrayList<String> viewInputPaths, distanceFilePaths, spatialMatrix, spatialMeasures, importantAttributes;
        ArrayList<Integer> treeTypes;
         public HashMap<String,Integer> preferenceHeader;
         public ArrayList<double []> preferences;
        ArrayList<Boolean> useNC;

        public ApplicationSettings(){
            pathStrict = false;
            graphSequence = 0; directed = 0;//only for pathMeasures (pathStrict : true - path must be short in all layers, false - the shortest path combining all edges regardless layers)
            minSupport=1; numIterations=1; numnewRAttr=1; numRetRed=Integer.MAX_VALUE; numTreesinForest=50; aTreeDepth=Integer.MAX_VALUE; numTargets=1600; clusteringMemory=2000;numRandomRestarts=1; numSupplementTrees=0;
            useJoin=true; minimizeRules=true; unguidedExpansion=false; redesSetSizeType=0; //0-flexible, 1-exact
            allowSERed=false; attributeImportance=0;  jsType=0; //add attributeImportanceArray - for each view possible different type!
            minJS=0.6; minAddRedJS=0.1; maxPval=0.02; JSImpWeight=0.2; PValImpWeight=0.2; AttDivImpWeight=0.2; ElemDivImpWeight=0.2; RuleSizeImpWeight=0.2;
            javaPath="java"; clusPath="clus.jar"; outFolderPath=""; outputName="Redescriptions.rr"; initClusteringFileName=""; preferenceFilePath="preferences.txt";
            viewInputPaths=new ArrayList<>();
            viewInputPaths.add("input1.arff"); viewInputPaths.add("input2.arff");
            maxSupport=Integer.MAX_VALUE;
            leftNegation=false; rightNegation=false; leftDisjunction=false; rightDisjunction=false;
            treeTypes=new ArrayList<>();
            treeTypes.add(1); treeTypes.add(1);
             preferences=new ArrayList<>(); 
            useNC=new ArrayList<>();
            useNC.add(false); useNC.add(false);
            distanceFilePaths=new ArrayList<>();
            spatialMatrix=new ArrayList<>();
            spatialMeasures=new ArrayList<>();
            preferenceHeader=new HashMap<>();
            Bandwith=100;
            Alpha=0.5;
            computeDMfromRules=false;
            maxDistance=100;
            networkInit=false;
            computeDensity=false;
            useNetworkAsBackground=false;
            useSplitTesting=false;
            percentageForTrain=0.8;
            importantAttributes=new ArrayList<>();
            containsView=0; // 0 - no attribute constraints, 1 attr constr for W1, 2 attr constr for W2, 3 attr constr for both
            //typeOfLSTrees=1; //1 regresion, 0 classification, 2 network etc...
            //typeOfRSTrees=1; //1 regresion, 0 classification, 2 network etc...
        }

     public void readSettings(File settings){

            BufferedReader reader;

            try {
                   Path path =Paths.get(settings.getAbsolutePath());
                   System.out.println("Path: "+settings.getAbsolutePath());
                   reader = Files.newBufferedReader(path,ENCODING);
                   String line = null;
                   int inputCount=0, numST=0, numNAC=0;
                   while ((line = reader.readLine()) != null) {
                        if(line.contains("JavaPath")){
                          String tmp[]=line.split("=");
                          tmp[1]=tmp[1].trim();
                          javaPath=tmp[1];
                        }
                        else if(line.contains("ClusPath")){
                          String tmp[]=line.split("=");
                          tmp[1]=tmp[1].trim();
                          clusPath=tmp[1];
                        }
                        else if(line.contains("OutputFolder")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           outFolderPath=tmp[1];
                        }
                        else if(line.contains("Input")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(inputCount<2)
                                viewInputPaths.set(inputCount,tmp[1]);
                           else{
                                viewInputPaths.add(tmp[1]);   
                           }
                           inputCount++;
                           System.out.println("Input: "+viewInputPaths);
                        }
                        else if(line.contains("MinSupport")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           minSupport=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("numIterations")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           numIterations=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("numNewAttr")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           numnewRAttr=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("numRetRed")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(tmp[1].toLowerCase().contentEquals("all"))
                                numRetRed=Integer.MAX_VALUE;
                           else if(tmp[1].toLowerCase().contentEquals("automatic"))
                                numRetRed=-1;
                           else
                                numRetRed=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("minimizeRules")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(tmp[1].contentEquals("yes"))
                                minimizeRules=true;
                           else minimizeRules=false;
                        }
                        else if(line.contains("unguidedExpansion")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(tmp[1].contentEquals("yes"))
                                unguidedExpansion=true;
                           else unguidedExpansion=false;
                        }
                        else if(line.contains("joiningProcedure")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(tmp[1].contentEquals("yes"))
                                useJoin=true;
                           else useJoin=false;
                        }
                        else if(line.contains("allowSERed")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           if(tmp[1].contentEquals("yes"))
                                allowSERed=true;
                           else allowSERed=false;
                        }
                        else if(line.contains("minJS")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           minJS=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("minAddRedJS")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           minAddRedJS=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("redesSetSizeType")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].toLowerCase().contains("exact"))
                                redesSetSizeType=1;
                            else redesSetSizeType=0;
                        }
                        else if(line.contains("maxPval")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           maxPval=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("numTrees")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            numTreesinForest=Integer.parseInt(tmp[1]);
                        }
                         else if(line.contains("jsType")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             if(tmp[1].toLowerCase().contains("pessimistic"))
                                 jsType=1;
                             else if(tmp[1].toLowerCase().contains("query non missing"))
                                 jsType=0;
                         }
                        else if(line.contains("numSupplementTrees")){
                                   String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            numSupplementTrees=Integer.parseInt(tmp[1]);
                                }
                        else if(line.contains("OutputFileName")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            outputName=tmp[1];
                        }
                        else if(line.contains("ATreeDepth")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("inf"))
                                aTreeDepth=Integer.MAX_VALUE;
                            else aTreeDepth=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("NumTarget")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            numTargets=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("clusteringMemory")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            clusteringMemory=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("JSImp")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            JSImpWeight=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("PValImp")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            PValImpWeight=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("AttDivImp")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            AttDivImpWeight=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("ElemDivImp")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            ElemDivImpWeight=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("RuleSizeImp")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            RuleSizeImpWeight=Double.parseDouble(tmp[1]);
                        }
                        else if(line.contains("MaxSupport")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("inf"))
                                maxSupport=Integer.MAX_VALUE;
                            else
                                maxSupport=Integer.parseInt(tmp[1]);
                        }
                         else if(line.contains("allowLeftNeg")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                leftNegation=true;
                            else
                                leftNegation=false;
                        }
                         else if(line.contains("allowRightNeg")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                rightNegation=true;
                            else
                                rightNegation=false;
                        }
                         else if(line.contains("allowLeftDisj")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                leftDisjunction=true;
                            else
                                leftDisjunction=false;
                        }
                         else if(line.contains("allowRightDisj")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                rightDisjunction=true;
                            else
                                rightDisjunction=false;
                        }
                         else if(line.contains("SideTrees")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             if(tmp[1].contains("regression")){
                                 if(numST<2)
                                     treeTypes.set(numST, 1);
                                 else treeTypes.add(1);
                             }
                             else{
                                 if(numST<2)
                                    treeTypes.set(numST, 0);
                                 else treeTypes.add(0);
                             }
                               numST++;  
                         }
                         else if(line.contains("useNetworkAC")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             if(numNAC<2){
                                 if(tmp[1].contains("true"))
                                    useNC.set(numNAC, true);
                                 else
                                    useNC.set(numNAC, false);
                             }
                             else{ 
                                 if(tmp[1].contains("true"))
                                     useNC.add(numNAC,true);
                                 else
                                      useNC.add(numNAC,false);
                             }
                             numNAC++;
                         }
                         else if(line.contains("computeDensity")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                 computeDensity=true;
                            else 
                                computeDensity=false;
                         }
                         else if(line.contains("graphSequence")){
                             String tmp[] = line.split("=");
                             tmp[1] = tmp[1].trim();
                             graphSequence = Integer.parseInt(tmp[1]);
                         }
                          else if(line.contains("preferenceFilePath")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           preferenceFilePath=tmp[1];
                        }
                         else if(line.contains("distanceFile")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             distanceFilePaths.add(tmp[1]);
                         }
                         else if(line.contains("spatialMatrix")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             spatialMatrix.add(tmp[1]);
                         }
                         else if(line.contains("spatialMeasure")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             spatialMeasures.add(tmp[1]);
                         }
                        else if(line.contains("Alpha")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             Alpha=Double.parseDouble(tmp[1]);
                         }
                        else if(line.contains("Bandwith")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            Bandwith=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("computeDistanceMatrixFromRules")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                 computeDMfromRules=true;
                            else 
                                computeDMfromRules=false;
                        }
                        else if(line.contains("maxDistance")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            maxDistance=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("useNetworkforInitialization")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                networkInit=true;
                        }
                        else if(line.contains("useNetworkAsBackground")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                useNetworkAsBackground=true;
                        }
                        else if(line.contains("useSplitTesting")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            if(tmp[1].contains("true"))
                                useSplitTesting=true;
                        }
                        else if(line.contains("initClusteringFileName")){
                          String tmp[]=line.split("=");
                          tmp[1]=tmp[1].trim();
                          initClusteringFileName=tmp[1];
                        }
                        else if(line.contains("percentageForTrain")){
                            String tmp[]=line.split("=");
                            tmp[1]=tmp[1].trim();
                            double perc= Double.parseDouble(tmp[1]);
                            if(perc>0.0 && perc<=1.0)
                                 percentageForTrain= perc;
                        }
                        else if(line.contains("importantAttributes")){
                            String tmp[]=line.split(":");
                            tmp[1]=tmp[1].trim();
                            String attrs[]=null;
                            
                            if(tmp[1].contains(","))
                                    attrs=tmp[1].split(",");
                            else if(tmp[1].contains(" "))
                                      attrs=tmp[1].split(" ");
                            else if(tmp[1].contains("\t"))
                                  attrs=tmp[1].split("\t");
                            else if(tmp[1].contains(";"))
                                attrs=tmp[1].split(";");
                            else{ 
                                attrs=new String[1];
                                attrs[0] = tmp[1];   
                            }
                            
                            for(int i=0;i<attrs.length;i++){
                                System.out.println("att: "+attrs[i].trim());
                                attrs[i]=attrs[i].trim();
                                importantAttributes.add(attrs[i]);
                            }
                        }
                        else if(line.contains("attributeImportanceW1")){//allowed options, none, hard, soft
                            String attributeImportanceTmp=line.split("=")[1].trim();
                            
                            if(!attributeImportanceTmp.equals("none") && !attributeImportanceTmp.equals("hard") && !attributeImportanceTmp.equals("soft"))
                                    attributeImportance=0;
                            else if(attributeImportanceTmp.equals("none"))
                                attributeImportance=0;
                            else if(attributeImportanceTmp.equals("soft"))
                                attributeImportance=1;
                            else if(attributeImportanceTmp.equals("hard"))
                                attributeImportance=2;
                           // attributeImportance+=1;
                            
                        }
                        else if(line.contains("attributeImportanceW2")){//allowed options, none, hard, soft
                            String attributeImportanceTmp=line.split("=")[1].trim();
                            
                            if(!attributeImportanceTmp.equals("none") && !attributeImportanceTmp.equals("hard") && !attributeImportanceTmp.equals("soft") && attributeImportance==0)
                                    attributeImportance=0;
                            else if(attributeImportanceTmp.equals("none") && attributeImportance==0)
                                attributeImportance=0;
                            else if(attributeImportanceTmp.equals("soft") && attributeImportance<2)
                                attributeImportance=1;
                            else if(attributeImportanceTmp.equals("hard"))
                                attributeImportance=2;
                            //attributeImportance+=2;
                            //use two variables, one for the first view and one for the second. Directly check the rules, not redescriptions
                        }
                        else if(line.contains("numRandomRestarts")){
                           String tmp[]=line.split("=");
                           tmp[1]=tmp[1].trim();
                           numRandomRestarts=Integer.parseInt(tmp[1]);
                        }
                        else if(line.contains("pathStrictness")){
                            String tmp[] = line.split("=");
                            tmp[1] = tmp[1].trim();
                            if(tmp[1].equals("true"))
                            pathStrict = true;
                        }
                        
                         /*else if(line.contains("leftSideTrees")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             if(tmp[1].contains("regression")){
                                 typeOfLSTrees=1;
                             }
                             else if(tmp[1].contains("classification")){
                                 typeOfLSTrees=0;
                             }
                         }
                        else if(line.contains("rightSideTrees")){
                             String tmp[]=line.split("=");
                             tmp[1]=tmp[1].trim();
                             if(tmp[1].contains("regression")){
                                 typeOfRSTrees=1;
                             }
                             else if(tmp[1].contains("classification")){
                                 typeOfRSTrees=0;
                             }
                         }*/
                    }
                   reader.close();
                }
             catch(IOException io){
                   io.printStackTrace();
             }
         }
        
        public void readPreference(){
            File input = new File(preferenceFilePath);
            System.out.println("Path: ");
            System.out.println(preferenceFilePath);
            
            BufferedReader reader;
            
            try{
                Path path =Paths.get(preferenceFilePath);
                reader = Files.newBufferedReader(path,ENCODING);
                String line="";
                int count=0;
                
                while ((line = reader.readLine()) != null) {
                    ++count;
                    String tmp[]=line.split(" ");
                    
                   if(count>1){ 
                    double tmpPref[]= new double[tmp.length];
                    
                    for(int i=0;i<tmp.length;i++)
                        tmpPref[i]=Double.parseDouble(tmp[i]);
                    
                    preferences.add(tmpPref);
                   }
                   else{
                       for(int i=0;i<tmp.length;i++)
                           preferenceHeader.put(tmp[i].trim(),i);
                   }
                    
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
            if(preferences.isEmpty()){
                System.out.println("Adding default preference parameters...");
                double tmp[]={0.17,0.16,0.17,0.17,0.16,0.17};
                
                if(useNetworkAsBackground == false){
                    for(int k=0;k<tmp.length-1;k++)
                        tmp[k] = 0.2;
                    tmp[tmp.length-1]=0;
                }
                
                preferences.add(tmp);
                if(!preferenceHeader.isEmpty())
                    preferenceHeader.clear();
                
                preferenceHeader.put("JSImp", 0);
                preferenceHeader.put("PValImp", 1);
                preferenceHeader.put("AttDivImp", 2);
                preferenceHeader.put("ElemDivImp", 3);
                preferenceHeader.put("ECoverage", 4);
                preferenceHeader.put("ACoverage", 5);
                preferenceHeader.put("NetImportance", 6);        
            }
            else if(preferenceHeader.keySet().size()<5 || this.useNetworkAsBackground==true && preferenceHeader.keySet().size()<6 ){
                    int num = preferenceHeader.keySet().size();
                    if(!preferenceHeader.keySet().contains("JSImp"))
                        preferenceHeader.put("JSImp", num++);
                    if(!preferenceHeader.keySet().contains("PValImp"))
                         preferenceHeader.put("PValImp", num++);
                    if(!preferenceHeader.keySet().contains("AttDivImp"))
                         preferenceHeader.put("AttDivImp", num++);
                    if(!preferenceHeader.keySet().contains("ElemDivImp"))
                         preferenceHeader.put("ElemDivImp", num++);
                    if(!preferenceHeader.keySet().contains("ECoverage"))
                         preferenceHeader.put("ECoverage", num++);
                    if(!preferenceHeader.keySet().contains("ACoverage"))
                        preferenceHeader.put("ACoverage", num++);
                    
                    if(this.useNetworkAsBackground==true)
                        preferenceHeader.put("NetImportance", num++);
                    
                   for(int i=0;i<preferences.size();i++){ 
                     
                       double row[] = preferences.get(i);
                       double newT[] = new double[num];
                       double sum= 0.0;
                       
                     for(int j=0;j<row.length;j++){
                         newT[j] = row[j];
                         sum+=row[j];
                     }
                     
                     double rem = 1.0 - sum;
                    
                     for(int j=row.length;j<num;j++)
                         newT[j] = rem/(num-j);
                     
                     preferences.set(i, newT);
                           }        
            }
        }
        
}
