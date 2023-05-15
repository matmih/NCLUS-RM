/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Matej
 */
public class ComputeCommunityRedescriptionCoverage {
    public static void main(String args[]){
      //  File inputReds = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Community detection\\RedsSupportsAll0.1");
        File inputReds = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Community detection\\RedsSupportsAllRegJoin.txt");
        File inputCircles = new File("F:\\Matej Dokumenti\\Redescription mining with CLUS\\ExperimentiMrezni\\Community detection\\0.circles");
        
        BufferedReader read = null;
        
        HashMap<String, HashSet<String>> circ = new HashMap<>();
        
        ArrayList<HashSet<String>> reds = new ArrayList<>();
        HashSet<String> allInst = new HashSet<>();
        
        try{
            Path p = Paths.get(inputReds.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
            String line = "";
            
            while((line = read.readLine())!=null){
                String tmp[] = line.split(" ");
                HashSet<String> t = new HashSet<>();
                
                for(String s:tmp){
                    s = s.replaceAll("\"", "");
                    t.add(s);
                    allInst.add(s);
                }
                
                int found = 0;
                for(int j=0;j<reds.size();j++){
                    if(reds.get(j).size()!=t.size()) continue;
                    else{
                        int same = 1;
                        for(String s:t){
                            if(!reds.get(j).contains(s))
                                same = 0;
                            break;
                        }
                        if(same == 1) found = 1;
                    }
                }
                
                if(found == 0)
                    reds.add(t);
            }
            
            read.close();
            
            p = Paths.get(inputCircles.getAbsolutePath());
            read = Files.newBufferedReader(p, StandardCharsets.UTF_8);
            
            
             while((line = read.readLine())!=null){
                String tmp[] = line.split("\t");
                HashSet<String> t = new HashSet<>();
                circ.put(tmp[0], new HashSet<String>());
                
                for(int i=1;i<tmp.length;i++){
                    circ.get(tmp[0]).add(tmp[i]);
                }
            }
            
            read.close();
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        
        System.out.println("Described instances: "+allInst.size());
        
        HashMap<String,ArrayList<HashSet<String>>> circRed = new HashMap<>();
        
        for(int i=0;i<reds.size();i++){
            System.out.println(i+" "+"Red size: "+reds.get(i).size());
            Iterator<String> it = circ.keySet().iterator();
            
            while(it.hasNext()){
                String c = it.next();
                HashSet<String> inst = circ.get(c);
                
                HashSet<String> inter = new HashSet<String>();
                
                for(String s:reds.get(i)){
                if(inst.contains(s))
                    inter.add(s);
            }
                   System.out.println(c+" "+((double)inter.size()/reds.get(i).size()));
                   double p = (double)inter.size()/reds.get(i).size();
                   if(p>=0.6){
                       if(!circRed.containsKey(c)){
                           circRed.put(c, new ArrayList<>());
                           circRed.get(c).add(reds.get(i));
                       }
                       else
                           circRed.get(c).add(reds.get(i));
                   }
            }
            System.out.println();
            
            
        }
        
        
        Iterator<String> it = circRed.keySet().iterator();
        
        while(it.hasNext()){
            String c = it.next();
            ArrayList<HashSet<String>> rs = circRed.get(c);
            System.out.println("All red: "+reds.size());
            System.out.println("Num red: "+rs.size());
            HashSet<String> all = new HashSet<>();
            for(HashSet<String> ts:rs)
                all.addAll(ts);
            
            HashSet<String> inst = circ.get(c);
            HashSet<String> inter = new HashSet<>();
            
            for(String s:all){
                if(inst.contains(s))
                    inter.add(s);
            }
            
            System.out.println(c+" Acc: "+((double)inter.size()/all.size()));
            System.out.println("perc: "+((double)inter.size()/(inst.size())));
            System.out.println(c+" JS: "+((double)inter.size()/(inst.size()+all.size()-inter.size())));
            
        }
        
        
        
    }
}
