/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
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
public class ExtractRSuppsN {
    public static void main(String args[]){
        String inputPath = "F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\EksperimentiFinal\\Circles\\Node0OK\\Connectivity\\";
        String filePref = "redescriptionsGuidedExperimentalIterativeFacebook0NoNetworkTestingNew";
        String fileSuff = "NoNetConnectivityAlpha=1.0Join.rr";
        
        String output = "F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Community detection\\RedsSupportsAllRegJoin.txt";
        
        BufferedReader read = null;
        Path p=null;
        
      try{
          FileWriter fw = new FileWriter(output);
          
        for(int i=1;i<=10;i++){
            String input = inputPath+filePref+i+fileSuff;
            
            p = Paths.get(input);
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            String line = "";
            
            int supp = 0;
            while((line = read.readLine())!=null){
                if(line.contains("Covered examples (intersection):")){
                    supp = 1;
                    continue;
                }
                
                if(supp == 1){
                    fw.write(line+"\n");
                    supp = 0;
                }
            }
            read.close();
           
        }
         fw.close();
      }
      catch(IOException e){
          e.printStackTrace();
      }
    }
}
