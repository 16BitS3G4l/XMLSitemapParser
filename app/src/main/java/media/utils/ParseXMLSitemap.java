
package media.utils;

import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.sql.*;
import java.net.MalformedURLException;
import org.xml.sax.SAXException;
import media.URLType;

public class ParseXMLSitemap extends Thread {

     	private URL url = null; 
	private HttpURLConnection connection = null;
	private Connection databaseConnection = null;
	private InputStream inputStream = null;
	private DocumentBuilderFactory documentBuilderFactory = null;
	private DocumentBuilder documentBuilder = null;
	private Document document = null;
	private Element documentElement = null; 
	private NodeList rootChildren = null;
	private String nodeURL = "";

	public ParseXMLSitemap(String url) {
		try {	
			this.url = new URL(url);
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} 
	} 

	// Process to get all this information and aggregate it somewhere (better name required) 
	//(1) setup a connection to the webpage
	//(2) setup appropriate data structures (in prepartion of parsing, and other necessary operations) 
	//(3) read from the inputStream data (otherwise known as the XML document) - and load that into the parser
	//(4) get the root element
	//(5) get all URL elements in the urlset
	//(6) for each of those, get the loc element (the first element) it's value reflects an individual url for each item of the website
	//(7) differentiate movies from tv shows
	//(8) get the id (basically the lastIndexOf(-) should get you there + 1 to the end....
	//(9) save this information somewhere or something....
	 
	public void setupConnection() {
		// setup the database connection
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		} 

		
		try {
			databaseConnection = DriverManager.getConnection("jdbc:postgresql://host:port/database_name", "username", "password");
		} catch(Exception e) {
			e.printStackTrace();
		} 

		// now setup the http connection
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch(IOException e) {
			e.printStackTrace();
		}

	} 
	
	public void setupAppropriateDataStructures() {
		documentBuilderFactory = documentBuilderFactory.newInstance();

		try {	
			documentBuilder = documentBuilderFactory.newDocumentBuilder();	
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}


	} 

	public void readInputStreamIntoParser() {
	try {
		inputStream = connection.getInputStream();
		document = documentBuilder.parse(inputStream);	
	} catch(Exception e) {
			e.printStackTrace();
		}
	} 

	public void loadRootElement() {
		documentElement = document.getDocumentElement();
	} 

	public void getRootElementChildren() {
		rootChildren = documentElement.getElementsByTagName("url");
	} 

	public void getURLForRootElementChild(Node node) {	
		nodeURL = node.getFirstChild().getTextContent();
	} 

	public URLType typeOfURL() {
		if(nodeURL.startsWith("https://www1.ev01.net/tv")) {
			return URLType.TV_SHOW;	
		} else {
			return URLType.MOVIE;
		}	
	} 
	
	public String getURLId() {
		int beginningIndexOfID = nodeURL.lastIndexOf("-") + 1; 
		return nodeURL.substring(beginningIndexOfID, nodeURL.length());
	} 
	
	public String getURLTitle() {
		int beginningIndexOfTitle = nodeURL.indexOf("-") + 1; 
		int endIndexOfTitle = nodeURL.lastIndexOf("-") - 7;

		String titleWithHyphens = nodeURL.substring(beginningIndexOfTitle, endIndexOfTitle);
		String[] titleArrayWithoutHyphens = titleWithHyphens.split("-");
		StringBuilder titleWithoutHyphens = new StringBuilder(); 

		for(String titlePart : titleArrayWithoutHyphens) {
			titleWithoutHyphens.append(titlePart + " ");
		} 

		return titleWithoutHyphens.toString();
	} 

	public void dealWithInformation(URLType urlType, String urlId, String urlTitle) {
		if(urlType == URLType.MOVIE) {
			
			try {
				String query = "INSERT INTO Movies " + 
				"VALUES(?, ?, ?)";
				PreparedStatement statement = databaseConnection.prepareStatement(query);
				statement.setString(1, nodeURL);
				statement.setInt(2, Integer.parseInt(urlId));
				statement.setString(3, urlTitle);
		
				statement.executeUpdate();
			} catch(Exception e) { 
				e.printStackTrace();
			} 	
		} else if(urlType == URLType.TV_SHOW) {
			try {
				String query = "INSERT INTO TvShows " + 
				"VALUES(?, ?, ?)";
				PreparedStatement statement = databaseConnection.prepareStatement(query);
				statement.setString(1, nodeURL);
				statement.setInt(2, Integer.parseInt(urlId));
				statement.setString(3, urlTitle);

				statement.executeUpdate();
			} catch(Exception e) {
				e.printStackTrace();
			} 
		} 
	} 

	@Override
	public void run() {
		setupConnection();
		setupAppropriateDataStructures();
		readInputStreamIntoParser();
		loadRootElement();
		getRootElementChildren();
		
		for(int i = 0; i < rootChildren.getLength(); i++) {
			Node node = rootChildren.item(i);
			getURLForRootElementChild(node);	
			
			dealWithInformation(typeOfURL(), getURLId(), getURLTitle());
		}			
	}
}
