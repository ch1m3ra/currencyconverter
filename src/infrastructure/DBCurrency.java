package infrastructure;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
//import java.sql.ResultSet;
//import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;

import org.hsqldb.HsqlException;

public class DBCurrency extends Database {

	// Treat as a Singleton
	static private DBCurrency __instance = null;
	static synchronized public DBCurrency getInstance() throws SQLException {
		if(__instance == null) {
			__instance = new DBCurrency();
		}
		return __instance;
	}

	private HashMap<String, Double> __cache = new HashMap<String, Double>();

	private DBCurrency() throws SQLException {
		super("db_currency");

		try {
//			this.update( "DROP TABLE exchange_rates" );
			this.update( 
					"CREATE TABLE exchange_rates ( " +
							"currency CHAR(3), " +
							"currency_value DECIMAL(100,10) " +
							")" 
			);
		} catch(HsqlException e) {
			System.out.println("Table probably already exists");
			//e.printStackTrace();
		} catch(SQLSyntaxErrorException e) {
			System.out.println("Table probably already exists");
			//e.printStackTrace();
		}

 		try {
//			this.update( "DROP TABLE exchange_rates_updates" );
			this.update(
					"CREATE TABLE exchange_rates_updates ( " +
							"id INTEGER IDENTITY, " +
							"date_updated datetime " +
							")"
			);
		} catch(HsqlException e) {
			System.out.println("Table probably already exists");
			//e.printStackTrace();
		} catch(SQLSyntaxErrorException e) {
			System.out.println("Table probably already exists");
			//e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void updateExchangeRates(HashMap<String, Double> exchangeRates) throws SQLException {
		PreparedStatement psInsertDateUpdated = _conn.prepareStatement(
				"INSERT INTO exchange_rates_updates (date_updated) " +
				"VALUES (?)"
		);

		java.util.Date today = new java.util.Date();
		psInsertDateUpdated.setDate(1, new java.sql.Date(today.getTime()));
		psInsertDateUpdated.executeUpdate();

		/*
		  		"INSERT INTO exchange_rates (currency, currency_value) " +
				"VALUES (?, ?)"
		 */
		PreparedStatement psInsertExchangeRates = _conn.prepareStatement(
 				"MERGE INTO exchange_rates USING (VALUES(?, ?)) " + 
				   "AS vals(currency,currency_value) ON exchange_rates.currency = vals.currency " +
				   "WHEN MATCHED THEN UPDATE SET exchange_rates.currency_value = vals.currency_value " +
				   "WHEN NOT MATCHED THEN INSERT VALUES vals.currency, vals.currency_value "
		);

		__cache.clear();
		Iterator<?> iterExchangeRates = exchangeRates.entrySet().iterator();
		while(iterExchangeRates.hasNext()) {
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>)iterExchangeRates.next();
			psInsertExchangeRates.setString(1, entry.getKey());
			psInsertExchangeRates.setDouble(2, entry.getValue());
			psInsertExchangeRates.executeUpdate();

			__cache.put(entry.getKey(), entry.getValue());
		}
	}

	public synchronized void populateCache() {
		Statement stExchangeRates = null;
		String sqlGetExchangeRates = "SELECT currency, currency_value FROM exchange_rates ORDER BY currency ASC";

		try {
			stExchangeRates = _conn.createStatement();
			ResultSet rs = stExchangeRates.executeQuery(sqlGetExchangeRates);
			while (rs.next()) {
				__cache.put(rs.getString(1), rs.getDouble(2));
			}
		} catch(Exception e) {
			System.out.println("Unable to populate the DBCurrency cache.");
			e.printStackTrace();
		}
	}
	
	public synchronized Date getCacheDate() {
		Statement stDateUpdated = null;
		String sqlDateUpdated = "SELECT date_updated FROM exchange_rates_updates ORDER BY date_updated DESC LIMIT 1";
		Date dateUpdated = null;

		try {
			stDateUpdated = _conn.createStatement();
			ResultSet rs = stDateUpdated.executeQuery(sqlDateUpdated);
			if (rs.next()) {
				dateUpdated = rs.getDate(1);
			} else {
				throw new Exception();
			}
		} catch(Exception e) {
			dateUpdated = new Date(-1);
		}
		return dateUpdated;
	}
	
	/*
	 * Convert fromCurrencyType to USD to toCurrencyType
	 */
	public Double getExchangeRate(String fromCurrencyType, String toCurrencyType) {
		if(__cache.size() == 0) populateCache();

		Double fromCurrency = __cache.get(fromCurrencyType);
		Double toCurrency = __cache.get(toCurrencyType);
		
		return toCurrency / fromCurrency;
	}
}
