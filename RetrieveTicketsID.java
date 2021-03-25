import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTicketsID {

   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }

   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONArray json = new JSONArray(jsonText);
         return json;
       } finally {
         is.close();
       }
   }

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONObject json = new JSONObject(jsonText);
         return json;
       } finally {
         is.close();
       }
   }
   
   public static Date parseStringToDate(String string) throws ParseException {	   
	   String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	   Date date = new SimpleDateFormat(format).parse(string); 
	   return date;
   }
   
   public static void getItemsAndWriteCSV(String projName, String type, String resolution) throws IOException, JSONException, ParseException {
	   Integer j = 0, i = 0, k = 0, l = 0, total = 1, sumElemDataArrayFinal = 0;
       BufferedWriter br = new BufferedWriter(new FileWriter("/Users/danielemariano/Desktop/result_file.csv"));	 
       
       do {
	         // Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
	         j = i + 1000;
	         
	         // Create a parametric URL
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                 + projName + "%22AND%22issueType%22=%22" + type + "%22AND%22resolution%22=%22"
	                 + resolution + "%22&fields=key,resolutiondate,versions,created&startAt="
	                 + i.toString() + "&maxResults=" + j.toString();
	         
	         // Get data from Jira restAPI search
	         JSONObject json = readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         ArrayList<Date> dataArray = new ArrayList<Date>();
	        
	         // Iterate through each 'type' search for 
	         total = json.getInt("total");
	         for (; i < total && i < j; i++) {
	          	JSONObject dataField = issues.getJSONObject(i%1000);
	         	String dataFieldObject = dataField.getJSONObject("fields").get("resolutiondate").toString();
	         	
	         	// Use my 'parseStringToDate' for a better date parsing
	         	dataArray.add(parseStringToDate(dataFieldObject));
	         }         
	         
	         // Reorder the array temporally
	         dataArray.sort(null);
	         int dataArraySize = dataArray.size();	         
	         
	         // Get some useful information about the time frame
	         ArrayList<Integer> dataArrayFinal = new ArrayList<Integer>();
	         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
	         cal.setTime(dataArray.get(0));
	         int startYear = cal.get(Calendar.YEAR);
	         cal.setTime(dataArray.get(dataArraySize - 1));
	         int endYear = cal.get(Calendar.YEAR);
	         int monthCounter = ((endYear + 1) - startYear) * 12;	         
	         
	         // Initialize array by 0-padding
	         for(; k < monthCounter; k++) {
	        	 dataArrayFinal.add(0);
	         }
	         
	         // Write header of the csv file produced in output
	         StringBuilder sb = new StringBuilder();
	         sb.append(type + " " + resolution);
	         sb.append(",");
	         sb.append("Resolution date");
		     sb.append("\n");
	         br.write(sb.toString());
	         
	         // Cycle on 'dataArray' counting for each month how many 
	         // 'types' have been 'resolution'
	         for(; l < dataArraySize; l++) {
	        	 int value_counter = 1;
	        	 cal.setTime(dataArray.get(l));
	        	 int year = cal.get(Calendar.YEAR);
	             int month = cal.get(Calendar.MONTH);
	             
	             for(int m = l + 1; m < dataArraySize; m++) {
	            	 cal.setTime(dataArray.get(m));
	            	 int year2 = cal.get(Calendar.YEAR);
	                 int month2 = cal.get(Calendar.MONTH);
	            	 
	                 if(month == month2 && year == year2) {
	            		 value_counter = value_counter + 1;
	            		 l = l + 1;
	            	 }
	                 
	            	 else {
	            		 break;
	            	 }
	             }
	             
	             // Update the respective 'value_counter' found in 'dataArrayFinal'
	             int index = ((year - startYear) * 12) + month;
	             dataArrayFinal.set(index, value_counter);

	         }
	         
	         // TODO --> stampe da eliminare
	         System.out.println("dataArrayFinal");
	         System.out.println(dataArrayFinal);
	         
	         // Cycle on 'dataArrayFinal' to write 'value_counter' associated
	         // to month and year to the csv file
	         int indexYear = 0;
	         int indexMonth = 1;
	         for(int elemDataArrayFinal : dataArrayFinal) {
	        	 sumElemDataArrayFinal = sumElemDataArrayFinal + elemDataArrayFinal;
	        	 
	        	 if (indexMonth == 13) {
	        		 indexMonth = 1;
			         indexYear ++;
	        	 }
	        	 String dateForDataSet = String.valueOf(indexMonth) + "/" + String.valueOf(startYear + indexYear);
		         
		         // Write data in csv file produced in output
	        	 StringBuilder sb2 = new StringBuilder();
		         sb2.append(elemDataArrayFinal);
		         sb2.append(",");
		         sb2.append(dateForDataSet);
			     sb2.append("\n");
		         br.write(sb2.toString());
		         
		         indexMonth ++;
	         }
	         
	         // TODO --> ciclo da eliminare
	         System.out.println("dataArray");
	         for(Date elemDataArray : dataArray)  {
	        	 System.out.println(elemDataArray);
	         } 

	         // TODO --> stampe da eliminare
	         System.out.println("sumElemDataArrayFinal");
	         System.out.println(sumElemDataArrayFinal);
	         System.out.println("dataArray.size()");
	         System.out.println(dataArray.size());
	       
	         br.close();
	         
	      } while (i < total);
	      return;
   } 

   public static void main(String[] args) throws IOException, JSONException, ParseException {
	   
	   // Set project's specifications
	   String projName = "MAHOUT";
	   String type = "Bug";
	   String resolution = "fixed"; 
	   
	   getItemsAndWriteCSV(projName, type, resolution);
   
   }
   
}