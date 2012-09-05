/**
 * @author ssatapathy
 */

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import org.jsoup.Jsoup;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.aliasi.util.Files;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.googleapis.ajax.common.PagedList;
import com.googleapis.ajax.schema.WebResult;
import com.googleapis.ajax.services.GoogleSearchQueryFactory;
import com.googleapis.ajax.services.WebSearchQuery;

import weka.classifiers.trees.REPTree;
import weka.core.converters.CSVLoader;
import weka.core.Instances;
import weka.core.Utils;



public class HangoutRecommender {

	public static ArrayList<ArrayList<CheckIn>> checkIns;
	public static ArrayList<ArrayList<PhotoAlbum>> photoAlbums;
	public static ArrayList<ArrayList<Music>> music;
	public static ArrayList<ArrayList<Books>> books;
	public static ArrayList<ArrayList<Movies>> movies;
	public static ArrayList<ArrayList<Event>> events;
	public static ArrayList<String> gender;
	public static ArrayList<Integer> age;
	public static ArrayList<String> locations;
	public static int avgAge;
	
	static HashMap<String, Integer> map=new HashMap<String, Integer>();
	static HashSet<String> spot=new HashSet<String>();
	static HashSet<String> loc=new HashSet<String>();
	//static HashMap<String, Integer> locToIntMap=new HashMap<String, Integer>();
	
	static HashSet<String> unqMovies=new HashSet<String>();
	static HashSet<String> unqMusic=new HashSet<String>();
	static HashSet<String> unqBooks=new HashSet<String>();
	static HashSet<String> unqEvents=new HashSet<String>();
	
	static REPTree tree = new REPTree();
	
	static GoogleSearchQueryFactory factory = GoogleSearchQueryFactory.newInstance("applicationKey");
	
	
	static class LocToIntMap{
		String loc;
		int label;
		LocToIntMap(String s, int l){
			this.loc=s;
			this.label=l;
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<String> classLabels=new ArrayList<String>();		
		checkIns=new ArrayList<ArrayList<CheckIn>>();
		photoAlbums=new ArrayList<ArrayList<PhotoAlbum>>();
		movies=new ArrayList<ArrayList<Movies>>();
		music=new ArrayList<ArrayList<Music>>();
		books=new ArrayList<ArrayList<Books>>();
		events=new ArrayList<ArrayList<Event>>();
		gender=new ArrayList<String>();
		locations=new ArrayList<String>();
		age=new ArrayList<Integer>();

		//Uncomment API calls to run corresponding function..
		
		//processSocialData();			// This processes user data collected from Google App Engine to compile training data
		buildClassifier(classLabels);	// This builds the Classification and Regression Tree model
		//createTestData();				// This can be used to create test examples
		generateRecommendations(classLabels); // This generates top 5 hangout recommendations

	}
	
	
	private static void generateRecommendations(ArrayList<String> classLabels) {
		try{
			ArrayList<String> recommendations=new ArrayList<String>();
			CSVLoader loaderTest = new CSVLoader();
			loaderTest.setSource(new File("E:/Dev/MLData/userdata_test.csv"));       //Set appropriate test example file
			Instances testdata = loaderTest.getDataSet();
			testdata.setClassIndex(testdata.numAttributes()-1);
			for (int i = testdata.numInstances()-1; i >= 0 ; i--) {
				double[] dist = tree.distributionForInstance(testdata.instance(i));
				int pred=Utils.maxIndex(dist);
				//System.out.print(testdata.instance(i).toString(testdata.classIndex()) + " - ");
				if(!recommendations.contains(classLabels.get((int)pred))){
					recommendations.add(classLabels.get((int)pred));
				}
			}
			
			if(recommendations.size()>5){
				for(int i=0;i<5;i++){
					System.out.println(recommendations.get(i));
				}
			}else{
				for(String rec:recommendations){
					System.out.println(rec);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}


	private static void buildClassifier(ArrayList<String> classLabels) {
		try{
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File("E:/Dev/MLData/userdata.csv"));			//Set appropriate training data
			Instances data = loader.getDataSet();
			data.setClassIndex(data.numAttributes()-1);
			tree.setNoPruning(true);
			tree.buildClassifier(data);
		    
			Enumeration<String> en=data.classAttribute().enumerateValues();
		    int k=0;
		    while(en.hasMoreElements()){
		    	String place= en.nextElement();
		        classLabels.add(place);
		        System.out.println(place);
		        k++;
		    }
			
			
			System.out.println(tree.toString());
			System.out.println();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}


	private static void processSocialData() {
		try{
			
			//location
			File dir=new File("E:/Dev/MLData/processed/location");
			File[] files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str.indexOf("'")!=-1){
					str=str.replace("'", "");
				}
				if(str.indexOf("\"")!=-1){
					str=str.replace("\"", "");
				}
				if(str.contains(",")){
					String[] strArr=str.split(",");
					locations.add(strArr[0]+" "+strArr[1]);
				}
				else{
					locations.add(str);
				}
				
			}
			
			//checkins
			dir=new File("E:/Dev/MLData/processed/checkins");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
			    if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
			    	ArrayList<CheckIn> checkIn=extractCheckIn(json);
				    //if(checkIn.getLikes()>=0 && checkIn.getName()!=null && checkIn.getCity()!=null){
				    	checkIns.add(checkIn);
				    //}
				   

				    	
			    }else{
			    	checkIns.add(new ArrayList<CheckIn>());
			    }
			    
			    

			}
			
			
			//albums
			dir=new File("E:/Dev/MLData/processed/albums");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
			    if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
				    ArrayList<PhotoAlbum> photoAlbum=extractPhotoAlbum(json);
				    photoAlbums.add(photoAlbum);

			    }else{
			    	photoAlbums.add(new ArrayList<PhotoAlbum>());
			    }
			}
			
			//gender
			dir=new File("E:/Dev/MLData/processed/gender");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str!=null && !str.equals(""))gender.add(str);
				else gender.add("");
				

			}
			
			//music
			dir=new File("E:/Dev/MLData/processed/music");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
				    ArrayList<Music> moozik=extractMusic(json);
				    music.add(moozik);

				}
				else{
					music.add(new ArrayList<Music>());
				}
			}
			
			//books
			dir=new File("E:/Dev/MLData/processed/books");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
				    ArrayList<Books> buks=extractBooks(json);
				    books.add(buks);
				    
				}
				else{
					books.add(new ArrayList<Books>());
				}
			}
			
			//movies
			dir=new File("E:/Dev/MLData/processed/movies");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
				    ArrayList<Movies> muveez=extractMovies(json);
				    movies.add(muveez);
				}
				else{
					movies.add(new ArrayList<Movies>());
				}
			}
			
			//events
			dir=new File("E:/Dev/MLData/processed/events");
			files=dir.listFiles();
			for(File file:files){
				String str = Files.readFromFile(file,"ISO-8859-1");
				if(str!=null && !str.equals("")){
			    	char[] charStr=str.toCharArray();
			    	if(charStr[2]!='d'){
			    		str=str.substring(str.indexOf(":")+1, str.length()-1);
			    	}
			    	JSONObject json = (JSONObject) JSONSerializer.toJSON( str );
				    ArrayList<Event> eventz=extractEvents(json);
				    events.add(eventz);
				}
				else{
					events.add(new ArrayList<Event>());
				}
			}
			
			//birthdate
			dir=new File("E:/Dev/MLData/processed/birthdate");
			files=dir.listFiles();
			for(File file:files){
				 String str = Files.readFromFile(file,"ISO-8859-1");
				 if(str!=null && !str.equals("") && !str.equals("undefined")){
					 String dob[]=str.split("/");
					 if(dob.length==3){
						 age.add(2011-Integer.parseInt(dob[2]));
					 }else{
						 age.add(0);
					 }
				 }else{
					 age.add(0);
				 }
			}
			
			int sumAge=0;
			for(Integer a:age){
				sumAge=sumAge+a;
			}
			avgAge=sumAge/age.size();
			System.out.println();
			
			generateUnqFeatures(movies,music,books,events);
			//readAndProcessCSVForSVM();
			
			writeToCsv();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}


	private static void generateUnqFeatures(
			ArrayList<ArrayList<Movies>> movies,
			ArrayList<ArrayList<Music>> music,
			ArrayList<ArrayList<Books>> books, ArrayList<ArrayList<Event>> events) {

		for(ArrayList<Movies> mov:movies){
			for(Movies mv:mov){
				String[] strArr=null;
				if(mv.getName().toLowerCase().contains(",")){
					strArr=mv.getName().toLowerCase().split(",");
					String str="";
					for(int i=0;i<strArr.length;i++){
						str=str+strArr[i]+" ";
					}
					unqMovies.add(str+" movie");
				}
				else unqMovies.add(mv.getName().toLowerCase()+" movie");
			}
		}
		
		for(ArrayList<Music> mus:music){
			for(Music ms:mus){
				String[] strArr=null;
				if(ms.getName().toLowerCase().contains(",")){
					strArr=ms.getName().toLowerCase().split(",");
					String str="";
					for(int i=0;i<strArr.length;i++){
						str=str+strArr[i]+" ";
					}
					unqMusic.add(str+" music");
				}
				else unqMusic.add(ms.getName().toLowerCase()+" music");
			}
		}
		
		for(ArrayList<Books> bookz:books){
			for(Books bks:bookz){
				String[] strArr=null;
				if(bks.getName().toLowerCase().contains(",")){
					strArr=bks.getName().toLowerCase().split(",");
					String str="";
					for(int i=0;i<strArr.length;i++){
						str=str+strArr[i]+" ";
					}
					unqBooks.add(str+" book");
				}
				else unqBooks.add(bks.getName().toLowerCase()+" book");
			}
		}
		
		for(ArrayList<Event> evs:events){
			for(Event ev:evs){
				String[] strArr=null;

					System.out.println(ev.getName());
					if(ev.getName().toLowerCase().contains(",")){
						strArr=ev.getName().toLowerCase().split(",");
						String str="";
						for(int i=0;i<strArr.length;i++){
							str=str+strArr[i]+" ";
						}
						unqEvents.add(str+" event");
					}
					else unqEvents.add(ev.getName().toLowerCase()+" event");

			}
		}
	}
	
	private static ArrayList<CheckIn> extractCheckIn(JSONObject json) {
		ArrayList<CheckIn> checkIns=new ArrayList<CheckIn>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	CheckIn checkIn=new CheckIn();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	JSONObject likes=jObj.getJSONObject("likes");
		    	if(likes!=null && !likes.toString().equals("null")){	
		    		checkIn.setLikes(likes.getJSONArray("data").size());
		    	}
		    	JSONObject comments=jObj.getJSONObject("comments");
		    	if(comments!=null && !comments.toString().equals("null")){
		    		checkIn.setComments(comments.getJSONArray("data").size());
		    	}
		    	checkIn.setTimeStamp(jObj.getString("created_time"));
		    	
		    	JSONObject place=jObj.getJSONObject("place");
		    	if(place!=null && place.getString("name")!=null){
		    		String str=place.getString("name").toLowerCase();
		    		if(str.contains(",")){
		    			String[] sArr=str.split(",");
		    			String s="";
		    			for(int i=0;i<sArr.length;i++){
		    				s=s+sArr[i]+" ";
		    			}
		    			checkIn.setName(s);
		    		}else{
		    			checkIn.setName(str);
		    		}
		    	}
		    		    	
		    	JSONObject location=place.getJSONObject("location");
		    	if(location!=null && !location.toString().equals("null")){
		    		if(location.containsKey("country") && location.getString("country")!=null)checkIn.setCountry(location.getString("country").toLowerCase());
			    	if(location.containsKey("city") && location.getString("city")!=null)checkIn.setCity(location.getString("city").toLowerCase());
			    	if(location.containsKey("latitude") && location.getString("latitude")!=null)checkIn.setLatitude(location.getDouble("latitude"));
			    	if(location.containsKey("longitude") && location.getString("longitude")!=null)checkIn.setLongitude(location.getDouble("longitude"));
		    	}
	    	}
	    	checkIns.add(checkIn);
	    	
	    }
		
		return checkIns;
	}
	
	private static ArrayList<PhotoAlbum> extractPhotoAlbum(JSONObject json) {
		ArrayList<PhotoAlbum> albums=new ArrayList<PhotoAlbum>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	PhotoAlbum album=new PhotoAlbum();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	JSONObject comments=jObj.getJSONObject("comments");
		    	if(comments!=null && !comments.toString().equals("null")){
		    		album.setComments(comments.getJSONArray("data").size());
		    	}
		    	if(jObj.containsKey("created_time"))album.setTimeStamp(jObj.getString("created_time"));
		    	if(jObj.containsKey("location"))album.setLocation(jObj.getString("location").toLowerCase());
		    	if(jObj.containsKey("name")){
		    		String str=jObj.getString("name").toLowerCase();
		    		if(str.contains(",")){
		    			String[] sArr=str.split(",");
		    			String s="";
		    			for(int i=0;i<sArr.length;i++){
		    				s=s+sArr[i]+" ";
		    			}
		    			album.setName(s);
		    		}else{
		    			album.setName(str);
		    		}
		    	}
		    	
	    	}
	    	albums.add(album);
	    }
		
		return albums;
	}



	private static ArrayList<Event> extractEvents(JSONObject json) {
		ArrayList<Event> eventslist=new ArrayList<Event>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	Event events=new Event();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	if(jObj.containsKey("name")){
		    		String name=jObj.getString("name").toLowerCase();
		    		
						if(name.indexOf("'")!=-1){
							name=name.replace("'", "");
						}
						if(name.indexOf("\"")!=-1){
							name=name.replace("\"", "");
						}
			    		events.setName(name);
		    		
		    		eventslist.add(events);
		    	}
	    	}
	    	
	    }
		
		return eventslist;
	}
	

	private static ArrayList<Movies> extractMovies(JSONObject json) {
		ArrayList<Movies> movieslist=new ArrayList<Movies>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	Movies movies=new Movies();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	if(jObj.containsKey("name")){
		    		String name=jObj.getString("name").toLowerCase();
					if(name.indexOf("'")!=-1){
						name=name.replace("'", "");
					}
					if(name.indexOf("\"")!=-1){
						name=name.replace("\"", "");
					}
		    		movies.setName(name);
		    	}
	    	}
	    	movieslist.add(movies);
	    }
		
		return movieslist;
	}


	private static ArrayList<Books> extractBooks(JSONObject json) {
		ArrayList<Books> bookslist=new ArrayList<Books>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	Books buks=new Books();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	if(jObj.containsKey("name")){
		    		String name=jObj.getString("name").toLowerCase();
					if(name.indexOf("'")!=-1){
						name=name.replace("'", "");
					}
					if(name.indexOf("\"")!=-1){
						name=name.replace("\"", "");
					}
		    		buks.setName(name);
		    	}
	    	}
	    	bookslist.add(buks);
	    }
		
		return bookslist;
	}


	private static ArrayList<Music> extractMusic(JSONObject json) {
		ArrayList<Music> musiclist=new ArrayList<Music>();
	    JSONArray jsonArr=json.getJSONArray("data");
	    for(int iter=0; iter<jsonArr.size();iter++){
	    	Music music=new Music();
	    	JSONObject jObj=jsonArr.getJSONObject(iter);
	    	if(jObj!=null && !jObj.toString().equals("null")){
		    	if(jObj.containsKey("name")){
		    		String name=jObj.getString("name").toLowerCase();
					if(name.indexOf("'")!=-1){
						name=name.replace("'", "");
					}
					if(name.indexOf("\"")!=-1){
						name=name.replace("\"", "");
					}
		    		music.setName(name);
		    	}
	    	}
	    	musiclist.add(music);
	    }
		
		return musiclist;
	}


	private static void createTestData() throws IOException {
			String outputFile = "E:/Dev/MLData/userdata2_test.csv";
			boolean alreadyExists = new File(outputFile).exists();
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
			if (!alreadyExists)
			{
				csvOutput.write("gender");
				csvOutput.write("age");
				//csvOutput.write("location");
				csvOutput.write("latitude");
				csvOutput.write("longitude");
				csvOutput.write("likes");
				
				for(String mv:unqMovies){
					csvOutput.write(mv);
				}
				for(String ms:unqMusic){
					csvOutput.write(ms);
				}
				for(String bks:unqBooks){
					csvOutput.write(bks);
				}
				
				for(String evs:unqEvents){
					csvOutput.write(evs);
				}
				//csvOutput.write("latitude");
				//csvOutput.write("longitude");
				csvOutput.write("hangoutspot");
				csvOutput.endRecord();
			}
			//sunday: study session and pizza
			//saturday: study session and pizza

			for(int i=0;i<15;i++){
				csvOutput.write(1+"");
				csvOutput.write(24+"");
				//csvOutput.write("Bangalore");
				csvOutput.write("12.97");
				csvOutput.write("77.60");
				csvOutput.write((i+1)+"");
				for(String mv:unqMovies){
					if(mv.equalsIgnoreCase("shawshank redemption movie")||mv.equalsIgnoreCase("kungfu panda movie")){
						csvOutput.write(1+"");
					}else{
						csvOutput.write(0+"");
					}
				}
				for(String ms:unqMusic){
					if(ms.equalsIgnoreCase("metalica music")||ms.equalsIgnoreCase("westlife music")){
						csvOutput.write(1+"");
					}else{
						csvOutput.write(0+"");
					}
				}
				for(String bks:unqBooks){
					if(bks.equalsIgnoreCase("the fountainhead book")||bks.equalsIgnoreCase("the davinci code book")){
						csvOutput.write(1+"");
					}else{
						csvOutput.write(0+"");
					}
				}
				
				for(String evs:unqEvents){
					if(evs.equalsIgnoreCase("sunday: study session and pizza")||evs.equalsIgnoreCase("saturday: study session and pizza")){
						csvOutput.write(1+"");
					}else{
						csvOutput.write(0+"");
					}
				}
				csvOutput.write("?");
				csvOutput.endRecord();
			}
			
			csvOutput.close();
		
	}
	
	private static String helper(String str){
		String ns="";
		if(str.indexOf(" ")!=-1){
			String[] sArr=str.split(" ");
			for(int i=0;i<sArr.length;i++){
				ns=ns+sArr[i]+"#";
			}
		}
		return ns;
	}

	private static void readAndProcessCSV(){
		try{
			ArrayList<String> gender=new ArrayList<String>();
			ArrayList<String> age=new ArrayList<String>();
			ArrayList<String> hspot=new ArrayList<String>();
			ArrayList<String> locations=new ArrayList<String>();
			ArrayList<String> locList=new ArrayList<String>();
			ArrayList<String> likes=new ArrayList<String>();
			CsvReader reader = new CsvReader("/media/EA4A364A4A361433/Documents/SUNY SB/ACADS/ML/Project/Data/users/userdatav35.csv");
			reader.readHeaders();
		    while (reader.readRecord()) {
		    	gender.add(reader.get("gender"));
		    	age.add(reader.get("age"));
		    	hspot.add(reader.get("hangoutspot"));
		    	locations.add(reader.get("location"));
		        locList.add(reader.get("location"));
		        likes.add(reader.get("likes"));
		    }
		    Collections.sort(locList);
		    int count=1;
		    ArrayList<LocToIntMap> locToIntMapList=new ArrayList<LocToIntMap>();
		    File nFile=new File("/media/EA4A364A4A361433/Documents/SUNY SB/ACADS/ML/Project/Data/users/userdataText36.txt");
		    Writer op=new BufferedWriter(new FileWriter(nFile));
		    for(String l:locList){
		    	//processLocation2(l, count);
		    	op.write(l+" "+count);
		    	op.write("\n");
		    	LocToIntMap liObj=new LocToIntMap(l, count);
		    	locToIntMapList.add(liObj);
		    	count++;
		    }
		    
		    op.close();
		    
			String outputFile = "/media/EA4A364A4A361433/Documents/SUNY SB/ACADS/ML/Project/Data/users/userdatav36.csv";
			
			boolean alreadyExists = new File(outputFile).exists();
				

				CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
				
				if (!alreadyExists)
				{
					csvOutput.write("gender");
					csvOutput.write("age");
					csvOutput.write("location");
					csvOutput.write("likes");
					csvOutput.write("hangoutspot");
					csvOutput.endRecord();
				}
				
				for(int i=0;i<locList.size();i++){
					csvOutput.write(gender.get(i));
					csvOutput.write(age.get(i));
					for(int j=0;j<locToIntMapList.size();j++){
						if((locToIntMapList.get(j).loc).equals(locations.get(i))){
							csvOutput.write(locToIntMapList.get(j).label+"");
							break;
						}
					}
					csvOutput.write(likes.get(i));
					csvOutput.write(hspot.get(i));
					csvOutput.endRecord();
				}
				
				csvOutput.close();
		    
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static void writeToCsv() {
		
		String outputFile = "E:/Dev/MLData/userdata2.csv";
		int count=1;
		
		boolean alreadyExists = new File(outputFile).exists();
			
		try {
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
			
			if (!alreadyExists)
			{
				csvOutput.write("gender");
				csvOutput.write("age");
				//csvOutput.write("location");
				csvOutput.write("latitude");
				csvOutput.write("longitude");
				csvOutput.write("likes");
				
				for(String mv:unqMovies){
					csvOutput.write(mv);
				}
				for(String ms:unqMusic){
					csvOutput.write(ms);
				}
				for(String bks:unqBooks){
					csvOutput.write(bks);
				}
				
				for(String evs:unqEvents){
					csvOutput.write(evs);
				}

				csvOutput.write("hangoutspot");

				csvOutput.endRecord();
			}
			
			for(int i=0;i<checkIns.size();i++){
				ArrayList<CheckIn> checkIn=checkIns.get(i);
				ArrayList<Movies> mvs=movies.get(i);
				ArrayList<Music> muzs=music.get(i);
				ArrayList<Books> bkz=books.get(i);
				ArrayList<Event> evz=events.get(i);
				for(int j=0;j<checkIn.size();j++){
					String gend=gender.get(i);
					if(gender.equals(""))continue;
					
					int ag=age.get(i);
					if(ag==0)continue;
					
					String loc=locations.get(i);
					if(loc==null || loc.equals("") || loc.equals("null"))continue;
					System.out.println(loc);
					String latlong= getLatLong(loc);
					if(latlong==null) continue;
					
					CheckIn chkIn=checkIn.get(j);
					String chkInName=chkIn.getName();
					if(chkInName==null || chkInName.equals(""))continue;
					
					String chkInCity=chkIn.getCity();
					if(chkInCity==null || chkInCity.equals(""))continue;
					
					int chkInLikes=chkIn.getLikes();
					if(chkInLikes==0)continue;
					
					if(gend.equalsIgnoreCase("male"))
						csvOutput.write(1+"");
					else
						csvOutput.write(0+"");
					
					csvOutput.write(ag+"");
					
					//location
					//csvOutput.write(loc+"");

					String[] llArr=latlong.split(",");
					csvOutput.write(llArr[0]+"");
					csvOutput.write(llArr[1]+"");
					
					if(chkInName.contains(",")){
						String[] chkInNameArr=chkInName.split(",");
						String nchkIn="";
						for(String st:chkInNameArr){
							nchkIn=nchkIn+st+" ";
						}
						chkInName=nchkIn;
					}
					if(chkInCity.contains(",")){
						String[] chkInCityArr=chkInCity.split(",");
						String nchkIn="";
						for(String st:chkInCityArr){
							nchkIn=nchkIn+st+" ";
						}
						chkInCity=nchkIn;
					}
					
					//processLocation2(chkInCity,count);
					
					//csvOutput.write(locToIntMap.get(chkInCity)+"");
					csvOutput.write(chkInLikes+"");
					
					
					for(String mv:unqMovies){
						boolean flag=false;
						for(Movies movie:mvs){
							if(movie.getName().equalsIgnoreCase(mv)){
								flag=true;
							}

						}
						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					for(String ms:unqMusic){
						boolean flag=false;
						for(Music music:muzs){
							if(music.getName().equalsIgnoreCase(ms)){
								flag=true;	
							}
						}

						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					for(String bk:unqBooks){
						boolean flag=false;
						for(Books book:bkz){
							if(book.getName().equalsIgnoreCase(bk)){
								flag=true;	
							}
						}

						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					
					for(String ev:unqEvents){
						boolean flag=false;
						for(Event evnt:evz){
							if(evnt.getName().equalsIgnoreCase(ev)){
								flag=true;
							}

						}
						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}

					
					csvOutput.write(chkInName);
					csvOutput.endRecord();
					count++;
				}
			}
			
			for(int i=0;i<photoAlbums.size();i++){
				ArrayList<PhotoAlbum> albums=photoAlbums.get(i);
				ArrayList<Movies> mvs=movies.get(i);
				ArrayList<Music> muzs=music.get(i);
				ArrayList<Books> bkz=books.get(i);
				ArrayList<Event> evz=events.get(i);
				for(int j=0; j<albums.size(); j++){
					String gend=gender.get(i);
					if(gender.equals(""))continue;
					
					int ag=age.get(i);
					if(ag==0)continue;
					
					String loc=locations.get(i);
					if(loc==null || loc.equals("") || loc.equals("null"))continue;
					System.out.println(loc);
					String latlong= getLatLong(loc);
					if(latlong==null) continue;
					
					PhotoAlbum album=albums.get(j);
					String albumName=album.getName();
					if(albumName==null || albumName.equals(""))continue;
					
					String albumLocation=album.getLocation();
					if(albumLocation==null || albumLocation.equals(""))continue;
					
					int albumLikes=album.getComments();
					if(albumLikes==0)continue;
					
					if(gend.equalsIgnoreCase("male"))
						csvOutput.write(1+"");
					else
						csvOutput.write(0+"");
					
					csvOutput.write(ag+"");
					
					//location
					//csvOutput.write(loc+"");

					String[] llArr=latlong.split(",");
					csvOutput.write(llArr[0]+"");
					csvOutput.write(llArr[1]+"");
					
					csvOutput.write(albumLikes+"");
					
					
					for(String mv:unqMovies){
						boolean flag=false;
						for(Movies movie:mvs){
							if(movie.getName().equalsIgnoreCase(mv)){
								flag=true;
							}

						}
						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					for(String ms:unqMusic){
						boolean flag=false;
						for(Music music:muzs){
							if(music.getName().equalsIgnoreCase(ms)){
								flag=true;	
							}
						}

						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					for(String bk:unqBooks){
						boolean flag=false;
						for(Books book:bkz){
							if(book.getName().equalsIgnoreCase(bk)){
								flag=true;	
							}
						}

						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}
					
					for(String ev:unqEvents){
						boolean flag=false;
						for(Event evnt:evz){
							if(evnt.getName().equalsIgnoreCase(ev)){
								flag=true;
							}

						}
						if(flag){
							csvOutput.write(1+"");
						}else{
							csvOutput.write(0+"");
						}

					}

					//processLocation2(albumLocation,count);
					
					//csvOutput.write(locToIntMap.get(albumLocation)+"");
					
					csvOutput.write(albumName);
					csvOutput.endRecord();
					count++;
				}
				
			}

			csvOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

/*	private static void processLocation2(String location, int count) {
				
		locToIntMap.put(location, count);
	}*/
	
	private static String getLatLong(String location_info) {
		try{
			    System.out.println(location_info);
			    
			    String url = "http://where.yahooapis.com/geocode?location=locname&appid=KtDYsU34";
		
			    String[] locname=location_info.split(" ");
			    String location="";
			    for(int k=0;k<locname.length; k++){
			    	location+=locname[k]+ "+";
			    }
			    
			    location = location.substring(0, location.length()-1);
			    
			    try{
			    	String s=url.replace("locname", location);
			    	System.out.println(">>>>>>"+s);
					org.jsoup.nodes.Document doc = Jsoup.connect(s).get();
					org.jsoup.nodes.Document doc1 = Jsoup.parse(doc.html());
					
					String responseLatLong = doc1.html();
					int start = responseLatLong.indexOf("<latitude>");
					int end = responseLatLong.indexOf("</latitude>");
					String loclat= responseLatLong.substring(start, end);
					

					start = responseLatLong.indexOf("<longitude>");
					end = responseLatLong.indexOf("</longitude>");
					String loclong= responseLatLong.substring(start, end);
					
					System.out.println(location_info + loclat.substring(11,loclat.length()-3).trim() +  loclong.substring(12,loclong.length()-3).trim());
					
					return  loclat.substring(11,loclat.length()-3).trim()  + "," +  loclong.substring(12,loclong.length()-3).trim() ;
					//locHmap.put(locations.get(i), loclat.substring(11,loclat.length()-3).trim()  + "," +  loclong.substring(12,loclong.length()-3).trim());
					//Thread.sleep(500);
			    }catch (Exception e) {
			    	//e.printStackTrace();
				}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;

	}

	private static String processLocation(String chkInCity) {
		WebSearchQuery query = factory.newWebSearchQuery();
		PagedList<WebResult> response = query.withQuery(chkInCity).list();
		System.out.println(response.getCurrentPageIndex());
		System.out.println(response.getEstimatedResultCount());
		System.out.println(response.getMoreResultsUrl());
		System.out.println(response.getPages());
		
		for (WebResult result : response) {
		        System.out.println(result.getTitle());                  
		        System.out.println(result.getContent());                        
		        System.out.println(result.getUrl());                    
		        System.out.println("=======================================");
		        if(result.getUrl().toLowerCase().contains("en.wikipedia.org") && result.getUrl().toLowerCase().contains(chkInCity.toLowerCase())){
		        	String[] arr=result.getUrl().split("/");
		        	System.out.println(arr[arr.length-1]);
		        	return arr[arr.length-1];
		        }
		}
		return "";
	}

}


