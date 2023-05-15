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
 * @author matej
 */
public class CreateBioViews {
    public static void main(String args[]){
        File view2 = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\biology\\BaselineOrganism1k=4.arff");
        File view1 = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\biology\\DistancekNNLog.arff");
        HashSet<String> ogsV1 = new HashSet<>();
        ArrayList<String> ogOrder = new ArrayList<>();
        HashSet<String> ogsV2 = new HashSet<>();
        
        ArrayList<String> attributes = new ArrayList<>();
        HashMap<String, ArrayList<Double>> data = new HashMap<>();
        HashMap<String,String> targets = new HashMap<>();
        
        File output = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\NetworkRM\\biology\\nfp.arff");
        
        try{
            Path p = Paths.get(view1.getAbsolutePath());
            BufferedReader read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
            String line = "";
            int dataSection = 0;
            
            while((line = read.readLine())!=null){

                if(!line.toLowerCase().contains("@data") && dataSection == 0)
                    continue;
                else if(line.toLowerCase().contains("@data")){
                    dataSection = 1;
                    continue;
                }
                if(dataSection == 1){
                    String tmp[] = line.split(",");
                    ogsV1.add(tmp[0].trim());
                    ogOrder.add(tmp[0].trim());
                }
            }
            
            read.close();
            
            
             p = Paths.get(view2.getAbsolutePath());
             read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
              line = "";
              dataSection = 0;
            
            while((line = read.readLine())!=null){
                if(!line.toLowerCase().contains("@data") && dataSection == 0)
                    continue;
                else if(line.toLowerCase().contains("@data")){
                    dataSection = 1;
                    continue;
                }
                if(dataSection == 1){
                    String tmp[] = line.split(",");
                    String og = tmp[0].trim();
                    ogsV2.add(og);
                    data.put(og, new ArrayList<>());
                    targets.put(og, tmp[tmp.length-1].trim());
                    
                    for(int i=1;i<tmp.length-1;i++){
                        if(!tmp[i].equals("?"))
                          data.get(og).add(Double.parseDouble(tmp[i]));
                        else data.get(og).add(Double.POSITIVE_INFINITY);
                    }
                    
                }
            }
            
            read.close();
            
             p = Paths.get(view2.getAbsolutePath());
             read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
              line = "";
              dataSection = 0;
              
              FileWriter fw = new FileWriter(output);
            
            while((line = read.readLine())!=null){
                if(!line.toLowerCase().contains("@data") && dataSection == 0){
                    fw.write(line+"\n");
                    continue;
                }
                else if(line.toLowerCase().contains("@data")){
                    fw.write(line+"\n");
                    dataSection = 1;
                    continue;
                }
                if(dataSection == 1){
                   break;
                }
            }
            
             String og="";
                   for(int i=0;i<ogOrder.size();i++){
                       og = ogOrder.get(i);
                   
                  fw.write(og+",");
                  ArrayList<Double> tmp = data.get(og);
                  
                    for(int j=0;j<tmp.size();j++){
                        if(tmp.get(j).isInfinite() && (j+1)<tmp.size())
                            fw.write("?,");
                        else fw.write(tmp.get(j)+",");
                    }
                    
                    fw.write(targets.get(og)+"\n");
                   }  
            
            read.close(); fw.close();
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        System.out.println("Sizes: "+ogsV1.size()+" "+ogsV2.size());
        
        for(String og:ogsV1){
            if(!ogsV2.contains(og)){
                System.out.println("Not contained V2"+og);
            }
        }
        System.out.println("\n");
        
        
        for(String og:ogsV2){
            if(!ogsV1.contains(og)){
                System.out.println("Not contained V1"+og);
            }
        }
    }
}
