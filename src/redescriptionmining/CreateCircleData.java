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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author matej
 */
public class CreateCircleData {
    public static void main(String args[]){
        
        String folderPath = "C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\facebook\\facebook\\";
        String inputFileString = "", outputFileString = "";
        File inputFilePath;
        int egos[] = {0,107,348,414,686,698,1684,1912,3437,3980};
        HashMap<Integer,Integer> edges = null;
        
        for(int i=0;i<egos.length;i++){
            
            //get the edges
            
            inputFileString = folderPath+egos[i]+".edges";
            outputFileString = folderPath+"distances"+egos[i]+".csv";
            inputFilePath = new File(inputFileString);
            
            try{
                edges = new HashMap<>();
                Path p = Paths.get(inputFilePath.getAbsolutePath());
                BufferedReader read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
                
                String line = "";
                String tmp[];
                
                 FileWriter fw = new FileWriter(outputFileString);
                
                while((line = read.readLine())!=null){
                    tmp = line.split(" ");
                    
                     fw.write(tmp[0].trim()+","+0+","+tmp[1].trim()+","+0+"\n");
                     
                }
                read.close();
                
                //write the edges
               
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
 
            //divide into views
            inputFileString = folderPath+egos[i]+".featnames";
            HashSet<Integer> fwIndex = new HashSet<>();
            HashSet<Integer> swIndex = new HashSet<>();
            
            inputFilePath = new File(inputFileString);
            int count = 0;
            
            try{
                Path p = Paths.get(inputFilePath.getAbsolutePath());
                BufferedReader read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
                
                String line = "";
                
                while((line = read.readLine())!=null){
                    
                    if(line.contains("work;"))
                        swIndex.add(count);
                    else fwIndex.add(count);
                    count++;

                }
                read.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
            //read data and write views
             inputFileString = folderPath+egos[i]+".feat";
            
            inputFilePath = new File(inputFileString);
            
            //view files
            String inputView1 = folderPath+egos[i]+"view1.arff";
            String inputView2 = folderPath+egos[i]+"view2.arff";
            
            File inv1 = new File(inputView1);
            File inv2 = new File(inputView2);
            
            try{
                Path p = Paths.get(inputFilePath.getAbsolutePath());
                BufferedReader read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
                
                FileWriter fw1 = new FileWriter(inv1.getAbsoluteFile());
                FileWriter fw2 = new FileWriter(inv2.getAbsoluteFile());
                
                fw1.write("relation attrs1\n\n");
                fw1.write("@attribute Uid string\n");
                fw2.write("relation attrs2\n\n");
                fw2.write("@attribute Uid string\n");
                //write headers
                
                for(int j=0;j<count;j++){
                    if(fwIndex.contains(j))
                        fw1.write("@attribute feat"+j+" numeric\n");
                    else
                         fw2.write("@attribute feat"+j+" numeric\n");
                }
                
                fw1.write("\n");
                fw1.write("@data\n");
                fw2.write("\n");
                fw2.write("@data\n");

                String line = "";
                String tmp[];
                
                int firstW1 = 1, firstW2 = 1;

                while((line = read.readLine())!=null){
                        tmp = line.split(" ");
                        
                        for(int j=0;j<tmp.length;j++){
                            if(j==0){
                                fw1.write(tmp[j]);
                                fw2.write(tmp[j]);
                            }
                            
                             if(fwIndex.contains(j-1)){
                                        fw1.write(","+tmp[j]);
                             }
                             else if(swIndex.contains(j-1)){  
                                   fw2.write(","+tmp[j]);
                              }
                            }
                        
                             fw1.write("\n"); fw2.write("\n");
                        
                        }
                read.close();
                fw1.close();
                fw2.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
        }
            
        
        //iterate over all files
        //use .edges to create a network
        //use .featNames to divide to views (everything\work -> view1, work -> view2)
        //use .feat to load features and create corresponding datasets
        
    }
}
