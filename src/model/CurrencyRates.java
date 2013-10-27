package model;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import config.Config;
import infrastructure.DBCurrency;


public class CurrencyRates {
	// Treat as a Singleton
	static private CurrencyRates __instance = null;
	static public CurrencyRates getInstance() {
		if(__instance == null) {
			__instance = new CurrencyRates();
		}

		return __instance;
	}

	private DBCurrency __dbCurrency = null;

	private CurrencyRates() {
		try {
			__dbCurrency = DBCurrency.getInstance();
			this.updateExchangeRatesIfStale();
			
			// Proof the database has data in it
			System.out.println(this.convertCurrencyType("EUR", "JPY", 5.00));
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Double convertCurrencyType(String fromCurrencyType, String toCurrencyType, Double value) {
		return __dbCurrency.getExchangeRate(fromCurrencyType, toCurrencyType) * value;
	}

	class updateExchangeRatesIfStaleTaskTimer extends TimerTask {
		public void run() {
			updateExchangeRatesIfStale();
		}
	}

	public void updateExchangeRatesIfStale() {
		Long OneDayInMilliseconds = 1000L * 60L * 60L * 24L;
//		Long OneDayInMilliseconds = 1000L * 60L;
		Date cacheDate = __dbCurrency.getCacheDate();
		Date cacheDateExpires = new Date(cacheDate.getTime() + OneDayInMilliseconds);
		Date today = new Date();
		Timer timerCacheRefresh = new Timer();
		Date cacheForceUpdateDate = null;
		
		if(today.getTime() >= cacheDateExpires.getTime()) {
			this.updateExchangeRates();
			cacheForceUpdateDate = new Date(today.getTime() + OneDayInMilliseconds);
		} else {
			cacheForceUpdateDate = cacheDateExpires;
		}

		timerCacheRefresh.schedule(new updateExchangeRatesIfStaleTaskTimer(), cacheForceUpdateDate);

	}

	public void updateExchangeRates() {
		String jsonTxt, line = "";
 		StringBuilder lines = new StringBuilder();
 		HashMap<String, Double> exchangeRates = new HashMap<String, Double>();
// 		String pathJsonFile = "/home/impakt/Workspaces/ProjectsWeb/CurrencyConverter/src/files/input/latest.json";
		String pathJsonFile = "http://openexchangerates.org/api/latest.json?app_id=" + Config.JSON_APP_ID;
		Reader resourceJson = null;
//		resourceJson = new FileReader(pathJsonFile);

		try {
			URL urlJsonFile = new URL(pathJsonFile);
			resourceJson = new InputStreamReader(urlJsonFile.openStream());			
		} catch(Exception e) {
			System.out.println("Malformed URL?");
		}

 		// Get the current exchange rates
 		try(BufferedReader reader = new BufferedReader(resourceJson)) {
			while ((line = reader.readLine()) != null) 
				lines.append(line);

	 		// Import the JSON file, parse it, and then get all the exchange rates relative to USD
	 		jsonTxt = lines.toString();
			JSONObject jsonInput = (JSONObject) JSONSerializer.toJSON( jsonTxt );
			JSONObject jsonRates = jsonInput.getJSONObject("rates");

			// Store all rates into a HashMap
			Iterator<?> iterRates = jsonRates.keys();
			while(iterRates.hasNext()) {
				String key = (String)iterRates.next();
				exchangeRates.put(key, jsonRates.getDouble(key));
			}
			
			// Persist the exchange rates
			try {
				DBCurrency dbCurrency = DBCurrency.getInstance();
				dbCurrency.updateExchangeRates(exchangeRates);
			} catch(SQLException e) {
				e.printStackTrace();
			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
			System.out.println("JSON file was not found at: \n" + pathJsonFile);
		} catch (IOException e) {
			System.out.println("Failed trying to read the input JSON.");
		}
	}	

}
