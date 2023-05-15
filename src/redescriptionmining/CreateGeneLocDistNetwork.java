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

/**
 *
 * @author matej
 */
public class CreateGeneLocDistNetwork {
    public static void main(String [] args){
        
        File input = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\biology\\DistancekNNLog.arff");
        HashMap<String,Integer> ogIndex = new HashMap<>();
        HashMap<Integer,String> indexOg = new HashMap<>();
        
        try{
            FileWriter fw = new FileWriter("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\biology\\distancesBio.csv");
            Path p = Paths.get(input.getAbsolutePath());
            BufferedReader read = Files.newBufferedReader(p,StandardCharsets.UTF_8);
            
            String line = "";
            
            int dataSection = 0, count = 0;
            
            while((line = read.readLine())!=null){
                
                 if(dataSection == 0 && !line.toLowerCase().contains("@attribute") && !line.toLowerCase().contains("@data"))
                     continue;
                 else if(dataSection == 0 && line.toLowerCase().contains("@attribute")){
                     line = line.replaceAll("( )+", " ");
                     //System.out.println(line);
                     if(line.trim().contains("ID") || line.trim().contains("index") || line.contains("class hierarchical"))
                         continue;
                     String tmp[] = line.split(" ");
                     ogIndex.put(tmp[1].replace("DT", "").trim(),count);
                     indexOg.put(count++,tmp[1].replace("DT","").trim());
                     //System.out.println(tmp[1].replace("DT", "").trim());
                     //System.out.println("loading index");
                 }
                 else if(dataSection == 0 && line.toLowerCase().contains("@data")){
                     dataSection = 1;
                     //System.out.println("Data section");
                     continue;
                 }
                 
                 if(dataSection == 1){
                     String tmp[] = line.split(",");
                     //System.out.println(tmp[0].replaceAll("\"", "").trim());
                     int id1 = ogIndex.get(tmp[0].replaceAll("\"", "").trim());
                     System.out.println(tmp[0].replaceAll("\"", "").trim()+" "+id1);
                     //System.out.println("tmp len: "+tmp.length);
                     for(int i=2;i<tmp.length-1;i++){
                         int id2 = i-2;
                         if(!tmp[i].contains("Infinity"))
                                 fw.write(id1+","+id2+","+Double.parseDouble(tmp[i])+"\n");
                         else continue;
                     }
                     
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
