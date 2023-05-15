/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import java.util.Collection;
import java.util.Iterator;
/**
 *
 * @author matej
 */
public class ProbaGraf {
    public static void main(String [] args){
        Graph<Integer,String> g=new DirectedSparseMultigraph<Integer,String>();
        g.addVertex(1); g.addVertex(2); g.addVertex(3);
        
        g.addEdge(1 + "-to-" + 2, 1, 2);
        g.addEdge(2+"-to-"+3, 2,3);
        
        if(g.getEdges().contains(2+"-to-"+3)){
            System.out.println("Sadrzi");
        }
        
        Collection<Integer> neighbs = g.getIncidentVertices(1 + "-to-" + 2);
			for (Iterator<Integer> iterator = neighbs.iterator(); iterator.hasNext();)
                            System.out.println(iterator.next()+" ");
                        
                        neighbs = g.getIncidentVertices(2 + "-to-" + 3);
			for (Iterator<Integer> iterator = neighbs.iterator(); iterator.hasNext();)
                            System.out.println(iterator.next()+" ");
                        
                        
                        
                        
    }
}
