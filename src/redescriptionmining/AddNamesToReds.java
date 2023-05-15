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
public class AddNamesToReds {
    static public void main(String [] args){
         File redescriptionInput=new File("F:\\PlosOneADPaperRevision2\\Dodatni dokumenti\\Appendix\\S11_File.txt");
         File output=new File("F:\\PlosOneADPaperRevision2\\Dodatni dokumenti\\Appendix\\S11_FileNew.txt");
         int count=1;

         try (BufferedReader bufRdr1 = new BufferedReader(new FileReader(redescriptionInput)))
        {
            FileWriter fw = new FileWriter(output);
            String line;
            String label="";
            while ((line = bufRdr1.readLine()) != null)
            {

                if(line.contains("Rules: ")){
                    line="Redescription R"+count+" "+line;
                    fw.write(line+"\n");
                    count++;
                }
                else{
                    fw.write(line+"\n");
                }
            }
            bufRdr1.close();
            fw.close();
        }
       catch(Exception e){
           e.printStackTrace();
       }
    }
}
