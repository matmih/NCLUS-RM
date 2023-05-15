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

/**
 *
 * @author Matej
 */
public class ArffToDenseReReMi {
    public static void main(String args[]){
        File input = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Exp1\\0View2.arff");
        File output = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\ReReMiData\\input2Facebook0.csv");
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
            
            while((line = read.readLine())!=null){
                if(!line.toLowerCase().contains("@attribute") && data == 0 && !line.toLowerCase().contains("@data"))
                    continue;
                else if(line.toLowerCase().contains("@attribute")){
                    if(firstAt == 0){
                        write.write(";");
                    }
                    String at[] = line.split(" ");
                    write.write(at[1].trim());
                    firstAt = 0;
                }
                 else if(data == 1){
                     write.write("\n");
                    line = line.replaceAll(",", ";");
                    write.write(line);
                }
                if(line.toLowerCase().contains("@data")){
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
