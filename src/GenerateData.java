/**
 * @author ssatapathy
 */

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class GenerateData {

	private static int count=1;

    public static void main (String[] args) throws IOException, XmlPullParserException {

		File fileDir=new File("E:/Dev/MLData/rawdata");
		File[] files=fileDir.listFiles();
		for(File file:files){
			FileInputStream stream=new FileInputStream(file);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(stream, null);
		    processXml(parser);
		}

    }

  
    
	public static void processXml(XmlPullParser parser) throws XmlPullParserException, IOException{
		try{
			int eventType=parser.next();
			while(eventType!=XmlPullParser.END_DOCUMENT){
				if((eventType==XmlPullParser.START_TAG) && (parser!=null)){
					if((parser.getName().equalsIgnoreCase("events"))){
						String content=parser.nextText();
						File newfile = new File("E:/Dev/MLData/processed/events/file"+count+".txt");
						Writer output = new BufferedWriter(new FileWriter(newfile));
						output.write(content);
						count++;
						output.close();
					}
				}
				eventType=parser.next();
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}
			
	}

}
