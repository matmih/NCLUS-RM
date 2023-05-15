/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;

/**
 *
 * @author matej
 */
public class Conjunction {
    TIntHashSet elements=null;
    TIntHashSet attributes=null;
    TIntObjectHashMap<ArrayList<Double>> Rule=null;
    boolean isNegated=false;
    
    Conjunction(){
        elements=new TIntHashSet();
        attributes=new TIntHashSet();
        Rule=new TIntObjectHashMap<>();
        isNegated=false;
    }
    
    Conjunction(DataSetCreator dat){
        elements=new TIntHashSet(2*dat.numExamples);
    
       Rule=new TIntObjectHashMap();
       attributes=new TIntHashSet(dat.schema.getNbAttributes());
       isNegated=false;
    }
    
    Conjunction(Conjunction con){
       elements=new TIntHashSet(con.elements);
       attributes=new TIntHashSet(con.attributes);
       Rule=new TIntObjectHashMap(con.Rule);
       isNegated=con.isNegated;
    }
    
    Conjunction(String _rule, DataSetCreator dat, Mappings map){
        elements=new TIntHashSet(dat.numExamples);
        attributes=new TIntHashSet(dat.schema.getNbAttributes());
        Rule=new TIntObjectHashMap();
        String rs[]=_rule.split(" AND ");
         
        for(int i=0;i<rs.length;i++){
                if(rs[i].contains(">")){
                    String t[]=rs[i].split(" > ");

                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");
                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("<=")){
                     String t[]=rs[i].split(" <= ");
                     if(!attributes.contains(map.attId.get(t[0]))){
                        t[1]=t[1].replaceAll(",", ".");

                        attributes.add(map.attId.get(t[0]));
                    }
                }
                else if(rs[i].contains("=") && !rs[i].contains("<")){
                      String t[]=rs[i].split(" = ");
                       if(!attributes.contains(map.attId.get(t[0]))){
                           attributes.add(map.attId.get(t[0]));
                      }
                }
            }
        
        if(attributes.size()==0){
            System.out.println("Something is wrong in the Conjunction from Rule computation!");
            System.out.println("Rule :"+_rule);
        }
    }
    
    public void negate(){
        isNegated=true;
    }
    
    public void computeIntersection(Conjunction cJoin){
        TIntHashSet toRemove=new TIntHashSet();
        TIntIterator it=elements.iterator();
        
        while(it.hasNext()){
            int elem=it.next();
            if(!cJoin.elements.contains(elem))
                toRemove.add(elem);
        }
        
        it=toRemove.iterator();
        
        while(it.hasNext()){
            elements.remove(it.next());
        }
        
        toRemove.clear();   
    }
    
    public int equalElements(Conjunction c){
        int equal=1;
        
        TIntIterator it=elements.iterator();
        
        while(it.hasNext()){
            if(!c.elements.contains(it.next()))
                return 0;
        }
        
        return equal;
    }
    
    public int dominates(Conjunction c){

        if((isNegated==true && c.isNegated==false) || (isNegated==false && c.isNegated==true))
            return 0;
        
        if(isNegated==true && c.isNegated==true){
            return 0;
           /* TIntIterator it=attributes.iterator();
            while(it.hasNext()){
            if(!c.attributes.contains(it.next()))
                return 0;
        }
            if(attributes.size()<c.attributes.size())
                return 1;
            else return -1;*/
        }
        
        TIntIterator it=c.elements.iterator();
      
     if(elements.size()>=c.elements.size()){
        while(it.hasNext()){
            if(!elements.contains(it.next()))
                return 0;
        }
     }
     else{
         return -1;
     }
        
        return 1;
    }
    
}
