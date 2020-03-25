package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "d4cfe7b24188256dee27a8757dfa46b1";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    LocationManager mlocationManager;
    LocationListener mlistener;





    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    int REQUEST_CODE=123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton =  findViewById(R.id.changeCityButton);



        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent=new Intent(WeatherController.this,ChangeCity.class);
                startActivity(myIntent);
            }
        });
    }


    //
    protected void onResume() {
        super.onResume();
        Log.d("Click", "onResume() called");
        Log.d("Clima", "Getting weather");
        Intent myIntent=getIntent();
        String mcity=myIntent.getStringExtra("city");
        if(mcity==null){getWeather();}
        else

        {getCityWeather(mcity);}
    }




    protected void getWeather() {
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlistener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima","On location change called");
                String longitude=String.valueOf(location.getLongitude());
                String latitude=String.valueOf(location.getLatitude());
                Log.d("Clima","Longitude is: "+longitude);
                Log.d("Clima","Latitude is: "+latitude);
                RequestParams params=new RequestParams();
                params.put("lat",latitude);
                params.put("lon",longitude);
                params.put("appid",APP_ID   );
                letsDoNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(" HI","Location Provider disabled!");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mlocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mlistener);
    }




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode==REQUEST_CODE){
            if( grantResults.length>1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d("Clima","Granted");
                getWeather();


            }else{
                Log.d("Clima","Denied");getWeather();
            }
        }
    }


    private void letsDoNetworking(RequestParams params){
        AsyncHttpClient client=new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){
            public void onSuccess(int statusCode, Header [] headers, JSONObject response){
                Log.d("Success","Success"+response.toString());
                WeatherDataModel myModel=WeatherDataModel.fromJson(response);
                updateUI(myModel);

            }
            public void onFailure(int statusCode, Header [] headers, Throwable e,JSONObject response){
                Log.e("Fail","Failed"+e.toString());
                Toast.makeText(WeatherController.this,"Failed",Toast.LENGTH_SHORT).show();;

            }

        });
    }


    private void getCityWeather(String mycity){
        RequestParams params=new RequestParams();
        params.put("q",mycity);
        params.put("appid",APP_ID);
        letsDoNetworking(params);


    }









    private void updateUI(WeatherDataModel modelcurrent){
        mTemperatureLabel.setText(modelcurrent.getMtemperature());
        mCityLabel.setText(modelcurrent.getmCity());
        int resourceID=getResources().getIdentifier(modelcurrent.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceID);
}



    protected void onPause(){
        super.onPause();
        if(mlocationManager!=null){
            mlocationManager.removeUpdates(mlistener);

        }


    }

}