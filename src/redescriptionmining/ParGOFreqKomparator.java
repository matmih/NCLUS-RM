/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

import java.util.Comparator;

/**
 *
 * @author Matej
 */
public class ParGOFreqKomparator implements Comparator<ParGOFreq> {
    
    @Override
    public int compare(ParGOFreq p1, ParGOFreq p2){
        if(p1.freq<p2.freq)
            return 1;
        else if(p1.freq == p2.freq)
            return 0;
        else return -1;
    }
    
}
