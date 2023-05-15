/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author matej
 */
public class GlobalAvDens {
    public static void main(String [] args){
        ApplicationSettings appset=new ApplicationSettings();
        appset.readSettings(new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\Settings.set"));
        appset.readPreference();  
        NHMCDistanceMatrix nclMatInit=null;
         nclMatInit = new NHMCDistanceMatrix();
        nclMatInit.loadDistanceMatrix(appset);
        System.out.println(nclMatInit.computeAverageDensity(appset)+"");
        
        ReadCLUSRMReds rreds = new ReadCLUSRMReds();
         Mappings fid=new Mappings();
         fid.createIndex(appset.outFolderPath+"\\Jinput.arff");
        rreds.inputFile=new File("C:\\Users\\matej\\Documents\\Redescription mining with CLUS\\redescriptionsGuidedExperimentalIterativeCountryNetworkTestingAlpha = 0.5SP.rr1.rr");
         DataSetCreator datJ=new DataSetCreator(appset.viewInputPaths, appset.outFolderPath,appset);
        rreds.readReds(datJ,fid);
        System.out.println("Read");
        
        ArrayList<Double> den = new ArrayList<>();
        
        for(int i=0;i<rreds.set.redescriptions.size();i++){
             den.add(rreds.set.redescriptions.get(i).JS);
            //den.add(rreds.set.redescriptions.get(i).computeNetworkDensity(nclMatInit, appset));
          //  System.out.println(rreds.set.redescriptions.get(i).computeNetworkDensity(nclMatInit, appset)+"");
        }
        
        for(int i=0;i<rreds.set.redescriptions.size();i++){
            System.out.println(den.get(i));
        }
        
    }
}
