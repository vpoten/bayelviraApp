package elvira.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * This class implements a parser for command line arguments.
 * 
 * @author dalgaard
 *
 */
public class CmdLineArguments {
	
	public static enum argumentType {b, i, d, s, l};
	
	private static class argument{
		
		public final String name;
		public String val;
		public final argumentType type;
		public final String defaultValue;
		public final String description;
		public int numOccurances = 0;
		public argument(String nam, argumentType typ, String defval, String desc){
			name = new String(nam);
			type = typ;
			defaultValue = new String(defval);
			val = new String(defval);
			description = new String(desc);
		}
		
		public void printHelp(){
			System.out.print(name+"\t");
			switch(type){
			case b:
				System.out.print("<boolean ("+defaultValue+")>\t"+Boolean.parseBoolean(val));
				break;
			case i:
				System.out.print("<integer ("+defaultValue+")>\t"+Integer.parseInt(val));
				break;
			case d:
				System.out.print("<double ("+defaultValue+")>\t"+Double.parseDouble(val));
				break;
			case s:
				System.out.print("<string ("+defaultValue+")>\t"+val);
				break;
			case l:
				System.out.print("<long ("+defaultValue+")>\t"+val);
				break;
			}

			System.out.println("\n"+description);
			System.out.println("----------");
		}
		
		private void checkNewValue(String val){
			switch(type){
			case b :
				Boolean.parseBoolean(val);
				break;
			case d :
				Double.parseDouble(val);
				break;
			case i :
				Integer.parseInt(val);
				break;
			case l:
				Long.parseLong(val);
				break;
			}
		}
		
		public boolean getBooleanValue() throws argumentException {
			if(type != argumentType.b) throw new argumentException("argument '"+name+"' not of type boolean!\n");
			return Boolean.parseBoolean(val);
		}
		
		public int getIntValue() throws argumentException {
			if(type != argumentType.i) throw new argumentException("argument '"+name+"' not of type integer!\n");
			return Integer.parseInt(val);
		}

		public long getLongValue() throws argumentException {
			if(type != argumentType.l) throw new argumentException("argument '"+name+"' not of type long!\n");
			return Long.parseLong(val);
		}
		
		public double getDoubleValue() throws argumentException {
			if(type != argumentType.d) throw new argumentException("argument '"+name+"' not of type double!\n");
			return Double.parseDouble(val);
		}
		
		public String getStringValue() throws argumentException {
			if(type != argumentType.s)  throw new argumentException("argument '"+name+"' not of type string!\n");
			return new String(val);
		}
		
		public void setVal(String v) throws argumentException {
			numOccurances++;
			if(numOccurances > 1){
				System.out.println("warning: argument '"+name+"' has been set "+numOccurances+" times!\n");
			}
			try{
				checkNewValue(v);
			} catch(Exception e){
				e.printStackTrace();
				throw new argumentException("could not set value '"+v+"' for argument of type "+type+"!\n");
			}
			this.val = new String(v);
		}
	}

	private static class argumentException extends Exception{
		public argumentException(String msg){
			super(msg);
		}
	}
	
	private final HashMap<String, argument> map;
	
	public CmdLineArguments(){
		 map = new HashMap<String, argument>();
	}
	
	private final argument getArgument(String n) throws CmdLineArgumentsException {
		argument a = null;
		Iterator<Entry<String,argument>> argItr = map.entrySet().iterator();
		while(argItr.hasNext()){
			Entry<String, argument> entry = argItr.next();
			if(n.equals(entry.getKey())){
				a = entry.getValue();
				break;
			}
		}
		if(a==null){
			throw new CmdLineArgumentsException("Unknown argument '"+n+"' !!\n");
		}
		return a;
	}
	
	public void parseArguments(String args[]) throws CmdLineArgumentsException{		
		String key, val;
		int i=0;
		while(i<args.length){
			key = args[i++];
			argument a = null;
			try{
				a = getArgument(key);
			} catch(CmdLineArgumentsException e){
				System.err.println(e.getMessage());
				printHelp();
				System.exit(1);
			}
			if(!(i < args.length)){
				System.err.println("missing value for argument '"+key+"'");
				printHelp();
				System.exit(1);
			}
			val = args[i++];
			try{
				a.setVal(val);
			} catch(argumentException e){
				throw new CmdLineArgumentsException("wrong type for argument '"+a.name+"'");
			}
		}
	}
	
	private void addArgument(argument arg) throws argumentException {
		if(map.put(arg.name, arg) != null) throw new argumentException("argument '"+arg.name+"' already has been specified!");
	}
	
	public void addArgument(String name, argumentType type, String defaultValue, String description) throws CmdLineArgumentsException{
		try {
			addArgument(new argument(name, type, defaultValue, description));
		} catch (argumentException e) {
			throw new CmdLineArgumentsException(e.getMessage());
		}
	}
	
	//public boolean getBooleanArgument(argument arg) throws argumentException{
	//	if(arg.type != argumentType.b){
	//		throw new argumentException("argument '"+arg.name+"' not of type boolean!");
	//	}
	//	return Boolean.parseBoolean(arg.val);
	//}
	
	//private int getKeyIndex(String key){
	//	int i;
	//	for (i=0;i<keys.length;i++){
	//		if(keys[i].compareToIgnoreCase(key) == 0) break;
	//	}
	//	return i;
	//}
	
	public boolean getBoolean(String key) throws CmdLineArgumentsException {
		boolean b = false;
		try{
			argument a = getArgument(key);
			b = a.getBooleanValue();
		} catch(argumentException ae){
			throw new CmdLineArgumentsException(ae.getMessage());
		}
		return b;
	}
	
	public int getInteger(String key) throws CmdLineArgumentsException{
		int i = 0;
		try{
			argument a = getArgument(key);
			i = a.getIntValue();
		} catch(argumentException ae){
			throw new CmdLineArgumentsException(ae.getMessage());
		}
		return i;
	}

	public long getLong(String key) throws CmdLineArgumentsException{
		long i = 0;
		try{
			argument a = getArgument(key);
			i = a.getLongValue();
		} catch(argumentException ae){
			throw new CmdLineArgumentsException(ae.getMessage());
		}
		return i;
	}

	
	public double getDouble(String key) throws CmdLineArgumentsException{
		double d = 0;
		try{
			argument a = getArgument(key);
			d = a.getDoubleValue();
		} catch(argumentException ae){
			throw new CmdLineArgumentsException(ae.getMessage());
		}
		return d;
	}
	
	public String getString(String key) throws CmdLineArgumentsException{
		String s = "";
		try{
			argument a = getArgument(key);
			s = a.getStringValue();
		} catch(argumentException ae){
			throw new CmdLineArgumentsException(ae.getMessage());
		}
		return s;
	}
	
	//public long getLong(String key, long defaultVal){
	//	int i = getKeyIndex(key);
	//	return ((i >= vals.length) ? defaultVal : Long.parseLong(vals[i]));	
	//}

	public void print(){
		System.out.println("key : val\n" +
							"==========");
		Iterator<Entry<String, argument>> entries = map.entrySet().iterator();
		while(entries.hasNext()){
			argument a = entries.next().getValue();
			System.out.println(a.name+" : "+a.val);
		}
	}
	
	public void printHelp(){
		System.out.println("Possible Arguments:");
		Iterator<Entry<String, argument>> entries = map.entrySet().iterator();
		while(entries.hasNext()){
			argument a = entries.next().getValue();
			a.printHelp();
		}
	}
	
	public class CmdLineArgumentsException extends Exception{
		private static final long serialVersionUID = -8955826831356576728L;
		public CmdLineArgumentsException(String msg){
			super(msg);			
		}
	}
	
	public static void main(String argv[]) throws Exception {
		CmdLineArguments params = new CmdLineArguments();
		params.addArgument(new argument("intArg", argumentType.i, "0", "An integer argument."));
		params.addArgument(new argument("doubleArg", argumentType.d, "0.0", "A double argument."));
		params.addArgument(new argument("booleanArg", argumentType.b, "false", "A boolean argument."));
		params.addArgument(new argument("stringArg", argumentType.s, "default", "A string argument."));
		params.parseArguments(argv);
		params.printHelp();
	}
}
