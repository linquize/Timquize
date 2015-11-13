package hk.linquize.timquize;

import java.io.*;

class FileUtil {
	public static void appendAllText(String asPath, String asContent) throws IOException {
		FileWriter loWriter = new FileWriter(asPath, true);
	    try {
	    	loWriter.write(asContent);
	    }
	    finally {
	    	loWriter.close();
	    }
	}
	
	public static String readAllText(String asPath) throws IOException {
		FileReader loReader = new FileReader(asPath);
		StringBuilder loSB = new StringBuilder();
		char[] loBuf = new char[4096];
		int liRead = 0;		
		try {
			do {
				liRead = loReader.read(loBuf);
				loSB.append(loBuf, 0, liRead);
			} while (liRead < 4096);
			return loSB.toString();
		}
		finally {
			loReader.close();
		}
	}
	
	public static void writeAllText(String asPath, String asContent) throws IOException {
		FileWriter loWriter = new FileWriter(asPath);
	    try {
	    	loWriter.write(asContent);
	    }
	    finally {
	    	loWriter.close();
	    }
	}
}