/*
*   Class FileOutput
*
*   Methods for writing doubles, floats, integers,
*   long integers, Strings, chars, booleans, Complex,
*   ErrorProp and ComplexErrorProp to a text file.
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:    July 2002
*   UPDATED: 26 April 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   FileOutput.html
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
import java.text.*;
import java.util.*;

import elvira.tools.statistics.complex.*;
import elvira.tools.statistics.analysis.ErrorProp;

public class FileOutput{

        // Instance variables
        private String filename = "";       // output file name
        private FileWriter fwoutput = null; // instance of FileWriter
        private PrintWriter output = null;  // instance of PrintWriter
        private boolean append = false;     // true data appended to a file, false new file
        private char app = 'w';             // 'w' new file - overwrites an existing file of the same name
                                            // 'a' append to existing file, creates a new file if file does not exist
                                            // 'n' adds a number to file name. If file name of that number exists creates a file with next highest number added to name

        // Constructors

        public FileOutput(String filename, char app){
                this.filename = filename;
                this.app =app;

                if(this.app == 'n'){
                        boolean test = true;
                        int i = 0;
                        BufferedReader input;
                        String ext = "";
                        String filename0 = "";

                        int idot=filename.indexOf('.');
                        if(idot!=-1){
                                ext += filename.substring(idot);
                                filename0 += filename.substring(0,idot);
                        }
                        else{
                                filename0 += filename;
                        }

                        while(test){
                                i++;
                                filename=filename0+String.valueOf(i)+ext;
                                try{
                                        input = new BufferedReader(new FileReader(filename));
                                }catch(FileNotFoundException e){
                                        test=false;
                                        this.filename=filename;
                                }
                        }
                }

                if(this.app == 'a'){
                        this.append=true;
                }
                else{
                        this.append = false;
                }
                try{
                        fwoutput = new FileWriter(filename, this.append);
                }
                catch(IOException e){
                        System.out.println(e);
                }

                output = new PrintWriter(new BufferedWriter(fwoutput));
        }

        public FileOutput(String filename, String apps){
                this.filename = filename;
                this.app = apps.charAt(0);

                if(this.app == 'n'){
                        boolean test = true;
                        int i = 0;
                        BufferedReader input;
                        String ext = "";
                        String filename0 = "";

                        int idot=filename.indexOf('.');
                        if(idot!=-1){
                                ext += filename.substring(idot);
                                filename0 += filename.substring(0,idot);
                        }
                        else{
                                filename0 += filename;
                        }

                        while(test){
                                i++;
                                filename=filename0+String.valueOf(i)+ext;
                                try{
                                        input = new BufferedReader(new FileReader(filename));
                                }catch(FileNotFoundException e){
                                        test=false;
                                        this.filename=filename;
                                }
                        }
                }

                if(this.app == 'a'){
                this.append=true;
                }else{
                        this.append = false;
                }
                try{
                        fwoutput = new FileWriter(filename, this.append);
                }catch(IOException e){
                        System.out.println(e);
                }

                output = new PrintWriter(new BufferedWriter(fwoutput));
        }

        public FileOutput(String filename){
                this.filename = filename;
                this.app = 'w';
                if(this.app == 'a'){
                        this.append=true;
                }else{
                        this.append = false;
                }
                try{
                        fwoutput = new FileWriter(filename, this.append);
                }catch(IOException e){
                        System.out.println(e);
                }
                output = new PrintWriter(new BufferedWriter(fwoutput));
        }

        // Methods

        // PRINT WITH NO FOLLOWING SPACE OR CHARACTER AND NO LINE RETURN

        // Prints character, no line return
        public final synchronized void print(char ch){
                output.print(ch);
        }

        // Prints character, no line return, fixed field length
        public final synchronized void print(char ch, int f){
                String ss ="";
                ss = ss + ch;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints string, no line return
        public final synchronized void print(String word){
                output.print(word);
        }


        // Prints string, no line return, fixed field length
        public final synchronized void print(String word, int f){
                String ss ="";
                ss = ss + word;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }
        // Prints double, no line return
        public final synchronized void print(double dd){
                output.print(dd);
        }

        // Prints double, no line return, fixed field length
        public final synchronized void print(double dd, int f){
                String ss ="";
                ss = ss + dd;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints float, no line return
        public final synchronized void print(float ff){
                output.print(ff);
        }

        // Prints float, no line return, fixed field length
        public final synchronized void print(float ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints Complex, no line return
        public final synchronized void print(Complex ff){
                output.print(ff.toString());
        }

        // Prints Complex, no line return, fixed field length
        public final synchronized void print(Complex ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints ErrorProp, no line return
        public final synchronized void print(ErrorProp ff){
                output.print(ff.toString());
        }

        // Prints ErrorProp, no line return, fixed field length
        public final synchronized void print(ErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints ComplexErrorProp, no line return
        public final synchronized void print(ComplexErrorProp ff){
                output.print(ff.toString());
        }

        // Prints ComplexErrorProp, no line return, fixed field length
        public final synchronized void print(ComplexErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints int, no line return
        public final synchronized void print(int ii){
                output.print(ii);
        }

        // Prints int, no line return, fixed field length
        public final synchronized void print(int ii, int f){
                String ss ="";
                ss = ss + ii;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints long integer, no line return
        public final synchronized void print(long ll){
                output.print(ll);
        }

        // Prints long integer, no line return, fixed field length
        public final synchronized void print(long ll, int f){
                String ss ="";
                ss = ss + ll;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints boolean, no line return
        public final synchronized void print(boolean bb){
                output.print(bb);
        }

        // Prints boolean, no line return, fixed field length
        public final synchronized void print(boolean bb, int f){
                String ss ="";
                ss = ss + bb;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
        }

        // Prints date and time  (no line return);
        public final synchronized void dateAndTime(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
        }

        // Prints file title (title), date and time  (no line return);
        public final synchronized void dateAndTime(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
        }

        // PRINT WITH SPACE (NO LINE RETURN)
        // Prints character plus space, no line return
        public final synchronized void printsp(char ch){
                output.print(ch);
                output.print(" ");
        }

        // Prints string plus space, no line return
        public final synchronized void printsp(String word){
                output.print(word + " ");
        }

        // Prints double plus space, no line return
        public final synchronized void printsp(double dd){
                output.print(dd);
                output.print(" ");
        }

        // Prints float plus space, no line return
        public final synchronized void printsp(float ff){
                output.print(ff);
                output.print(" ");
        }

        // Prints Complex plus space, no line return
        public final synchronized void printsp(Complex ff){
                output.print(ff.toString());
                output.print(" ");
        }

        // Prints ErrorProp plus space, no line return
        public final synchronized void printsp(ErrorProp ff){
                output.print(ff.toString());
                output.print(" ");
        }

        // Prints ComplexErrorProp plus space, no line return
        public final synchronized void printsp(ComplexErrorProp ff){
                output.print(ff.toString());
                output.print(" ");
        }

        // Prints int plus space, no line return
        public final synchronized void printsp(int ii){
                output.print(ii);
                output.print(" ");
        }

        // Prints long integer plus space, no line return
        public final synchronized void printsp(long ll){
                output.print(ll);
                output.print(" ");
        }

        // Prints boolean plus space, no line return
        public final synchronized void printsp(boolean bb){
                output.print(bb);
                output.print(" ");
        }

        // Prints  space, no line return
        public final synchronized void printsp(){
                output.print(" ");
        }

        // Prints date and time (plus space, no line return);
        public final synchronized void dateAndTimesp(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(" ");
        }

        // Prints file title (title), date and time  (no line return);
        public final synchronized void dateAndTimesp(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(" ");
        }

        // PRINT WITH LINE RETURN
        // Prints character with line return
        public final synchronized void println(char ch){
                output.println(ch);
        }

        // Prints string with line return
        public final synchronized void println(String word){
                output.println(word);
        }

        // Prints double with line return
        public final synchronized void println(double dd){
                output.println(dd);
        }

        // Prints double with line return, fixed field length
        public final synchronized void println(double dd, int f){
                output.print(dd);
                String ss ="";
                ss = ss + dd;
                char sp =  ' ';
                int n = ss.length();
                if(f>n){
                    for(int i=n+1; i<=f; i++){
                        ss=ss+sp;
                    }
                }
                output.println(ss);
        }

        // Prints float with line return
        public final synchronized void println(float ff){
                output.println(ff);
        }

        // Prints Complex with line return
        public final synchronized void println(Complex ff){
                output.println(ff.toString());
        }

        // Prints ErrorProp with line return
        public final synchronized void println(ErrorProp ff){
                output.println(ff.toString());
        }

        // Prints ComplexErrorProp with line return
        public final synchronized void println(ComplexErrorProp ff){
                output.println(ff.toString());
        }

        // Prints int with line return
        public final synchronized void println(int ii){
                output.println(ii);
        }

        // Prints long integer with line return
        public final synchronized void println(long ll){
                output.println(ll);
        }

        // Prints boolean with line return
        public final synchronized void println(boolean bb){
                output.println(bb);
        }

        // Prints  line return
        public final synchronized void println(){
                output.println("");
        }

        // Prints date and time as date-month-year hour:minute:second (with line return);
        public final synchronized void dateAndTimeln(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+this.filename+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.println(day);
        }

        // Prints file title (title), date and time (with line return);
        public final synchronized void dateAndTimeln(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.println(day);
        }

        // PRINT WITH FOLLOWING TAB, NO LINE RETURN
        // Prints character plus tab, no line return
        public final synchronized void printtab(char ch){
                output.print(ch);
                output.print("\t");
        }

        // Prints character plus tab, no line return, fixed field length
        public final synchronized void printtab(char ch, int f){
                String ss ="";
                ss = ss + ch;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints string plus tab, no line return
        public final synchronized void printtab(String word){
                output.print(word + "\t");
        }

        // Prints string plus tab, no line return, fixed field length
        public final synchronized void printtab(String word, int f){
            String ss ="";
                ss = ss + word;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints double plus tab, no line return
        public final synchronized void printtab(double dd){
                output.print(dd);
                output.print("\t");
        }

        // Prints double plus tab, fixed field length, fixed field length
        public final synchronized void printtab(double dd, int f){
                String ss ="";
                ss = ss + dd;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints float plus tab, no line return
        public final synchronized void printtab(float ff){
                output.print(ff);
                output.print("\t");
        }

        // Prints float plus tab, no line return, fixed field length
        public final synchronized void printtab(float ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints Complex plus tab, no line return
        public final synchronized void printtab(Complex ff){
                output.print(ff.toString());
                output.print("\t");
        }

        // Prints Complex plus tab, no line return, fixed field length
        public final synchronized void printtab(Complex ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints ErrorProp plus tab, no line return
        public final synchronized void printtab(ErrorProp ff){
                output.print(ff.toString());
                output.print("\t");
        }

        // Prints ErrorProp plus tab, no line return, fixed field length
        public final synchronized void printtab(ErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints ComplexErrorProp plus tab, no line return
        public final synchronized void printtab(ComplexErrorProp ff){
                output.print(ff.toString());
                output.print("\t");
        }

        // Prints ComplexErrorProp plus tab, no line return, fixed field length
        public final synchronized void printtab(ComplexErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints int plus tab, no line return
        public final synchronized void printtab(int ii){
                output.print(ii);
                output.print("\t");
        }

        // Prints int plus tab, no line return, fixed field length
        public final synchronized void printtab(int ii, int f){
               String ss ="";
                ss = ss + ii;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints long integer plus tab, no line return
        public final synchronized void printtab(long ll){
                output.print(ll);
                output.print("\t");
        }

        // Prints long integer plus tab, no line return, fixed field length
        public final synchronized void printtab(long ll, int f){
               String ss ="";
                ss = ss + ll;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints boolean plus tab, no line return
        public final synchronized void printtab(boolean bb){
                output.print(bb);
                output.print("\t");
        }

        // Prints boolean plus tab, no line return, fixed field length
        public final synchronized void printtab(boolean bb, int f){
                String ss ="";
                ss = ss + bb;
                ss = FileOutput.setField(ss,f);
                output.print(ss);
                output.print("\t");
        }

        // Prints tab, no line return
        public final synchronized void printtab(){
                output.print("\t");
        }

        // Prints date and time (plus tab, no line return);
        public final synchronized void dateAndTimetab(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print("\t");
        }

        // Prints file title (title), date and time (plus tab, no line return);
        public final synchronized void dateAndTimetab(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print("\t");
        }

        // PRINT FOLLOWED BY A COMMA, NO LINE RETURN
        // Prints character plus comma, no line return
        public final synchronized void printcomma(char ch){
                output.print(ch);
                output.print(",");
        }

        // Prints string plus comma, no line return
        public final synchronized void printcomma(String word){
                output.print(word + ",");
        }

        // Prints double plus comma, no line return
        public final synchronized void printcomma(double dd){
                output.print(dd);
                output.print(",");
        }

        // Prints float plus comma, no line return
        public final synchronized void printcomma(float ff){
                output.print(ff);
                output.print(",");
        }

        // Prints Complex plus comma, no line return
        public final synchronized void printcomma(Complex ff){
                output.print(ff.toString());
                output.print(",");
        }

        // Prints ErrorProp plus comma, no line return
        public final synchronized void printcomma(ErrorProp ff){
                output.print(ff.toString());
                output.print(",");
        }

        // Prints ComplexErrorProp plus comma, no line return
        public final synchronized void printcomma(ComplexErrorProp ff){
                output.print(ff.toString());
                output.print(",");
        }

        // Prints int plus comma, no line return
        public final synchronized void printcomma(int ii){
                output.print(ii);
                output.print(",");
        }

        // Prints long integer plus comma, no line return
        public final synchronized void printcomma(long ll){
                output.print(ll);
                output.print(",");
        }

        // Prints boolean plus comma, no line return
        public final synchronized void printcomma(boolean bb){
                output.print(bb);
                output.print(",");
        }

        // Prints comma, no line return
        public final synchronized void printcomma(){
                output.print(",");
        }

        // Prints date and time (plus comma, no line return);
        public final synchronized void dateAndTimecomma(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(",");
        }

        // Prints file title (title), date and time (plus comma, no line return);
        public final synchronized void dateAndTimecomma(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(",");
        }

        // PRINT FOLLOWED BY A SEMICOLON, NO LINE RETURN
        // Prints character plus semicolon, no line return
        public final synchronized void printsc(char ch){
                output.print(ch);
                output.print(";");
        }

        // Prints string plus semicolon, no line return
        public final synchronized void printsc(String word){
                output.print(word + ";");
        }

        // Prints double plus semicolon, no line return
        public final synchronized void printsc(double dd){
                output.print(dd);
                output.print(";");
        }

        // Prints float plus semicolon, no line return
        public final synchronized void printsc(float ff){
                output.print(ff);
                output.print(";");
        }

        // Prints Complex plus semicolon, no line return
        public final synchronized void printsc(Complex ff){
                output.print(ff.toString());
                output.print(";");
        }

        // Prints ErrorProp plus semicolon, no line return
        public final synchronized void printsc(ErrorProp ff){
                output.print(ff.toString());
                output.print(";");
        }

        // Prints ComplexErrorProp plus semicolon, no line return
        public final synchronized void printsc(ComplexErrorProp ff){
                output.print(ff.toString());
                output.print(";");
        }

        // Prints int plus semicolon, no line return
        public final synchronized void printsc(int ii){
                output.print(ii);
                output.print(";");
        }

        // Prints long integer plus semicolon, no line return
        public final synchronized void printsc(long ll){
                output.print(ll);
                output.print(";");
        }

        // Prints boolean plus semicolon, no line return
        public final synchronized void printsc(boolean bb){
                output.print(bb);
                output.print(";");
        }

        // Prints  semicolon, no line return
        public final synchronized void printsc(){
                output.print(";");
        }

        // Prints date and time (plus semicolon, no line return);
        public final synchronized void dateAndTimesc(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(";");
        }

        // Prints file title (title), date and time (plus semicolon, no line return);
        public final synchronized void dateAndTimesc(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                output.print("This file, "+title+", was created at ");
                output.print(tim);
                output.print(" on ");
                output.print(day);
                output.print(";");
        }

        // Close file
        public final synchronized void close(){
                output.close();
        }

        // Print a 2-D array of doubles to a text file
        public static void printArrayToText(double[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("double array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of doubles to a text file
        public static void printArrayToText(double[] array){
            double[][] newArray = new double[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of ints to a text file
        public static void printArrayToText(int[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("int array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of ints to a text file
        public static void printArrayToText(int[] array){
            int[][] newArray = new int[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of longs to a text file
        public static void printArrayToText(long[][] array){
            FileOutput fo = new FileOutput("arrayToText.txt", 'n');
            fo.dateAndTimeln("long array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of longs to a text file
        public static void printArrayToText(long[] array){
            long[][] newArray = new long[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of Complex to a text file
        public static void printArrayToText(Complex[][] array){
            FileOutput fo = new FileOutput("arrayToText.txt", 'n');
            fo.dateAndTimeln("Complex array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of Complex to a text file
        public static void printArrayToText(Complex[] array){
            Complex[][] newArray = new Complex[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of chars to a text file
        public static void printArrayToText(char[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("char array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of chars to a text file
        public static void printArrayToText(char[] array){
            char[][] newArray = new char[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of Strings to a text file
        public static void printArrayToText(String[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("String array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of Strings to a text file
        public static void printArrayToText(String[] array){
            String[][] newArray = new String[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of floats to a text file
        public static void printArrayToText(float[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("float array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of floats to a text file
        public static void printArrayToText(float[] array){
            float[][] newArray = new float[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of ErrorProp to a text file
        public static void printArrayToText(ErrorProp[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("Array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of ErrorProp to a text file
        public static void printArrayToText(ErrorProp[] array){
            ErrorProp[][] newArray = new ErrorProp[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        // Print a 2-D array of ComplexErrorProp to a text file
        public static void printArrayToText(ComplexErrorProp[][] array){
            FileOutput fo = new FileOutput("ArrayToText.txt", 'n');
            fo.dateAndTimeln("Array to text");
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of ComplexErrorProp to a text file
        public static void printArrayToText(ComplexErrorProp[] array){
            ComplexErrorProp[][] newArray = new ComplexErrorProp[1][array.length];
            FileOutput.printArrayToText(newArray);
        }

        private static String setField(String ss, int f){
             char sp =  ' ';
                int n = ss.length();
                if(f>n){
                    for(int i=n+1; i<=f; i++){
                        ss=ss+sp;
                    }
                }
                return ss;
        }

}
