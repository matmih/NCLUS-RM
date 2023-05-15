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

/**
 *
 * @author Matej
 */
public class ArffToSparseReReMi {
    public static void main(String args[]){
          File input = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\0view2.arff");
        File output = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\input2Node0.csv");
       // ArrayList<String> attributes;
        
        try{
          
            BufferedReader read;
            FileWriter write;
            
            Path p = Paths.get(input.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            write = new FileWriter(output.getAbsoluteFile());
            
            String line = "";
            int data = 0;
            int firstAt = 1;
            int count = 1, count1 = 1; 
            
            while((line = read.readLine())!=null){
                if(!line.toLowerCase().contains("@attribute")&& data == 0 && !line.toLowerCase().contains("@data"))
                    continue;
                 else if(line.toLowerCase().contains("@attribute")){
                     if(firstAt == 1){
                         firstAt = 0;
                         write.write("ID;AID;value\n");
                         continue;
                     }
                     String at[] = line.split(" ");
                    write.write("-1;"+count+";"+at[1].trim()+"\n");
                    count++;
                 }
                 else if(data == 1){
                    String tmp[] = line.split(",");
                    tmp[0] = tmp[0].replaceAll("\"", "");
                    write.write(count1+";"+"-1"+";"+tmp[0]+"\n");
                    count1++;
                }
                if(line.toLowerCase().contains("@data")){
                    data = 1;
                    continue;
                }
            }
            read.close();
            
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            data = 0;
            count1 = 1;
            while((line = read.readLine())!=null){
                if(line.toLowerCase().contains("@attribute") && data == 0 && !line.toLowerCase().contains("@data"))
                    continue;
                 else if(data == 1){
                   String tmp[] = line.split(",");
                    for(int i=1;i<tmp.length;i++)
                        if(tmp[i].trim().equals("1")){
                            write.write(count1+";"+i+";"+"1"+"\n");
                        }
                    count1++;
                }
                 else if(line.toLowerCase().contains("@data")){
                    data = 1;
                    continue;
                }
            }
            read.close();
            write.close();
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
