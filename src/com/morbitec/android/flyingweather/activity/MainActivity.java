package com.morbitec.android.flyingweather.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
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

import java.util.StringTokenizer;

public class MainActivity extends Activity
{
  private static final String URL_METAR_RAW = "http://weather.noaa.gov/pub/data/observations/metar/stations/";
  private static final String URL_METAR_DECODED = "http://weather.noaa.gov/pub/data/observations/metar/decoded/";
  private static final String URL_TAF = "http://weather.noaa.gov/pub/data/forecasts/taf/stations/";
  private static final String URL_EXTENSION = ".TXT";

  private static final String RESULT_SEPARATOR =
"__________________________________________________";

  private TextView wxText;
  private CheckBox decodeCb;
  private CheckBox includeTafCb;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    wxText = (TextView)findViewById(R.id.wxText);
    decodeCb = (CheckBox)findViewById(R.id.decodeMetarCB);
    includeTafCb = (CheckBox)findViewById(R.id.includeTafCB);

    _addAirportTextListener();

    _addGoButtonClickListener();
  }



  //-------------------- private methods --------------------

  
  
  private void _addGoButtonClickListener()
  {
    final Button goBtn = (Button)findViewById(R.id.goButton);
    goBtn.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        wxText.setText("");

        final EditText airportTxt = (EditText)findViewById(R.id.airportId);
        String airportIds = airportTxt.getText().toString();
        
        if (airportIds == null || airportIds.length() < 1)
        {
          wxText.setText("Please enter at least one airport identifier.");
          return;
        }
        
        String[] airportIdArray = airportIds.split(" ");

        for (int i = 0; i < airportIdArray.length; i++)
        {
          GetWeatherTask getWeatherTask = new GetWeatherTask();
          getWeatherTask.execute(new String[]{airportIdArray[i]});
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


  //----------------- private async tasks -----------------

  private class GetWeatherTask extends AsyncTask<String, Void, String>
  {
    @Override
    protected void onPostExecute(String s)
    {
      wxText.append(s);
    }

    @Override
    protected String doInBackground(String... strings)
    {
      String airportId = strings[0].trim().toUpperCase();

      StringBuilder wxResult = new StringBuilder();

      String metarRequest = null;
      if (decodeCb.isChecked())
        metarRequest = URL_METAR_DECODED + airportId + URL_EXTENSION;
      else
        metarRequest = URL_METAR_RAW + airportId + URL_EXTENSION;

      wxResult.append(airportId).append("\n\n");

      try
      {
        wxResult.append("METAR\n").append(_callWebSvc(metarRequest));
      }
      catch (Throwable t)
      {
        wxResult.append("METAR not available for " + airportId);
      }

      if (includeTafCb.isChecked())
      {
        wxResult.append("\nTAF\n");

        String tafRequest = URL_TAF + airportId + URL_EXTENSION;
        try
        {
          wxResult.append(_callWebSvc(tafRequest));
        }
        catch (Throwable t)
        {
          wxResult.append("TAF not available for " + airportId);
        }
      }

      wxResult.append("\n" + RESULT_SEPARATOR + "\n\n");

      return wxResult.toString();
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
}
