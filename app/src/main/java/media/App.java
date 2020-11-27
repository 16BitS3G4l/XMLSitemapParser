package media; 

import media.utils.ParseXMLSitemap;

public class App {
	public static void main(String[] args) {
		String prefixURL = "https://websitename.com/sitemap-list-";
		
		int amountOfURLs = 18;
		int counter = 1; 	
		for(int i = 0; i < amountOfURLs; i++) {
			new ParseXMLSitemap(prefixURL + String.valueOf(counter) + ".xml").start(); 	
			counter += 1;
		}
	}
}
