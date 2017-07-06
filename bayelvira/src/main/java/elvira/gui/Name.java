/* Name */

package elvira.gui;
import elvira.Elvira;

/**
 * Class Name.
 * This class replaces not allowed symbols of the variable's name
 *
 * @author Almer�a
 *
 * @since 28/10/2002
 */



public class Name {
    String chain;
    
    public String checkSymbols() {
        String checked;
        checked=chain.replace(',','.');
        checked=checked.replace('-','_');
        checked=checked.replace('�','a');
        checked=checked.replace('�','A');
        checked=checked.replace('�','e');
        checked=checked.replace('�','E');
        checked=checked.replace('�','i');
        checked=checked.replace('�','I');
        checked=checked.replace('�','o');
        checked=checked.replace('�','O');
        checked=checked.replace('�','u');
        checked=checked.replace('�','U');
        
        checked=checked.replace('�','n');
        checked=checked.replace('�','N');
        
        checked=checked.replace('�','o');
        checked=checked.replace('�','a');
        
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