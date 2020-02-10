package com.currencyconverter.REST;

import com.currencyconverter.jdbc.ConverterJdbc;
import com.currencyconverter.model.Currency;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

public class ApiConnect {
    private final String URL_STR = "https://api.exchangeratesapi.io/latest?base=USD";
    private final String USER_AGENT = "API Client/1.0";

    static final String USER = "sa";
    static final String PASS = "";

    private String output;

    private static Connection connection = null;
    private static Statement statement = null;
    private static ConverterJdbc search = new ConverterJdbc();
    private static Currency curr = new Currency();

    public void update() {
        connect();
    }

    public void connect() {
        try {
            URL url = new URL(URL_STR);
            URLConnection request = url.openConnection();
            System.out.println("Connected to API");

            InputStream stream = request.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(stream));
            output = "["+read.readLine()+"]";
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getExchangeRate() {
        JSONArray jsonArray = new JSONArray(output);
        JSONObject obj = jsonArray.getJSONObject(0);
        Object exRates = obj.get("rates");
        return "["+exRates.toString()+"]";
    }

    public void updateCurrencies() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:~/CurrencyConverterMk6/src/data/curr", USER, PASS);
            statement = connection.createStatement();

            update();

            JSONArray array = new JSONArray(getExchangeRate());
            JSONObject obj = array.getJSONObject(0);
            Iterator<String> list = obj.keys();

            while (list.hasNext()) {
                curr.setAbbrv(list.next().toString());
                Object rates = obj.get(curr.getAbbrv());
                String rateString = rates.toString();
                curr.setExchangeRate(Double.parseDouble(rateString));

                search.update(curr, statement);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
}
