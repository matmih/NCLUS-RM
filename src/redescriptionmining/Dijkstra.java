/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
 
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.util.HashMap;
import org.javatuples.Triplet;

/**
 *
 * @author matej
 */
public class Dijkstra {//this only works for undirected graphs
    double maxDist = -1.0;
    private Graph<Vertex, String> g;
	private Set<List<Vertex>> allShortestPaths;
 
	public Dijkstra(Graph<Vertex, String> g) {
		this.g = g;
	}
 
	private Vertex getSourceFromId(Integer sourceId) {
		Collection<Vertex> vertices = g.getVertices();
		for (Iterator<Vertex> iterator = vertices.iterator(); iterator
				.hasNext();) {
			Vertex vertex = (Vertex) iterator.next();
			if (vertex.getId() == sourceId)
				return vertex;
		}
		return null;
	}
        
        
        void constructPaths(Vertex source, Vertex v, ArrayList<Vertex> curr, ArrayList<ArrayList<Vertex>> allPaths){
            if(v == source){
                ArrayList<Vertex> newcurr = new ArrayList<>(curr);
                newcurr.add(source);
                allPaths.add(newcurr);
                System.out.println(source.getId()+" "+v.getId()+" "+allPaths.size());
                return;
            }
            
            for(int i=0;i<v.getPrev().size();i++){
                Vertex u  = v.getPrev().get(i);
                if(curr.contains(u))
                    continue;
                ArrayList<Vertex> newcurr = new ArrayList<>(curr);
                newcurr.add(0, u);
               /* System.out.println("Prev path: ");
                for(int ii=0;ii<curr.size();ii++)
                    System.out.print(curr.get(ii).getId()+" ");
                //System.out.println();
                System.out.println("Curr path: ");
                for(int ii=0;ii<newcurr.size();ii++)
                    System.out.print(newcurr.get(ii).getId()+" ");
                System.out.println();*/
                constructPaths(source,u,newcurr,allPaths);
            }
            
        }
 
	/**
	 * Computes all shortest paths to all the vertices in the graph using the
	 * Dijkstra's shortest path algorithm.
	 * 
	 * @param sourceId
	 *            : Starting node from which to find the shortest paths.
	 */
	public void computeAllShortestPaths(Integer sourceId, NHMCDistanceMatrix mat){//also compute the case in which entities need to be close in all layers 
            //implementation as multigraph -> combination of edges from multiple layers (the important thing is that they are close)
            //finding shortest paths on each layer and averaging -> must be close in all layers (to implement)
            //two for-loops for each layer (special case -> graphs sequence)
            //create graph and compute all shortest paths, save in array for all pairs of layers
            //output sorted by minimal distance + number of layers in which connected
            //for total score, count disconnected nodes as having maximum overall distance (weight = 100)
            
		Vertex source = getSourceFromId(sourceId);
		source.sourceDistance = 0;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);
		List<Vertex> prev = null;
                 Collection<String> edges = g.getEdges();
                 String e = "";
 
		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();
                        double weight = 1.0;//add different weights
                       
			Collection<Vertex> neighbs = g.getNeighbors(u);

			for (Iterator<Vertex> iterator = neighbs.iterator(); iterator
					.hasNext();) {
				Vertex nv = (Vertex) iterator.next();
                                if(mat.inputType == 0){
                                    e = u.getId()+"-to-"+nv.getId();
                                    if(!edges.contains(e))
                                        continue;
                                    if(weight>mat.m_distancesS.get(u.getId()+"#"+nv.getId()))
                                        weight = mat.m_distancesS.get(u.getId()+"#"+nv.getId());
                                        
                                }
                                else{
                                    // g.addEdge(e1+"_"+l1+"to"+el2+"_"+l2, e1,el2);
                                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> t=mat.connectivityMultiplex.get(u.getId());
                                    if(t == null)
                                        continue;
                                    Iterator<Integer> tint = t.keySet().iterator();
                                    int lay = 0, contained = 0;
                                     weight = 1.0;
                                    
                                    while(tint.hasNext()){
                                        lay = tint.next();
                                        
                                        
                                        HashMap<Integer,HashMap<Integer,Double>> lay2I = mat.optim.get(u.getId()).get(lay);
                                        
                                        Iterator<Integer> it1 = lay2I.keySet().iterator();
                                        
                                        while(it1.hasNext()){
                                            int lay2 = it1.next();
                                            if(lay2I.get(lay2).containsKey(nv.getId()) && edges.contains(u.getId()+"_"+lay+"-to-"+nv.getId()+"_"+lay2)){
                                                contained = 1;
                                                if(weight>(1.0-lay2I.get(lay2).get(nv.getId())))
                                                    weight = (1.0-lay2I.get(lay2).get(nv.getId()));
                                            }
                                        }
                                        
                                        /*ArrayList<Triplet<Integer,Integer,Double>> n = t.get(lay);
                                        
                                        for(int z = 0;z<n.size();z++)
                                            if(n.get(z).getValue0() == nv.getId() && edges.contains(u.getId()+"_"+lay+"-to-"+nv.getId()+"_"+n.get(z).getValue1())){                 
                                                contained = 1;
                                               
                                                if(weight>(1.0-n.get(z).getValue2()))
                                                    weight = (1.0-n.get(z).getValue2()); */
                                                 //if((nv.getId() == 1 || nv.getId() == 0) && sourceId == 4)
                                                //    System.out.println("Weight to 1: "+weight+" "+n.get(z).getValue2()+" "+(u.getId()+"_"+lay+"-to-"+nv.getId()+"_"+n.get(z).getValue1()));
                                           // }
                                    }
                                    if(contained == 0)
                                        continue;
                                }
                                
				prev = nv.getPrev();
				
				double distanceThroughU = u.sourceDistance + weight;

				if (distanceThroughU < nv.sourceDistance) {
					vertexQueue.remove(nv);
					nv.sourceDistance = distanceThroughU;
                                        if(distanceThroughU>maxDist)
                                            maxDist = distanceThroughU;
					nv.setPrevious(u);
					vertexQueue.add(nv);
					prev = new ArrayList<Vertex>();
                                        //nv.allShortestPathsNode.clear();
					prev.add(u);
					nv.setPrev(prev);
                                        
                                       /* ArrayList<ArrayList<Vertex>> paths = new ArrayList<>();
                                        ArrayList<Vertex> cur = new ArrayList<>();
                                        cur.add(nv);
                                        constructPaths(source,nv,cur, paths);
                                           
                                        nv.allShortestPathsNode.addAll(paths);*/
				} else if (distanceThroughU == nv.sourceDistance) {
                                     //ArrayList<ArrayList<Vertex>> paths = new ArrayList<>();
                                       // ArrayList<Vertex> cur = new ArrayList<>();
                                       // cur.add(nv);
                                        //constructPaths(source,nv,cur, paths);
                                           
                                       // nv.allShortestPathsNode.addAll(paths);
					if (prev != null)
						prev.add(u);
					else {
						prev = new ArrayList<Vertex>();
						prev.add(u);
						nv.setPrev(prev);
					}
				}
			}
		}
                
               /* System.out.println("Shortest paths done...");
                int count= 0;
               for(Vertex v:g.getVertices()){
                   if(v.getId() == source.getId())
                       continue;
                     ArrayList<ArrayList<Vertex>> paths = new ArrayList<>();
                                        ArrayList<Vertex> cur = new ArrayList<>();
                                        cur.add(v);
                                        constructPaths(source,v,cur, paths);
                                           
                                        v.allShortestPathsNode.addAll(paths);
                                        System.out.println("gp: "+(++count));
               } */
                
	}
        
        //Add ArrayList<ShortestPaths> during contruction???
        //create new function computeAllShartestPaths(Integer sourceId, Integer targetId)
 
        public void computeAllShortestPaths(Integer sourceId, NHMCDistanceMatrix mat, int lay1, int lay2){//also compute the case in which entities need to be close in all layers 
            //implementation as multigraph -> combination of edges from multiple layers (the important thing is that they are close)
            //finding shortest paths on each layer and averaging -> must be close in all layers (to implement)
            //two for-loops for each layer (special case -> graphs sequence)
            //create graph and compute all shortest paths, save in array for all pairs of layers
            //output sorted by minimal distance + number of layers in which connected
            //for total score, count disconnected nodes as having maximum overall distance (weight = 100)
            //interlayer paths require testing!!!!
            
            
            
            
            int lay1G = lay1, lay2G = lay2;
            int iter = 0;
		Vertex source = getSourceFromId(sourceId);
		source.sourceDistance = 0;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);
		List<Vertex> prev = null;
                 Collection<String> edges = g.getEdges();
                 String e = "";
                 double tmpDist = 0.0;
 
		while (!vertexQueue.isEmpty()) {
                    //System.out.println("Queue size: "+vertexQueue.size());
			Vertex u = vertexQueue.poll();
                        double weight = 1.0;//add different weights
                       
			Collection<Vertex> neighbs = g.getNeighbors(u);

			for (Iterator<Vertex> iterator = neighbs.iterator(); iterator
					.hasNext();) {
				Vertex nv = (Vertex) iterator.next();
                                
                                if(mat.inputType == 0){
                                    e = u.getId()+"-to-"+nv.getId();
                                    if(!edges.contains(e))
                                        continue;
                                    if(weight>mat.m_distancesS.get(u.getId()+"#"+nv.getId()))
                                        weight = mat.m_distancesS.get(u.getId()+"#"+nv.getId());
                                        
                                }
                                else{
                                    // g.addEdge(e1+"_"+l1+"to"+el2+"_"+l2, e1,el2);
                                    if(!mat.connectivityMultiplex.containsKey(u.getId()))
                                        continue;
                                    HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> t=mat.connectivityMultiplex.get(u.getId());
                                    Iterator<Integer> tint = t.keySet().iterator();
                                    int lay = lay1, contained = 0;
                                     weight = 1.0;
                                     
                                     if(!t.containsKey(lay1))
                                         return;
                                     
                                     HashMap<Integer,HashMap<Integer,Double>> optl2 = mat.optim.get(u.getId()).get(lay);
                                     
                                     if(!optl2.containsKey(lay2))
                                         return;
                                    
                                     HashMap<Integer,Double> el2opt = optl2.get(lay2);
                                     if(!el2opt.containsKey(nv.getId()))
                                         continue;
                                     
                                     if(edges.contains(u.getId()+"_"+lay+"-to-"+nv.getId()+"_"+lay2)){
                                         contained = 1;
                                          if(weight>(1.0-el2opt.get(nv.getId())))
                                                    weight = (1.0-el2opt.get(nv.getId())); 
                                     }
                                     
                                   // while(tint.hasNext()){
                                      /*  lay = lay1;
                                        ArrayList<Triplet<Integer,Integer,Double>> n = t.get(lay);
                                        
                                        for(int z = 0;z<n.size();z++)
                                            if(n.get(z).getValue0() == nv.getId() && edges.contains(u.getId()+"_"+lay+"-to-"+nv.getId()+"_"+n.get(z).getValue1()) && n.get(z).getValue1()==lay2){                 
                                                contained = 1;
                                                if(weight>(1.0-n.get(z).getValue2()))
                                                    weight = (1.0-n.get(z).getValue2()); 
                                            }*/
                                   // }
                                    if(contained == 0)
                                        continue;
                                }
                                
				prev = nv.getPrev();
				
				double distanceThroughU = -1.0; 
                                
                                if(lay2G == lay2)
                                    distanceThroughU = u.sourceDistance + weight;
                                else{
                                    distanceThroughU = tmpDist + weight;
                                }

				if (distanceThroughU < nv.sourceDistance) {
					vertexQueue.remove(nv);
                                        if(lay2G == lay2)
                                            nv.sourceDistance = distanceThroughU;
                                        else tmpDist = distanceThroughU;
                                        if(distanceThroughU>maxDist)
                                            maxDist = distanceThroughU;
					nv.setPrevious(u);
					vertexQueue.add(nv);
                                        int tl = lay1;
					lay1 = lay2;
					lay2 = tl;
					prev = new ArrayList<Vertex>();
					prev.add(u);
					nv.setPrev(prev);
				} else if (distanceThroughU == nv.sourceDistance) {
					if (prev != null)
						prev.add(u);
					else {
						prev = new ArrayList<Vertex>();
						prev.add(u);
						nv.setPrev(prev);
					}
				}
			}
                        /* System.out.println("Iteration: "+(++iter));
                  for(Vertex z:vertexQueue)
                      System.out.print(z.getId()+" "+z.sourceDistance+", ");
                  System.out.println();*/
		}
                 
	}
 
        
        
	/**
	 * @param target
	 * @return A List of nodes in order as they would appear in a shortest path.
	 *         (There can be multiple shortest paths present. This method
	 *         returns just one of those paths.)
	 */
	public List<Vertex> getShortestPathTo(Vertex target) {
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = target; vertex != null; vertex = vertex
				.getPrevious())
			path.add(vertex);
		Collections.reverse(path);
		return path;
	}
 
	/**
	 * @param target
	 * @return A set of all possible shortest paths from the source to the given
	 *         target.
	 */
	public Set<List<Vertex>> getAllShortestPathsTo(Vertex target) {
		allShortestPaths = new HashSet<List<Vertex>>();
 
		getShortestPath(new ArrayList<Vertex>(), target);
 
		return allShortestPaths;
	}
 
	/**
	 * Recursive method to enumerate all possible shortest paths and add each
	 * path in the set of all possible shortest paths.
	 * 
	 * @param shortestPath
	 * @param target
	 * @return
	 * 
	 */
	private List<Vertex> getShortestPath(List<Vertex> shortestPath,
			Vertex target) {//must add a list of visited nodes
		List<Vertex> prev = target.getPrev();
                
                if(prev == null)
                    System.out.println("num prev: "+0);
                //else System.out.println("num prev: "+prev.size());

		if (prev == null /*|| prev.size() == 1*/) {
                    List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
			 updatedPath.add(target);
			Collections.reverse( updatedPath);
			allShortestPaths.add( updatedPath);
                         // System.out.println("Target fin: "+target);
                      //  System.out.println("SP size fin: "+shortestPath.size());
                      System.out.println("ASP size: "+allShortestPaths.size());
		}
               /* else if(prev.size() == 0){
                    List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
			 updatedPath.add(target);
			Collections.reverse( updatedPath);
			allShortestPaths.add( updatedPath);
                          System.out.println("Target fin: "+target);
                        System.out.println("SP size fin: "+shortestPath.size());
                }*/
                else {
			int count = 0;
                        int num = prev.size();
			for (Iterator<Vertex> iterator = prev.iterator(); iterator
					.hasNext();) {
                            List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
                          
                        if(!updatedPath.contains(target))     
			    updatedPath.add(target);
                        else break;
                      //  System.out.println(updatedPath);
                       // System.out.println("Target: "+target);
                      //  System.out.println("SP size: "+shortestPath.size());
				Vertex vertex = (Vertex) iterator.next();
                                if(shortestPath.contains(vertex)){
                                    count++;
                                    continue;
                                }
                               /* System.out.println("Done: "+(++count)+" / "+num);
                                System.out.println("Target: "+target.toString());
                                System.out.println("Node info: "+updatedPath.size()+" "+this.g.getVertexCount());*/
				getShortestPath(updatedPath, vertex);
			}
		}
		return shortestPath;
	}
        
        public Set<List<Vertex>> getAllShortestPathsTo(Vertex intermediate, Vertex target, NHMCDistanceMatrix mat, int layStart, int layInt, int layTar) {
		allShortestPaths = new HashSet<List<Vertex>>();
 
		getShortestPath(new ArrayList<Vertex>(), new ArrayList<Integer>(), intermediate, target, mat, layStart, layInt, layTar, 0);
 
		return allShortestPaths;
	}
        
        private List<Vertex> getShortestPath(List<Vertex> shortestPath, List<Integer> SPLayers , Vertex intermediate,
			Vertex target, NHMCDistanceMatrix mat, int layStart, int layInt, int layTar, int containInter){//toDo, unfinished, should be corrected
            String e = "";
            int found = 0, foundInt = 0, tmp, tried = 0;
            
		List<Vertex> prev = target.getPrev();
                if(prev!=null){
                /*System.out.println("T: "+target.getId()+"  I:"+intermediate.getId()+" PSize: "+prev.size());
                System.out.println("LS: "+layStart+" LI: "+layInt+" LT: "+layTar);
                System.out.println("SP size: "+shortestPath.size());
                for(int v=0;v<shortestPath.size();v++)
                    System.out.print(shortestPath.get(v).getId()+" ");
                System.out.println();*/
                }
		if (prev == null) {
			shortestPath.add(target);
                        SPLayers.add(layTar);
			Collections.reverse(shortestPath);
                        if(containInter == 1)
                            allShortestPaths.add(shortestPath);
		} else {
			List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
			updatedPath.add(target);
                        List<Integer> updatedLayers = new ArrayList<Integer>(SPLayers);
			updatedLayers.add(layTar);
 
			for (Iterator<Vertex> iterator = prev.iterator(); iterator
					.hasNext();) {
				Vertex vertex = (Vertex) iterator.next();
                                Iterator<Integer> it = mat.connectivityMultiplex.keySet().iterator();
                                int layT = layStart;
                                
                                //while(it.hasNext()){
                                  //  int layT = it.next();
                                    if( vertex.getPrev() == null && layT!=layStart)
                                        continue;
                                    
                                    int same = 0;
                                   // System.out.println("UPS: "+updatedPath.size());
                                   // System.out.println("ULS: "+updatedLayers.size());
                                   for(int g=0;g<updatedPath.size();g++){
                                       if(updatedPath.get(g).equals(vertex)){
                                           if(updatedLayers.get(g).equals(layT)){
                                               same = 1;
                                               break;
                                           }                              
                                       }
                                   }
                                    
                                   if(same == 1)
                                       continue;
                                
                                   tried++;
                                ArrayList<Triplet<Integer,Integer,Double>> d = mat.connectivityMultiplex.get(vertex.getId()).get(layT);
                               for(int z=0;z<d.size();z++)
                                   if(d.get(z).getValue0() == target.getId() && d.get(z).getValue1() == layTar){
                                       found = 1;
                                       tmp = layStart;
                                       layStart = layTar;
                                       layTar = tmp;
                                       if(vertex.getId() == intermediate.getId() && layT == layInt || containInter == 1)
                                           foundInt = 1;
                                    //   if(found == 1)
                                     //System.out.println("UPSbc: "+updatedPath.size());
                                    //System.out.println("ULSbc: "+updatedLayers.size());
                                    getShortestPath(updatedPath, updatedLayers, intermediate, vertex,mat,layStart,layInt, layT, foundInt);
                                    foundInt = 0;
                                }
                                   }
                               
			}
		//}
                if(prev!=null && tried == 0 && foundInt == 1)
                     allShortestPaths.add(shortestPath);
		return shortestPath;
	}
        
        
        public Graph<Vertex, String> generateGraph(NHMCDistanceMatrix mat) {
		Graph<Vertex, String> g = new SparseMultigraph<Vertex, String>();
                 int n=0;
                 
                 Iterator<Integer> it=null;
                 
                HashMap<Integer,Vertex> vertexList = new HashMap<>(); 
                 
                if(mat.inputType == 0){
                    n = mat.connectivity.keySet().size(); 
                    it = mat.connectivity.keySet().iterator();
                    
                    while(it.hasNext()){
                        int e = it.next();
                        vertexList.put(e,new Vertex(e));
                        g.addVertex(vertexList.get(e));
                    }
                    
                    it = mat.connectivity.keySet().iterator();
                    
                    while(it.hasNext()){
                        int e1 = it.next();
                        HashSet<Integer> els = mat.connectivity.get(e1);
                        Iterator<Integer> it1 = els.iterator();
                        
                        int id1=0,id2=0;
                        
 
                        while(it1.hasNext()){
                             int el2 = it1.next();
                            
                             if(e1 == el2)
                                 continue;
                                    if(!vertexList.containsKey(el2)){
                                        vertexList.put(el2,new Vertex(el2));
                                         g.addVertex(vertexList.get(el2));
                                    }
                            g.addEdge(e1+"-to-"+el2, vertexList.get(e1),vertexList.get(el2)); 
                        }
                    }
                    
                }
                else{ 
                    n = mat.connectivityMultiplex.keySet().size();
                    it = mat.connectivityMultiplex.keySet().iterator();
                
                     while(it.hasNext()){
                         int e = it.next();
                        vertexList.put(e,new Vertex(e));
                        g.addVertex(vertexList.get(e));
                    }
                     
                     it = mat.connectivityMultiplex.keySet().iterator();
                     
                     while(it.hasNext()){
                         int e1 = it.next();
                         HashMap<Integer,ArrayList<Triplet<Integer,Integer,Double>>> d=mat.connectivityMultiplex.get(e1);
                         
                         Iterator<Integer> l = d.keySet().iterator();
                         
                         while(l.hasNext()){
                             int l1 = l.next();
                             ArrayList<Triplet<Integer,Integer,Double>> vl = d.get(l1);
                             int el2, l2;
                             for(int k=0;k<vl.size();k++){
                                 el2 = vl.get(k).getValue0();
                                 l2 = vl.get(k).getValue1();
                                 
                                 if(e1 == el2 && l1 == l2)
                                          continue;
                                  if(!vertexList.containsKey(el2)){
                                        vertexList.put(el2,new Vertex(el2));
                                         g.addVertex(vertexList.get(el2));
                                    }
                                 g.addEdge(e1+"_"+l1+"-to-"+el2+"_"+l2, vertexList.get(e1),vertexList.get(el2));
                             }
                         }
                         
                     }
                     
                }
                this.g = g;
		return g;
	}
        
}
