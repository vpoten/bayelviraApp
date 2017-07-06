/* Name */

package elvira.gui;
import elvira.Elvira;

/**
 * Class Name.
 * This class replaces not allowed symbols of the variable's name
 *
 * @author Almería
 *
 * @since 28/10/2002
 */



public class Name {
    String chain;
    
    public String checkSymbols() {
        String checked;
        checked=chain.replace(',','.');
        checked=checked.replace('-','_');
        checked=checked.replace('á','a');
        checked=checked.replace('Á','A');
        checked=checked.replace('é','e');
        checked=checked.replace('É','E');
        checked=checked.replace('í','i');
        checked=checked.replace('Í','I');
        checked=checked.replace('ó','o');
        checked=checked.replace('Ó','O');
        checked=checked.replace('ú','u');
        checked=checked.replace('Ú','U');
        
        checked=checked.replace('ñ','n');
        checked=checked.replace('Ñ','N');
        
        checked=checked.replace('º','o');
        checked=checked.replace('ª','a');
        
        checked=checked.replace('\'','_');
        char empty=' ';
        checked=checked.replace(empty,'_');
        
        return checked;
    }
    
    
    /**
      *If the first char in String is a number then add a '_' at the beginning
     */
    String checkNumbers() {
        String checked=chain;
        char firstValue;
        
        firstValue=chain.charAt(0);
        if (firstValue=='0' ||firstValue=='1'||firstValue=='2'||firstValue=='3'||firstValue=='4'||firstValue=='5'||firstValue=='6'||firstValue=='7'||firstValue=='8'||firstValue=='9') {
            String underlined=new String("_");
            checked=underlined.concat(chain);
        }
        
        return checked;
    }
    
}