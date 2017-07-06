/*
*   Class   FileInput
*
*   Methods for entering doubles, floats, integers,
*   long integers, lines (as String), words (as String)
*   and chars from a text file.
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:       July 2002
*   REVISED:    14 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   FileInput.html
*
*   Copyright (c) April 2004
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package elvira.tools.statistics.io;

import java.io.*;
import elvira.tools.statistics.complex.Complex;

public class FileInput{

        // Instance variables
        private String filename = "";           //input filename
        private String fullLine = "";           //current line in input file
        private String fullLineT = "";          //current line in input file trimmed of trailing spaces
        private BufferedReader input = null;    //instance of BufferedReader
        private boolean testFullLine = false;   //false if fullLine is empty
        private boolean testFullLineT = false;  //false if fullLineT is empty
        private boolean eof = false;            //true if reading beyond end of file attempted
        private boolean fileFound = true;       //true if file named is found
        private boolean inputType = false;      //false in input type is a Sting
                                                //true if input type is numeric or char, i.e. double, float, int, long, char

        // Constructor
        public FileInput(final String filename){
                this.filename = filename;
                try{
                        this.input = new BufferedReader(new FileReader(this.filename));
                }catch(java.io.FileNotFoundException e){
                        System.out.println(e);
                        fileFound=false;
                }
        }

        // Methods

        // Reads a double from the file
        public final synchronized double readDouble(){
                this.inputType = true;
                String word="";
                double dd=0.0D;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)dd = Double.parseDouble(word.trim());

                return dd;
        }

        // Reads a float from the file
        public final synchronized float readFloat(){
                this.inputType = true;
                String word="";
                float ff=0.0F;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ff = Float.parseFloat(word.trim());

                return ff;
        }

        // Reads an integer (int) from the file
        public final synchronized int readInt(){
                this.inputType = true;
                String word="";
                int ii=0;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ii = Integer.parseInt(word.trim());

                return ii;
        }

        // Reads a long integer from the file
        public final synchronized long readLong(){
                this.inputType = true;
                String word="";
                long ll=0L;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ll = Long.parseLong(word.trim());

                return ll;
        }

        // Reads a Complex from the file
        public final synchronized Complex readComplex(){
                return Complex.readComplex();
        }

        // Reads a word (a string between spaces) from the file
        public final synchronized String readWord(){
                this.inputType = false;
                String word="";

                if(!this.testFullLineT) this.enterLine();
                if(this.fullLine.equals("")){
                    word="";
                }else
                {
                    word = nextWord();
                }

                return word;
        }

        // Public method for reading a line from the file
        public final synchronized String readLine(){
            this.inputType = false;
            return this.readLineL();
        }

        // Private method for reading a line from the file
        private final synchronized String readLineL(){
                String line="";
                try{
                        line = input.readLine();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
               if(line==null){
                    System.out.println("Attempt to read beyond the end of the file");
                    eof=true;
                    line="";
                }
                return line;
        }

        // Reads a character from the file
        public final synchronized char readChar(){
                String word="";
                char ch=' ';

                if(!this.testFullLine) this.enterLine();
                word = nextWord();
                if(!eof)ch = word.charAt(0);
                return ch;
        }

        // Close file
        public final synchronized void close(){
            if(fileFound){
                try{
                        input.close();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
            }
        }

        // Get the end of file status, eof.
        public boolean eof(){
            return eof;
        }

        // Get the file existence status, fileFound.
        public boolean fileFound(){
            return fileFound;
        }

        // enters a line from the file into the fullLine and fullLineT strings
        private final synchronized void enterLine(){
                int i=0;

                this.fullLine=this.readLineL();
                this.fullLineT=this.fullLine;
                if(!this.fullLine.equals("")){
                    i=this.fullLineT.length()-1;
                    while(this.fullLineT.charAt(i)==' ' && i>=0){
                            this.fullLineT=this.fullLineT.substring(0,i);
                            i--;
                    }
                }
        }

        // reads the next word (a string between spaces) from the String fullLine
        private final synchronized String nextWord(){
                this.testFullLine=true;
                this.testFullLineT=true;
                String  word = "";
                int     posspa=-1, postab=-1, possp=-1, poscom=-1;
                boolean test = true;
                int len=this.fullLine.length();

                // strip end of the word of any leading spaces, tabs or, if numerical input, commas
                boolean test0 = true;
                boolean test1 = false;
                int pend =this.fullLine.length();
                while(test0){
                    pend--;
                    if(this.fullLine.charAt(pend)==' ')test1=true;
                    if(this.fullLine.charAt(pend)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(pend)==',')test1=true;
                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(0,pend);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // strip front of the word of any leading spaces, tabs or, if numerical input, commas
                test0 = true;
                test1 = false;
                while(test0){
                    if(this.fullLine.charAt(0)==' ')test1=true;
                    if(this.fullLine.charAt(0)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(0)==',')test1=true;
                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(1);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // find first space, tab or, if numeric, comma
                posspa=this.fullLine.indexOf(' ');
                postab=this.fullLine.indexOf('\t');
                if(this.inputType)poscom=this.fullLine.indexOf(',');

                if(posspa==-1){
                    if(postab==-1){
                        if(poscom==-1){
                            possp=-1;
                        }
                        else{
                            possp=poscom;
                        }
                    }
                    else{
                        if(poscom==-1){
                            possp=postab;
                        }
                        else{
                            if(postab<poscom){
                                possp=postab;
                            }
                            else{
                                possp=poscom;
                            }
                        }
                    }
                }
                else{
                    if(postab==-1){
                        if(poscom==-1){
                            possp=posspa;
                        }
                        else{
                            if(poscom<posspa){
                                possp=poscom;
                            }
                            else{
                                possp=posspa;
                            }
                        }
                    }
                    else{
                        if(poscom==-1){
                            if(posspa<postab){
                                possp=posspa;
                            }
                            else{
                                possp=postab;
                            }
                        }
                        else{
                            if(posspa<postab && posspa<poscom)possp=posspa;
                            if(postab<posspa && postab<poscom)possp=postab;
                            if(poscom<posspa && poscom<postab)possp=poscom;
                        }
                    }
                }

                // remove first word first word from string
                if(possp==-1){
                        word=this.fullLine;
                        this.fullLine="";
                        this.testFullLine=false;
                }
                else{
                        word=this.fullLine.substring(0,possp);

                        if(possp+1>this.fullLine.length()){
                                this.fullLine="";
                                this.testFullLine=false;
                        }
                        else{
                                this.fullLine=this.fullLine.substring(possp+1);
                                if(this.fullLine.length()==0)this.testFullLine=false;
                        }
                }
                if(this.testFullLineT){
                        if(!this.testFullLine){
                                this.testFullLineT=false;
                                this.fullLineT="";
                         }
                         else{
                                if(possp+1>this.fullLineT.length()){
                                        this.fullLineT="";
                                        this.testFullLineT=false;
                                }
                        }
                }

                // return first word of the supplied string
                return word;
        }

       // reads the next char from the String fullLine
        private final synchronized char nextChar(){
                this.testFullLine=true;
                char  ch=' ';
                boolean test = true;

                ch=this.fullLine.charAt(0);
                this.fullLine=this.fullLine.substring(1);
                if(this.fullLine.length()==0)this.testFullLine=false;
                if(this.testFullLineT){
                        this.fullLineT=this.fullLineT.substring(1);
                        if(this.fullLineT.length()==0)this.testFullLineT=false;
                }

                return ch;
        }
}
