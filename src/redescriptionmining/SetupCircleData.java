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
 * @author Matej
 */
public class SetupCircleData {
    
    public static void main(String args[]){
        
        String path = "C:\\Users\\Matej\\Downloads\\Facebook\\Facebook\\";
        File inputView = null;
        File inputDistance = null;
        String output = "distancesMod";
        FileWriter fw = null;
        BufferedReader reader = null;
        Path p = null;
        int ext[] = {0,107,348,414,686,698,1684,1912,3437,3980};
        
        HashMap<Integer,Integer> map = null;
        
      try{
       for(int i:ext){
           map = new HashMap<>();
           inputView = new File(path+i+"view1.arff");
           inputDistance = new File(path+"distances"+i+".csv");
           
           p = Paths.get(inputView.getAbsolutePath());
           reader = Files.newBufferedReader(p,StandardCharsets.UTF_8);
           
           int count = 0;
           String line = "";
           int data = 0;
           while((line = reader.readLine())!=null){
               if(line.toLowerCase().contains("@data")){
                   data = 1;
                   continue;
               }
               
               if(data == 1){
                   String tmp[] = line.trim().split(",");
                   map.put(Integer.parseInt(tmp[0]), count);
                   count++;
               }
               
           }
           
           reader.close();
           
           p = Paths.get(inputDistance.getAbsolutePath());
           reader = Files.newBufferedReader(p,StandardCharsets.UTF_8);
           fw = new FileWriter(path+output+i+".csv");
           
            line = "";
           while((line = reader.readLine())!=null){
               String tmp[] = line.split(",");
               String l = map.get(Integer.parseInt(tmp[0]))+",0,"+map.get(Integer.parseInt(tmp[2]))+",0";
               fw.write(l+"\n");
           }
           
           reader.close(); fw.close();
           
       }
      }
      catch(IOException e){
          e.printStackTrace();
      }
        
        
    }
    
}
