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
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Matej
 */
public class FilterBioData {
    public static void main(String args[]){
        File skup1 = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\nfpShort.arff");
        File skup2 = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\PhyleticProfile.arff");
        File mreza = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\distances.csv");
       ArrayList<Par> lista = new ArrayList<>();
       HashSet<String> retainedOgs = new HashSet<>();
       
       HashMap<Integer,Integer> oldIndexToNewIndex = new HashMap<>();
       
       File output1 = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\nfpShortFiltered.arff");
       File output2 = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\PhyleticProfileFiltered.arff");
       File mrezaOut = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\distancesBioFiltered.csv");
       
        BufferedReader read;
        
        try{
            Path p = Paths.get(skup2.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
            String line = "";
            int data = 0;
            while((line = read.readLine())!=null){
                if(data == 0){
                    if(line.contains("@DATA")){
                        data = 1; continue;
                    }
                    else continue;
                }
                if(data == 1){
                    String tmp[] = line.split(",");
                    int count = 0;
                    for(int i=1;i<tmp.length;i++)
                        count+=Integer.parseInt(tmp[i].replaceAll(" ", ""));
                    lista.add(new Par(tmp[0].trim(),count));
                }
            }
            
            read.close();
            
            lista.sort(new Komparator());
            
            for(int i=0;i<lista.size();i++)
                System.out.println(lista.get(i));
            
            for(int i=0;i<500;i++)
                retainedOgs.add(lista.get(i).og);
            
            p = Paths.get(skup1.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
           line = "";
             data = 0;
             int indOld = -1;
             int indNew = -1;
             FileWriter fw = new FileWriter(output1.getAbsolutePath());
            while((line = read.readLine())!=null){
                if(data == 0){
                    fw.write(line+"\n");
                    if(line.toUpperCase().contains("@DATA")){
                        data = 1; continue;
                    }
                    else continue;
                }
                if(data == 1){
                    ++indOld;
                    String tmp[] = line.split(",");
                    String og = tmp[0].trim();
                     System.out.println(og+ " "+retainedOgs.size());
                    if(retainedOgs.contains(og)){
                        oldIndexToNewIndex.put(indOld, ++indNew);
                        fw.write(line+"\n");
                    }
                }
            }
            
            read.close();
            fw.close();
            
             p = Paths.get(skup2.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
           line = "";
             data = 0;
              fw = new FileWriter(output2.getAbsolutePath());
            while((line = read.readLine())!=null){
                if(data == 0){
                    fw.write(line+"\n");
                    if(line.contains("@DATA")){
                        data = 1; continue;
                    }
                    else continue;
                }
                if(data == 1){
                    String tmp[] = line.split(",");
                    String og = tmp[0].trim();
                   
                    if(retainedOgs.contains(og)){
                        fw.write(line+"\n");
                    }
                }
            }
            
            read.close();
            fw.close();
            
             p = Paths.get(mreza.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
             line = "";
              fw = new FileWriter(mrezaOut.getAbsolutePath());
                while((line = read.readLine())!=null){
                    String tmp[] = line.split(",");
                    int id1 = Integer.parseInt(tmp[0].replaceAll(" ", ""));
                    int id2 = Integer.parseInt(tmp[2].replaceAll(" ", ""));
                   
                    if(oldIndexToNewIndex.containsKey(id1) && oldIndexToNewIndex.containsKey(id2)){
                         int id1new = oldIndexToNewIndex.get(id1);
                         int id2new = oldIndexToNewIndex.get(id2);
                         String lineNew = id1new+","+tmp[1]+","+id2new+","+tmp[3]+","+tmp[4];
                        fw.write(lineNew+"\n");
                    }
                }
                
            read.close();
            fw.close();
             
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        
    }
}
