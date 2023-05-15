/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author matej
 */
public class JoinDistAndRed {
    public static void main(String args[]){
        
        File redescriptionInput=new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\Clinical dataset redescriptions\\TargetedADRedescriptions\\redescriptionsConstrainedADFirstPAPP-AReduced1.rr");
        File distributionFile=new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\Clinical dataset redescriptions\\TargetedADRedescriptions\\redescriptionsConstrainedADFirstPAPP-AReduced1Dist.txt");
       

        HashMap<Integer,String> distributionMapping=new HashMap<>();
        
            try (BufferedReader bufRdr1 = new BufferedReader(new FileReader(distributionFile)))
        {
            String line;
            String label="";
            int count=0, useJSPval=1;
            while ((line = bufRdr1.readLine()) != null)
            {
                
                line = line.trim();
                 count++;
                distributionMapping.put(count, line);   
            }
            bufRdr1.close();
        }
       catch(Exception e){
           e.printStackTrace();
       }
        
        ArrayList<String> lines=new ArrayList<>();
        ArrayList<Double> JSlist=new ArrayList<>();
         try (BufferedReader bufRdr1 = new BufferedReader(new FileReader(redescriptionInput)))
        {
            String line;
            String label="";
            int count=0,found=0, useJSPval=1;
            while ((line = bufRdr1.readLine()) != null)
            {
                
                lines.add(line+"\n");
                
                if(line.contains("Redescriptions")){
                    lines.add("[CN, SMC, EMCI, LMCI, AD]\n");
                }
                
                if(found==1){
                    lines.add("Dist: "+distributionMapping.get(count)+"\n");
                    found=0;
                }
                
                line = line.trim();
                if(line.contains("Rules")){
                    found=1;
                    count++;
                }
             
            }
            bufRdr1.close();
        }
       catch(Exception e){
           e.printStackTrace();
       }
         
         
         File output=new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\Clinical dataset redescriptions\\TargetedADRedescriptions\\RedsWithDistTargeted.txt");
         
         try
            {
                FileWriter fw = new FileWriter(output);
                
                for(int j=0;j<lines.size();j++){
                    if(j+1<lines.size())
                        fw.write(lines.get(j));
                    else
                        fw.write(lines.get(j));
                }
                   
                fw.close();
           
            }
           catch(Exception e){
                e.printStackTrace();
            }
    }
}
