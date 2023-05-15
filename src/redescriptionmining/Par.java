/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redescriptionmining;

/**
 *
 * @author Matej
 */
public class Par {
    String og;
    int brojPojavljivanja; 
    
    public Par(){
        og = "";
        brojPojavljivanja = 0;
    }
    
    
    public Par(String o, int b){
        og = o; brojPojavljivanja = b;
    }
    
    public String getOg(){return og;}
    public int getPoj(){return brojPojavljivanja;}
    
    @Override
    public String toString(){
        String tmp;
        
        tmp = this.og+" "+this.brojPojavljivanja;
        return tmp;
    }
    
}
