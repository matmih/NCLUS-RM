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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author matej
 */
public class ExtractDensity {
    public static void main(String [] args){
        
        File input = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\redescriptionsGuidedExperimentalIterativeCountryNetworkTestingSpatialNoNetwork.rr1.rr");
        File output = new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\densityDist.txt");
        
         BufferedReader reader1;
             ArrayList<Double> vals = new ArrayList<>();
             
              try {
                     Path path =Paths.get(input.getAbsolutePath());
                     reader1 = Files.newBufferedReader(path);
                      String line = null;
                     
                      while ((line = reader1.readLine()) != null) {
                          if(line.contains("av. density : ")){
                              String tmp[] = line.split(" : ");
                              vals.add(Double.parseDouble(tmp[1].trim()));
                          }

                    }
         //System.out.println("Num pids: "+proteinsEggnog.size());
      reader1.close();
       }
         catch(Exception e){e.printStackTrace();}
        
              try{
           FileWriter fw = new FileWriter(output.getAbsolutePath());     
                    for(int i=0;i<vals.size();i++)
                        fw.write(vals.get(i)+"\n");
           
                    fw.close();
              }
              catch(IOException e){
                  e.printStackTrace();
              }
              
    }
}
