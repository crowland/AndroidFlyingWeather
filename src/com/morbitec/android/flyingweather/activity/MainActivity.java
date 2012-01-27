package com.morbitec.android.flyingweather.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.morbitec.android.flyingweather.R;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends Activity
{
  private static final String URL_METAR_RAW = "http://weather.noaa.gov/pub/data/observations/metar/stations/";
  private static final String URL_METAR_DECODED = "http://weather.noaa.gov/pub/data/observations/metar/decoded/";
  private static final String URL_TAF = "http://weather.noaa.gov/pub/data/forecasts/taf/stations/";
  private static final String URL_EXTENSION = ".TXT";
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    _addAirportTextListener();

    _addGoButtonClickListener();
  }



  //-------------------- private methods --------------------

  
  
  private void _addGoButtonClickListener()
  {
    final Button searchBtn = (Button)findViewById(R.id.goButton);
    searchBtn.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        final TextView metarTxt = (TextView)findViewById(R.id.metarText);
        metarTxt.setText("");

        final TextView tafTxt = (TextView)findViewById(R.id.tafText);
        tafTxt.setText("");

        final EditText airportTxt = (EditText)findViewById(R.id.airportId);
        String airportId = airportTxt.getText().toString();        
        
        final CheckBox decodeCb = (CheckBox)findViewById(R.id.decodeMetarCB);
        final CheckBox includeTafCb = (CheckBox)findViewById(R.id.includeTafCB);
        
        String metarRequest = null;
        if (decodeCb.isChecked())
          metarRequest = URL_METAR_DECODED + airportId.toUpperCase() + URL_EXTENSION;
        else
          metarRequest = URL_METAR_RAW + airportId.toUpperCase() + URL_EXTENSION;
        
        String metarResult = null;
        try
        {
          metarResult = _callWebSvc(metarRequest);
        }
        catch (Throwable t)
        {
          metarResult = "Metar not available";
        }        
        
        metarTxt.setText(metarResult);
        
        if (includeTafCb.isChecked())
        {
          String tafRequest = URL_TAF + airportId.toUpperCase() + URL_EXTENSION;
          String tafResult = null;

          try
          {
            tafResult = _callWebSvc(tafRequest);
          }
          catch (Throwable t)
          {
            tafResult = "TAF not available";
          }

          tafTxt.setText(tafResult);
        }
      }
    });
  }

  private void _addAirportTextListener()
  {
    final EditText airportText = (EditText)findViewById(R.id.airportId);

    airportText.setOnClickListener(new EditText.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        airportText.setText("");
      }
    });
  }

  private String _callWebSvc(String query) throws Exception
  {
    HttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(query);
    get.addHeader("deviceid", "12345");
    ResponseHandler<String> handler = new BasicResponseHandler();
    String result = null;

    result = client.execute(get, handler);
    client.getConnectionManager().shutdown();

    return result;
  }
}
