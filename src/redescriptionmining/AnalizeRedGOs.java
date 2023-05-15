/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Matej
 */
public class AnalizeRedGOs {
    public static void main(String args[]){
        File targets = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\EksperimentiFinal\\Bio\\Targets.txt");
        HashMap<String,ArrayList<String>> ogGOMap = new HashMap<>();
        
        BufferedReader read = null;
        String line = "";
        Path p = Paths.get(targets.getAbsolutePath());
        
        
        try{
             read = Files.newBufferedReader(p);
             while((line = read.readLine())!=null){
                    String tmp[] = line.split(",");
                    String go[] = tmp[1].split("@");
                    String cog = tmp[0].trim().replaceAll("\"", "");
                    ogGOMap.put(cog, new ArrayList<>());
                    for(String g:go)
                        ogGOMap.get(cog).add(g.trim());
             }
             
             read.close();
        
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        String ogs[]={"COG0093", "COG0090", "COG0087", "COG0088", "COG0197" }; 

        
        
        ArrayList<ParGOFreq> l = new ArrayList<>();
        System.out.println(ogGOMap.keySet().size());
        Iterator<String> it = ogGOMap.keySet().iterator();
        
        while(it.hasNext()){
            String c = it.next();
           ArrayList<String> g = ogGOMap.get(c);
            System.out.println(c+" "+g.size());
        }
        
        for(String s:ogs){
            ArrayList<String> gos = ogGOMap.get(s.trim());
            System.out.println("s: "+s);
            if(l.size()==0){
                for(String g:gos)
                     l.add(new ParGOFreq(g.trim(),1.0));
            }
            else{
                for(String g:gos){
                    int found = 0;
                     for(ParGOFreq p1:l){
                         if(p1.GO.trim().equals(g.trim())){
                                 p1.freq = p1.freq+1;
                                 found = 1;
                                 break;
                           }
                         }
                     if(found == 0){
                          l.add(new ParGOFreq(g.trim(),1.0));
                     }
                }
            }
        }
        
        for(ParGOFreq p1:l)
            p1.freq = p1.freq/ogs.length;
        
        
        l.sort(new ParGOFreqKomparator());
        
        for(ParGOFreq p1:l)
            System.out.println(p1.GO+" "+p1.freq);
        
    }
}
