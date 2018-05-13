
import org.jsoup.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FactBookParser {
    private String baseUrl;
    private Document doc;
    
    
    private ArrayList<String> countryLinks;
    private HashMap<String, HashMap<String, String>> countryInfo;
    private String accessLink;
    private TreeMap<Integer, String> oldestOrgs;

    /**
     * Constructor creates parser and pulls html from webpage
     */
    public FactBookParser() {
        baseUrl = "https://www.cia.gov/library/publications/the-world-factbook/";
        countryInfo = new HashMap<String, HashMap<String, String>>();
        
        try {
            doc = getDoc(baseUrl);
        } catch (IOException e) {
            System.out.println("Could not access the link!");
        }
        
        getCountryLinks();
        fillCountryInfo();
    }
    
    /**
     * Gets links of countries 
     * @return ArrayList<String>
     */
    private ArrayList<String> getCountryLinks() {
        ArrayList<String> links = new ArrayList<>();
        Elements countries = doc.getElementsByTag("option");
        for (Element country : countries) {
            links.add(this.baseUrl + country.attr("value"));
            HashMap<String, String> placeHolder = new HashMap<String, String>();
            countryInfo.put(country.text().toLowerCase(), placeHolder);
        }
        
        countryLinks = links;
        accessLink = countryLinks.get(5);
        return links;
    }
    
    /**
     * Retrieves url in a document from Recitation 3 Code
     * @param any URL
     */
    
    public Document getDoc(String urlInput) throws IOException {
        URL url = new URL(urlInput);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder builder = new StringBuilder();
        String curr = in.readLine();
        while(curr != null) {
            builder.append(curr);
            curr = in.readLine();
        }
        return Jsoup.parse(builder.toString());
    }
    

    
    /**
     * Gets all the relevant country info for the questions
     * @return ArrayList<String>
     */
    private void fillCountryInfo() {
        try {
            this.doc = getDoc(this.accessLink);
        } catch (IOException e) {
        	
        }
        Elements allFields = doc.select("div#field");
        
        //Where in terms of sections each piece of data is on the website and making treeMappings from section number to concept
        TreeMap<Integer, String> questionData = new TreeMap<Integer, String>() {
            {
                put(3, "continent");
                put(4, "area");
                put(6, "border");
                put(7, "coastline");
                put(9, "climate");
                put(16, "natural hazards");
                put(19, "landlocked");
                put(20, "population");
                put(24, "religions");
                put(75, "flag");
            };
        };
        
        for (Entry<Integer, String> field: questionData.entrySet()) {
            int key = field.getKey();
            String sectionName = field.getValue();
            
            Element mapReferences = allFields.get(key);
            String mapReferencesLink = baseUrl + mapReferences.select("a").get(1).attr("href").substring(3);
            
            try {
                doc = getDoc(mapReferencesLink);
            } catch (IOException e) {
            }
            
            //Differentiates by country and places each country and its corresponding info into the big array of info
            Elements countryNames = doc.select("tr ~ tr");
            for (Element countryName : countryNames) {
                String country = countryName.getElementsByClass("country").get(0).getElementsByTag("a").get(0).text().toLowerCase();
                String info = countryName.getElementsByClass("fieldData").get(0).text().toLowerCase();
                
                HashMap<String, String> fieldInfo = countryInfo.get(country);
                fieldInfo.put(sectionName, info);
                countryInfo.put(country, fieldInfo);
            }
        }
        
        try {
            doc = getDoc(this.baseUrl);
        } catch (IOException e) {
        }
        
        //getting elements by the section tags
        Element organizeMenu = doc.getElementsByClass("sub_menu").get(2);
        String organizeLinks = baseUrl + organizeMenu.getElementsByTag("a").get(1).attr("href");
        try {
            doc = getDoc(organizeLinks);
        } catch (IOException e) {
        }
        
        /**
         * This is the organization question and the functionality needed by grabbing specific sections of the doc
         */
        
        Element pageOrganizations = doc.getElementById("GetAppendix_B");
        Elements allOrganizations = pageOrganizations.getElementsByTag("li");
        TreeMap<Integer, String> organizationsDate = new TreeMap<Integer, String>();
        
        for (Element organization : allOrganizations) {
            String organizationName = organization.getElementsByClass("category").get(0).text();
            
            String organizationDescription = organization.getElementsByTag("td").get(0).text();
            Pattern pattern = Pattern.compile("[0-9]{4}.*");
            Matcher matcher = pattern.matcher(organizationDescription);
            if (matcher.find()) {
                String yearStr = matcher.group();
                yearStr = yearStr.substring(0, 4);
                int year = Integer.parseInt(yearStr);
                
                organizationsDate.put(year, organizationName);
            }
                
        }
        
        oldestOrgs = organizationsDate;
        
        //Removing entries that aren't countries
        countryInfo.remove("world");
        countryInfo.remove("European Union");
        countryInfo.remove("United States Pacific Island Wildlife Refuges");
    }
    
    public static void main(String[] args) {
    	FactBookParser parser = new FactBookParser();
    	System.out.println("Please input a question to be answered (1-8): ");
    	Scanner scan = new Scanner(System.in);

        
        while (true) {
        	/**
        	 * Depending on the input of the user, will display different messages prompting the user
        	 * To provide necessary values to answer the questions
        	 */
            
            String question = scan.nextLine();
            
            if (question.equals("1")) {
            	
            	System.out.println("Please enter a continent:");
                String continent = scan.nextLine();
                System.out.println("Please enter a natural disaster");
                String naturalDisaster = scan.nextLine();
                System.out.println("The countries in " + continent + " that are prone to " + naturalDisaster + "s are:");
                parser.q1(continent, naturalDisaster);
            }
            else if (question.equals("2")) {
            	System.out.println("Please enter a symbol:");
            	String symbol = scan.nextLine();
            	System.out.println("Countries that have a " + symbol + " in their flag are: ");
                parser.q2(symbol);
            }
            else if (question.equals("3")) {
            	System.out.println("Please enter a continent:");
            	String continent = scan.nextLine().toLowerCase();
            	System.out.println("The smallest country in " + continent + " is: ");
                parser.q3(continent);
            }
            else if (question.equals("4")) {
            	System.out.println("Please enter a continent:");
            	String continent = scan.nextLine().toLowerCase();
            	System.out.println("The countries in " + continent + " that have a smaller total area than Pennsylvania are: ");
                parser.q4(continent);
            }
            else if (question.equals("5")) {
            	System.out.println("Please enter a number of organizations: ");
            	int numberOfOrganizations = scan.nextInt();
            	System.out.println("The " + numberOfOrganizations + " oldest International Organizations in "
            			+ "bChronological Order are: ");
                parser.q5(numberOfOrganizations);
            }
            else if (question.equals("6")) {
            	System.out.println("Please enter the percentage of the population you want to have countries with more than "
            			+ "that for the dominant religion:");
                int percent1 = scan.nextInt();
                System.out.println("Please enter the percentage you want to find countries with dominant religions less than "
                		+ "this percentage of the population:");
                int percent2 = scan.nextInt();
                parser.q6(percent1, percent2);
            }
            else if (question.equals("7")) {
                System.out.println("The countries that are landlocked by a single country are:");
                parser.q7();
            }
            else if (question.equals("8")) {
            	System.out.println("Please enter a continent:");
            	String continent = scan.nextLine().toLowerCase();
            	System.out.println("Please enter a climate type:");
            	String climate = scan.nextLine().toLowerCase();
            	System.out.println("The countries in " + continent + " with a " + climate + " climate and lots of coasts"
            			+ " for pretty sunsets are: ");
                parser.q8(continent, climate);
            }
            else {
                System.out.println("Please enter a number between 1 and 8.");
            }
        }
        

    }
    
    public void q1 (String continentChosen, String naturalDisaster) {
        ArrayList<String> question1Answers = new ArrayList<String>();
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String continent = fields.get("continent");
            String naturalDisasters = fields.get("natural hazards");
            
            
            if (continent != null & naturalDisasters != null) {
                if (continent.equals(continentChosen.toLowerCase()) & 
                        naturalDisasters.contains(naturalDisaster.toLowerCase())) {
                    question1Answers.add(country);
                }
            }
        }
        System.out.println(Arrays.toString(question1Answers.toArray()));
    }
    
    public void q2 (String symbol) {
        ArrayList<String> question2Answers = new ArrayList<String>();
        
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String flag = fields.get("flag");
            if (flag != null) {
                if (flag.contains(symbol)) {
                    question2Answers.add(country);
                }
            }
        }
        
        System.out.println(Arrays.toString(question2Answers.toArray()));
    }
    
    public void q3 (String continent) {
        String smallestCountry = "";
        Integer smallestPopulation = 1000000000;
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String continentOfInterest = fields.get("continent");
            if (continentOfInterest != null) {
                if (continentOfInterest.equals(continent)) {
                    String populationText = fields.get("population");
                        if (populationText != null) {
                        populationText = populationText.replaceAll(",", "");
                        Pattern pattern = Pattern.compile("([0-9]+) .*");
                        Matcher matcher = pattern.matcher(populationText);
                        if (matcher.find()) {
                            String population = matcher.group(1);
                            int populationCount = Integer.parseInt(population);
                            
                            //System.out.println(populationCount);
                            
                            if (populationCount < smallestPopulation) {
                                smallestCountry = country;
                                smallestPopulation = populationCount;
                            }
                        }
                    }
                }
            }
        }
        System.out.println(smallestCountry);
    }
    
    public void q4 (String continent) {
    	int pennsylvaniaArea = 119280;
        ArrayList<String> question4Answers = new ArrayList<String>();
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String continentOfInterest = fields.get("continent");
            if (continentOfInterest != null) {
                if (continentOfInterest.equals(continent)) {
                    String areaText = fields.get("area").replace(",", "");
                    Pattern pattern = Pattern.compile("([0-9]+) .*");
                    Matcher matcher = pattern.matcher(areaText);
                    if (matcher.find()) {
                        String area = matcher.group(1);
                        int areaNumeric = Integer.parseInt(area);
                        
                        //System.out.println(areaNumberic);
                        
                        if (areaNumeric < pennsylvaniaArea) {
                            question4Answers.add(country);
                        }
                    }
                }
            }
        }
        System.out.println(Arrays.toString(question4Answers.toArray()));
    }
    
    public void q5(int numberOfOrganizations) {
        int numberOrganizationss = numberOfOrganizations;
        ArrayList<String> oldestOrganizations = new ArrayList<String>();
        int count = 0;
        for (Entry<Integer, String> entry : oldestOrgs.entrySet()) {
            String organizationName = entry.getValue();
            if (count < numberOrganizationss) {
                oldestOrganizations.add(organizationName);
            }
            count++;
        }
        System.out.println(oldestOrganizations);
    }
    
    public void q6 (int percent1, int percent2) {
        ArrayList<String> percent1Countries = new ArrayList<String>();
        ArrayList<String> percent2Countries = new ArrayList<String>();

        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String religions = fields.get("religions");
            if (religions != null) {
                Pattern pattern = Pattern.compile("([0-9]+).*");
                Matcher matcher = pattern.matcher(religions);
                if (matcher.find()) {
                    String dominantReligionPercentage = matcher.group(1);
                    int religionPercentage = Integer.parseInt(dominantReligionPercentage);
                    if (religionPercentage > percent1) {
                        percent1Countries.add(country);
                    }
                    
                    if (religionPercentage < percent2 ) {
                        percent2Countries.add(country);
                    }
                }
            }
        }
        System.out.println("The countries with a dominant religion greater than " + percent1 + " percent are:");
        System.out.println(percent1Countries);
        System.out.println("The countries with a dominant religion less than " + percent2 + " percent are:");
        System.out.println(percent2Countries);
    }
    
    public void q7 () {
        ArrayList<String> question7Answers = new ArrayList<String>();
        
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String landlocked = fields.get("landlocked");
            if (landlocked != null) {
                if (landlocked.contains("landlock")) {
                    String border = fields.get("border");
                    if (border != null) {
                        Pattern pattern = Pattern.compile("\\((.*?)\\)");
                        Matcher matcher = pattern.matcher(border);
                        if (matcher.find()) {
                            String borderCountries = matcher.group(1);
                            int numOfBorderingCountries = Integer.parseInt(borderCountries);
                            if (numOfBorderingCountries == 1) {
                                question7Answers.add(country);
                            }
                        }
                    }  
                }
            }
        }
        System.out.println(question7Answers);
    }
    
    public void q8 (String continentChosen, String climateChosen) {
    	int lotsOfCoasts = 2500;
    	
    	ArrayList<String> question8Answers = new ArrayList<String>();
        for (Entry<String, HashMap<String, String>> countryFields : countryInfo.entrySet()) {
            String country = countryFields.getKey();
            HashMap<String, String> fields = countryFields.getValue();
            String continent = fields.get("continent");
            String climates = fields.get("climate");
            
            
            if (continent != null & climates != null) {
            	
                if (continent.equals(continentChosen.toLowerCase()) & 
                        climates.contains(climateChosen.toLowerCase())) {
                	String coastLine = fields.get("coastline").replace(",", "");
                    Pattern pattern = Pattern.compile("([0-9]+) .*");
                    Matcher matcher = pattern.matcher(coastLine);
                    if (matcher.find()) {
                        String coastLineString = matcher.group(1);
                        int coastLineNumeric = Integer.parseInt(coastLineString);
                        if (coastLineNumeric > lotsOfCoasts) {
                            question8Answers.add(country);
                        }
                    }
                }
            }
        }
        System.out.println(Arrays.toString(question8Answers.toArray()));
        }
      
}
