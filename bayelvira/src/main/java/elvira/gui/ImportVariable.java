
package elvira.gui;

import elvira.Elvira;

/**
 * Class ImportVariable.
 * This class creates an array for the variable's information of the dbc files
 * Contains the associate methods to this array.
 *
 * @author fsoler@ual.es
 * @author avrofe@ual.es
 *
 * @since 07/06/2004
 */

public class ImportVariable {
    
    int regNumber;
    String registers[];
    String variableName;
    private int counter = 0;
    boolean isNumeric = true;
    boolean isContinuous = false;
    boolean isInteger = false;
    boolean isforced = false;
    int casesNumber = 0;
    String cases[];
    Double maxValue;
    Double minValue;
    String comment;
    int states;
    String output[];
    double[][] valuesArray;
    String statesDefinition = new String();
    boolean isEdited = false;
    
    
    
    
    /* METHODS */
    
    /**
     * Array Initialisation
     * @param value An integer value with the registers.
     */
    
    public void initialise(int value) {
        
        regNumber = value;
        registers = new String[regNumber];
    }
    
    
    /**
     * Set the name of the array's variable
     * @param name A String with the variable name.
     */
    
    public void setName(String name) {
        
        variableName = name; // A string contains the variable's name.
    }
    
    
    /**
     *
     * @return
     */
    
    public void setInte(boolean state){
        
        isInteger = state;
        
        
    }
    
    
    /**
     * Get the name of the array's variable
     * @return A String with the variable's name.
     */
    
    public String getName() {
        
        return variableName;
    }
    
    
    /**
     * Replaces not allowed symbols of the variable's name
     * @param name A String with the variable's name.
     * @return A String with the  corrected variable's name.
     */
    
    public static String checkSymbols(String name) {
        
        String checked;
        
        checked = name.replace(',','.');
        checked = checked.replace('á','a');
        checked = checked.replace('Á','A');
        checked = checked.replace('é','e');
        checked = checked.replace('É','E');
        checked = checked.replace('í','i');
        checked = checked.replace('Ì','I');
        checked = checked.replace('ó','o');
        checked = checked.replace('Ó','O');
        checked = checked.replace('ú','u');
        checked = checked.replace('Ú','U');
        checked = checked.replace('ñ','n');
        checked = checked.replace('Ñ','N');
        checked = checked.replace('º','o');
        checked = checked.replace('ª','a');
        checked = checked.replace('-','_');
        // checked = checked.replace('/','_');
        //checked = checked.replace('\\','_');
        checked = checked.replace('(','_');
        checked = checked.replace(')','_');
        //checked = checked.replace('"','_');
        
        //se permite que haya espacios al principio y al final
        //no se cambian por _ pero si se tienen q borrar
        
        char empty=' ';
        
        while (checked.indexOf(empty)==0)
            checked = checked.substring(1);
        
        while (checked.indexOf(empty)==(checked.length()-1)){
            checked = checked.substring(0, checked.length() - 1);
        }
        
        
        checked = checked.replace(empty, '_' ); //por si habia por enmedio tb
        
        if (name.compareTo("?")==0)
            checked = checked.replace('?','_'); //si el valor del campo es ? que lo cambie por _
        
        //checked = replace('+',"_plus_", checked);
        //    checked = replace('&',"_and_", checked);
        //  checked = replace('%',"_perCent", checked);
        //checked = replace('<',"_smaller_than_", checked);
        //checked = replace('>',"_bigger_than_", checked);
        //checked = replace('=',"_equal_than_", checked);
        
        // * ! \'a1 \'bf #  @ $ ~ { [ ???????????????????
        
        return checked;
    }
    
    
    /**
     * Replace in the String name, all occurrences of th character oldChar
     * with the String newString
     *
     * @param oldChar the old char
     * @param newString the new String
     * @param name the String within we replaced
     * @return
     */
    
    public static String replace(char oldChar, String newString, String name){
        
        int beginIndex ;
        String subString = name ;
        beginIndex = subString.indexOf(oldChar);
        String superString;
        
        
        while ((beginIndex!=(-1))&&(subString.compareTo("")!=0)){
            
            if (beginIndex!=name.length())
                subString = name.substring(beginIndex+1);
            else subString ="";
            
            if (beginIndex!=0)
                superString = name.substring(0,beginIndex-1);
            else superString="";
            
            if ((subString.compareTo("")!=0)&&(superString.compareTo("")!=0))
                name = superString+newString+subString;
            else if ((subString.compareTo("")!=0)&&(superString.compareTo("")==0))
                name = newString+subString;
            else if ((superString.compareTo("")!=0)&&(subString.compareTo("")==0))
                name = superString+newString;
            else if ((superString.compareTo("")==0)&&(subString.compareTo("")==0))
                name = newString;
            
            if (subString.compareTo("")!=0)
                beginIndex = subString.indexOf(oldChar);
            
        }
        
        return name;
        
    }
    
    
    /**
     * If the first char in String is a number then add a '_' at the beginning
     */
    
    public static String checkNumbers(String name) {
        
        String checked = name;
        char firstValue;
        
        firstValue = name.charAt(0);
        //If the first char in String is a number then add a 's' at the beginning
        if (firstValue=='0'
        ||firstValue=='1'||firstValue=='2'||firstValue=='3'||firstValue=='4'||firstValue=='5'||firstValue=='6'||firstValue=='7'||firstValue=='8'||firstValue=='9') {
            String underlined = new String("s");
            checked = underlined.concat(name);
        }
        
        return checked;
    }
    
    
    void removeLastRegister(){
        regNumber = regNumber-1;
    }
    
    /**
     * Add a new register and check the String
     * @param chain A  new String.
     */
    
    void addRegister(String chain) {
        
        String checked = new String(chain);
        
        char empty = ' ';
        
        //remove spaces at the begining (it can be " ")
        while (checked.indexOf(empty)==0)
            checked = checked.substring(1);
        
        
        if ((checked.length())==0){
            //if it is empty, replace with "?"
            checked = new String("?");
            System.out.print(checked+" ");
        }
        else{
            boolean primeravez ;
            checked = checked.replace(',','.');
            
            while (checked.indexOf(empty)==(checked.length()-1)){
                checked = checked.substring(0, checked.length() - 1);
            }
            
            System.out.print(checked+" ");
        }//del else if it is empty
        
        if (checked.compareTo("?")!=0){
            try {
                Double value = new Double(checked);
            } catch(NumberFormatException conversionError) {
                isNumeric = false;
            }
            if (isNumeric==false){
                checked = checkSymbols(checked);
                //no sigas, ya sabes q es un string y q
                //isInteger=false;
                //isContinuous=false;
                //pero no hace falta ponerlo pq son los valores por defecto
                //para evitar confusiones:
                //(pueden mezclarse /2 con 2 ojoo
                
            }
            else{
            	if (isContinuous!= true)
            	{
            		try {
            			Integer value2 = new Integer(checked);
            			isInteger = true;
            		}
            		catch (NumberFormatException conversionError) {
            			isContinuous = true;
            		}
            	}
            }
            
        }//del if it is not "?"
        
        
        if (isNumeric==false && counter==(regNumber-1)){
            //it must be a string
            for (int i = 0; i < counter ; i++){
            	//si he leido antes un valor numerico, lo he almacenado como numerico
                //y tienen q ser todos string
                if (registers[i].compareTo("?")!=0){
                	registers[i] = checkSymbols(registers[i]);
                    registers[i] = checkNumbers(registers[i]);
                    //write "" in all the values, if it is finite
                    //but, if it had " ", don't write
                   	registers[i] = "\"" + registers[i] + "\"";
                }
            }
            
            
            //ojo esto puede hacer que un numerico 5 sea _5 q ya no es numerico
            if (checked.compareTo("?")!=0){
                //if it had " ", don't write
            	checked = checkSymbols(checked);
                checked = checkNumbers(checked);
                registers[counter] = "\"" + checked + "\"";
            }else
                registers[counter] = checked;
            counter++;
        }//end of (isNumeric==false && counter==(regNumber-1))
        else{
            //if it is numeric but not continuous, write " " and "s "
            if (isNumeric==true && isContinuous==false && counter==(regNumber-1)){
                for (int i = 0; i < counter ; i++){
                    //si he leido antes un valor numerico, lo he almacenado como numerico
                    //y tienen q ser todos string
                    if (registers[i].compareTo("?")!=0){
                    	//write "" in all the values, if it is finite
                        registers[i] = "\"s"+registers[i]+"\"";
                    }
                }
                
                
                //ojo esto puede hacer que un numerico 5 sea _5 q ya no es numerico
                if (checked.compareTo("?")!=0){
                	registers[counter] = "\"s"+checked+"\"";
                }else
                    registers[counter] = checked;
                counter++;
            }//end of if it is numeric but not continuous
            else {
                registers[counter] = checked;
                counter++;
            }
        }//end of else if it is not numeric and counter is the last
        
        
        
    }//end of addRegister
    
    
    /**
     * To know if the variable is numeric or not
     * @return <code>true</code> if the variable is numeric,
     * <code>false</code> otherwise.
     */
    
    boolean getNumeric() {
        
        return isNumeric;
    }
    
    
    /**
     * To know if the variable is continuous or not
     * @return <code>true</code> if the variable is continuous,
     * <code>false</code> otherwise.
     */
    
    boolean getContinuous() {
        
        return isContinuous;
    }
    
    
    /**
     * To know if the variable is an integer or not
     * @return <code>true</code> if the variable is an integer,
     * <code>false</code> otherwise.
     */
    
    boolean getInte() {
        
        return isInteger;
    }
    
    
    /**
     * To know if the variable has been showed
     * @return <code>true</code> if the variable ihas been showed,
     * <code>false</code> otherwise.
     */
    
    boolean getEdited() {
        
        return isEdited;
    }
    
    
    /**
     * To know if the variable has been forced to be discrete
     * @return <code>true</code> if the variable has been forced to be discrete,
     * <code>false</code> otherwise.
     */
    
    boolean disForced(boolean force){
        
        boolean isDiscrete = force;
        return isDiscrete;
    }
    
    
    /**
     * Get the values contained in the array
     * @return A String with the number of cases
     */
    
    String getValues() {
        
        Integer value = new Integer(casesNumber);
        return value.toString();
    }
    
    
    /**
     * Sort the array's values and set the repetition of them
     */
    
    void makeConversion() {
        
        maxValue = new Double(0);
        minValue = new Double(0);
        output = new String[regNumber];
        String auxiliar[] = new String[regNumber];
        
        //if it is finite-states, store states
        if ((isNumeric==false) || (isContinuous==false)) {
            //cases = new String[casesNumber];
            for (int j=0 ; j<regNumber ; j++) {
                output[j] = registers[j];
                String compare = registers[j];
                int exit = 0;
                int k = 0;
                for (k=0 ; k<casesNumber ; k++) {
                    if (compare.compareTo(auxiliar[k]) == 0) {
                        exit = 1;
                    }
                }
                if ((exit==0) && (casesNumber==k)) {
                    if(compare.compareTo("?")!=0){
                        compare = checkSymbols(compare);
                        auxiliar[casesNumber] = compare;
                        casesNumber++;
                    }
                    
                }
            }//end for j
            cases = new String[casesNumber];
            for (int j=0; j<casesNumber ; j++)
            {
            	cases[j]=auxiliar[j];
            }            
        }//end if isnumeric=false o iscont = false
        
        //if it is numeric , calculate min and max
        if (isNumeric ==true) {
            
            comment = new String();
            
            double smaller = 999999;
            double bigger = 0;
            double comparer = 0 ;
            
            String aux;
            
            valuesArray = new double[regNumber][2];
            int state = 0;
            
            for (int j=0 ; j<regNumber ; j++) {
                valuesArray[j][0] = 0.00000000;
                valuesArray[j][1] = 0;
                output[j] = registers[j];
            }
            int found;
            for (int j=0 ; j<regNumber ; j++) {
                
                try{
                    Double reg = new Double(0.0);
                    if (registers[j].compareTo("?")!=0)
                    {
                    //if it is not continuos, it must be integer
                    	if (isContinuous == false){
                            aux = new String(registers[j].substring(2,
                            registers[j].length() - 1));
                            reg = reg.valueOf(aux);
                            comparer = reg.intValue();
                        }else{
                            reg = reg.valueOf(registers[j]);
                            comparer = reg.doubleValue();
                        }
                    	if (comparer > bigger) {
                    		bigger = comparer;
                    	}
                    	if (comparer < smaller) {
                    		smaller = comparer;
                    	}
                    
                    	found = 0;
                    
                    	int k;
                    	for (k = 0; k < state; k++) {
                    		if (valuesArray[k][0] == comparer) {
                    			valuesArray[k][1] = valuesArray[k][1] + 1;
                    			found = 1;
                    		}
                    	}
                    	if ( (k == state) && (found == 0)) {
                    		valuesArray[state][0] = comparer;
                    		valuesArray[k][1] = valuesArray[k][1] + 1;
                    		state++;
                    	}
                    	casesNumber = state;
                    }
                } catch (NumberFormatException e){
                }
            }//end of for j
            
            //To sort the array
            for (int q=0 ; q<state ; q++) {
                for (int w=0 ; w<state-1 ; w++) {
                    if (valuesArray[w][0] > valuesArray[w+1][0]) {
                        double indexValue = valuesArray[w][0];
                        double numberValue = valuesArray[w][1];
                        
                        valuesArray[w][0] = valuesArray[w+1][0];
                        valuesArray[w][1] = valuesArray[w+1][1];
                        
                        valuesArray[w+1][0] = indexValue;
                        valuesArray[w+1][1] = numberValue;
                    }
                }
            }
            
            maxValue = new Double(bigger);
            minValue = new Double(smaller);
            
            
        }//end if isnumeric=true
        
    }//end makeConversion
    
    
    /**
     * This method makes the desired number of intervals with the same number
     * of observations and assign each value to its interval.
     */
    
    void sameNumberOfObservationIntervals() {
        
        if (isNumeric == true) {
            int intervals; //The desired number of intervals
            int byInterval;
            int intervalElements; //Elements in each interval.
            intervals = states;
            intervalElements = casesNumber / intervals;
            
            int accumulate = 0;
            int actualInterval = 0;
            
            comment = "The intervals are : {"+minValue.doubleValue()+"-";
            
            int position = 0;
            int situation = 0;
            
            //To make the intervals
            for (int q=0 ; q<states-1 ; q++) {  //states is the number of intervals that you want
                if (q == 0) {
                    situation = intervalElements - 1;
                    comment = comment + valuesArray[situation][0] + "-";
                }
                else {
                    situation = situation + intervalElements;
                    comment = comment + valuesArray[situation][0] + "-";
                }
                //To know where is the observation
                for (int l=0 ; l<intervalElements ; l++)
                    valuesArray[position+l][1] = q;
                
                position = position + intervalElements;
            }//end for
            
            //The last observation are in the last interval
            for (int p=position ; p<regNumber ; p++)
                valuesArray[p][1] = states-1;
            
            comment = comment + maxValue + "}";
            
            //Write in an array the interval name
            for (int j=0 ; j<intervals ; j++) {
                statesDefinition = statesDefinition + "S" + j + " ";
            }
            
            //Put the information in an array
            for (int j=0 ; j<regNumber ; j++) {
                Double value = new Double(0.0);
                value = value.valueOf(registers[j]);
                
                double valueD = value.doubleValue();
                
                int index = 0;
                for (int k=0 ; k<casesNumber ; k++) {
                    if (valueD == valuesArray[k][0]) {
                        index = k;
                    }
                }
                
                registers[j] = "S" + (int)valuesArray[index][1];
            }//for end
        }
    }
    
    
    /**
     * This method makes the desired number of intervals with the same length,
     * and assign each value to its interval.
     */
    
    void sameLengthIntervals() {
        
        if (isNumeric == true) {
            Integer intervals = new Integer(states); //We want this number of intervals
            int byInterval;
            Double intervalsLength = new Double((maxValue.doubleValue() -
            minValue.doubleValue())/intervals.doubleValue());//Cálculo de la longitud de cada intervalo
            double lg = intervalsLength.doubleValue();
            int accumulate = 0;
            int actualInterval = 0;
            comment="The intervals are : {"+minValue.doubleValue()+"-";
            
            Integer position = new Integer(0);
            Double situation = new Double(minValue.doubleValue());
            int po = position.intValue();
            int cut;
            
            //To make the intervals
            for (int q=0 ; q<states-1 ; q++) { //states is the number of intervals that you want
                
                boolean control = true;
                double pos;
                pos = (situation.doubleValue()+intervalsLength.doubleValue());
                situation = new Double(pos);
                comment = comment + situation.doubleValue() + "-";
                
                //To know where is the observation
                while (control == true) {
                    double value = valuesArray[po][0];
                    
                    if (value < pos) {
                        valuesArray[po][1] = q;
                        po++;
                    }//end if
                    else
                        control = false;
                }//end while
            }//end for
            
            
            for (int f=po ; f<regNumber ; f++)
                valuesArray[f][1] = states-1;
            
            comment = comment + maxValue + "}";
            
            
            //Write in an array the intervals' name
            for (int j=0 ; j<intervals.intValue() ; j++) {
                statesDefinition = statesDefinition + "S" + j + " ";
            }
            
            //To know where is the observation
            for (int j=0 ; j<regNumber ; j++) {
                Double value = new Double(0.0);
                value = value.valueOf(registers[j]);
                double valueD = value.doubleValue();
                
                int index = 0;
                for (int k=0 ; k<casesNumber ; k++) {
                    if (valueD==valuesArray[k][0]) {
                        index = k;
                    }
                }
                
                registers[j] = "S" + (int)valuesArray[index][1];
            }//end for
        }
    }
    
    
    /**
     * This method doesn't divide in itervals.
     */
    
    void nothing() {
        
        isNumeric = false;
        if (isNumeric == true) {
        }//End if
    }
}

