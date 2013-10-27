package controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.*;

@WebServlet("/Main")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CurrencyRates currencyRates = null;
	
    public Main() {
    	super();
    	currencyRates = CurrencyRates.getInstance();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
 		ArrayList<String> currenciesAllowed = new ArrayList<String>(Arrays.asList("EUR", "GBP", "JPY", "USD"));

		try {
			request.setAttribute("currencies", currenciesAllowed);
			request.getRequestDispatcher("/index.jsp").forward(request, response);			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		try {
			String type = request.getParameter("type");
		
			if(type.equals("convert")) {
				String fromCurrencyType = "";
				String toCurrencyType = "";
				Double currencyValue = 0.00;
				StringBuilder errors = new StringBuilder();

				try {
					fromCurrencyType = request.getParameter("fromCurrencyType");
					
					if((fromCurrencyType == null) || (fromCurrencyType.equals(""))) {
						throw new Exception();
					}
				} catch(Exception e) {
					e.printStackTrace();
					errors.append("Missing 'From Currency'");
				}

				try {
					toCurrencyType = request.getParameter("toCurrencyType");

					if((toCurrencyType == null) || (toCurrencyType.equals(""))) {
						throw new Exception();
					}
				} catch(Exception e) {
					e.printStackTrace();
					errors.append("Missing 'To Currency'");
				}
					
				try {
					currencyValue = Double.valueOf(request.getParameter("currencyValue"));
				} catch(Exception e) {
					e.printStackTrace();
					errors.append("Missing 'Value'");
				}

				String result = null;
				if(errors.length() == 0) {
					result = currencyRates.convertCurrencyType(fromCurrencyType, toCurrencyType, currencyValue).toString();
				} else {
					result = errors.toString();
				}
				OutputStream output = response.getOutputStream();
				output.write(result.getBytes());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
