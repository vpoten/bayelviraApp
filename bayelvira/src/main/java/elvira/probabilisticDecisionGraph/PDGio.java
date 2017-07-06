package elvira.probabilisticDecisionGraph;

import java.io.*;

/**
 * @author dalgaard
 *
 */
public class PDGio {

	/**
	 * This method loads a PDG from a text-file containing a PDG defenition.
	 * 
	 * @param filename - the file containing the PDG describtion
	 * @return The PDG object that was loaded from the file
	 * @throws ParseException - if the file can not be parsed as a valid PDG file.
	 * @throws FileNotFoundException - if the file can not be found.
	 */
	public static PDG load(String filename) throws ParseException, FileNotFoundException, PDGException {
		PDG retval;
		FileInputStream f = new FileInputStream(filename);
		PDGParse p = new PDGParse(f);
		retval = p.parse();
		return retval;
	}
	
	public static void save(PDG myPdg, String filename) throws IOException{
		String str = myPdg.toString();
		FileWriter fw = new FileWriter(filename);
		fw.write(str);
		fw.close();
	}
	
	public static void main(String argv[]) throws FileNotFoundException, ParseException, IOException, PDGException {
		if(argv.length == 0){
			System.out.println("please specify a file for reading a pdg specification");
		}
		PDG p = load(argv[0]);
		System.out.println("// PDG loaded from file '"+argv[0]+"':");
		System.out.print(p.toString());
	}
}
