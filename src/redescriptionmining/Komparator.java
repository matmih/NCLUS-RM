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
public class Komparator implements Comparator<Par> {
    @Override
    public int compare(Par x, Par y){
        if(x.brojPojavljivanja<y.brojPojavljivanja)
            return 1;
        else if(x.brojPojavljivanja == y.brojPojavljivanja)
            return 0;
        else return -1;
    }
}
