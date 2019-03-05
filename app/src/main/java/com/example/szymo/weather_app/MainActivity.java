package com.example.szymo.weather_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    Spinner listTownsSpinner;
    TextView tempTextView;
    TextView pressureTextView;
    TextView windTextView;
    TextView coordinateLonTextView;
    TextView coordinateLatTextView;
    TextView locationTextView;
    ImageView downloaderImage;


    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;


            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;

        }

    }


    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {

            String result="";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();

                InputStream in = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data!=-1){
                    char current = (char)data;
                    result+=current;

                    data=reader.read();
                }

                return result;


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Could't find a weather", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String tempWeather="";
            String pressureWeather="";
            String windWeather="";
            String icon="";
            try {

                JSONObject jsonObject=new JSONObject(result);

                JSONObject jsonPartTempPressure = jsonObject.getJSONObject("main");
                tempWeather = jsonPartTempPressure.getString("temp");
                pressureWeather = jsonPartTempPressure.getString("pressure");

                JSONObject jsonPartWind = jsonObject.getJSONObject("wind");
                windWeather = jsonPartWind.getString("speed");

                String weatherInfo = jsonObject.getString("weather");
                String cityInfo = jsonObject.getString("name");

                JSONObject jsonPartCoordinates = jsonObject.getJSONObject("coord");
                String coordinateLonInfo =jsonPartCoordinates.getString("lon");
                String coordinateLatInfo=jsonPartCoordinates.getString("lat");

                Log.i("temperatura",tempWeather);
                Log.i("cisnienie",pressureWeather);
                Log.i("wiatr",windWeather);
                Log.i("pogoda",weatherInfo);
                Log.i("miasto",cityInfo);
                Log.i("lon",coordinateLonInfo);
                Log.i("lat",coordinateLatInfo);

                JSONArray arr=new JSONArray(weatherInfo);

                for(int i=0;i<arr.length();i++){
                    JSONObject partJson = arr.getJSONObject(i);

                    icon=partJson.getString("icon");

                }



                if(tempWeather!="" && pressureWeather!="" &&windWeather!=""){
                    tempTextView.setText(tempWeather);
                    pressureTextView.setText(pressureWeather);
                    windTextView.setText(windWeather);
                    locationTextView.setText(cityInfo);
                    coordinateLonTextView.setText(coordinateLonInfo);
                    coordinateLatTextView.setText(coordinateLatInfo);

                    ImageDownloader image = new ImageDownloader();
                    Bitmap myImage;
                    try {
                        myImage = image.execute("http://openweathermap.org/img/w/"+icon+".png").get();

                        downloaderImage.setImageBitmap(myImage);

                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Not Found",Toast.LENGTH_LONG).show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    @SuppressLint("MissingPermission")
    public void giveYourWeather(View view){

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                DownloadTask task = new DownloadTask();
                task.execute("http://api.openweathermap.org/data/2.5/weather?lat="+String.valueOf(location.getLatitude())+"&lon="+String.valueOf(location.getLongitude())+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT<23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2, locationListener);
        }else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2, locationListener);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2, locationListener);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        tempTextView = (TextView)findViewById(R.id.tempTexView);
        pressureTextView = (TextView)findViewById(R.id.pressureTextView);
        windTextView = (TextView)findViewById(R.id.windTextView);
        coordinateLatTextView = (TextView)findViewById(R.id.coordinateLatTextView);
        coordinateLonTextView = (TextView)findViewById(R.id.coordinateLonTextView);
        locationTextView = (TextView)findViewById(R.id.locationTextView);
        downloaderImage = (ImageView)findViewById(R.id.downloaderImage);
        listTownsSpinner=(Spinner)findViewById(R.id.listTownsSpinner);

        createSpinner();


    }

    public void createSpinner(){

        final String[] elements = {"Warszawa", "Krakow", "Gdansk", "Zywiec", "Poznan"};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, elements);

        listTownsSpinner.setAdapter(adapter);

        listTownsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int id, long position) {

                Toast.makeText(MainActivity.this, "Wybrano opcję" + (id+1), Toast.LENGTH_SHORT).show();
                DownloadTask task=new DownloadTask();
                switch((int)position)
                {
                    case 0:
                        task.execute("http://api.openweathermap.org/data/2.5/weather?q="+elements[0]+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");
                        break;
                    case 1:
                        task.execute("http://api.openweathermap.org/data/2.5/weather?q="+elements[1]+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");
                        break;
                    case 2:
                        task.execute("http://api.openweathermap.org/data/2.5/weather?q="+elements[2]+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");
                        break;
                    case 3:
                        task.execute("http://api.openweathermap.org/data/2.5/weather?q="+elements[3]+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");
                        break;
                    case 4:
                        task.execute("http://api.openweathermap.org/data/2.5/weather?q="+elements[4]+"&units=metric&appid=2260632784cf3936ba06cc9ea641b558");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }













}
