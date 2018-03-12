package com.shopphukienxe.xechungtnhn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView edtDiemdon;
    private  TextView edtDiemtra;
    private TextView tvHanhtrinh;
    private Button btnDatxe;
    //private Button btn_current_location;
    private static final int LOCATION_REQUEST = 500;
    public static String StartAdd;
    public static String EndAdd;
    public static String Distance;
    public static String Duration;

    private LatLng current_location;
    public static LatLng picklocation;

    private final LatLng mDefaultLocation = new LatLng(21.5458491, 105.8678594);
    private static final int DEFAULT_ZOOM = 16;

    private FusedLocationProviderClient mFusedLocationClient;

    ArrayList<LatLng> listPoints;
    //Chuyển văn bản thành giọng nói
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Chuyển văn bản thành giọng nói
        tts = new TextToSpeech(MapsActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.forLanguageTag("VN"));
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS","Language is not suported");
                    }else {
                        ConvertTextToSpeech("Ok");
                    }
                }else {
                    Log.e("TTS","Init Failled");
                }
            }
        });

        edtDiemdon = (TextView) findViewById(R.id.etxtDiemdon);
        edtDiemdon.setSelected(true);
        edtDiemtra = (TextView)findViewById(R.id.etxtDiemtra);
        edtDiemtra.setSelected(true);
        btnDatxe = (Button)findViewById(R.id.btnDatxe);
        //btn_current_location = (Button)findViewById(R.id.btn_current_location) ;
        tvHanhtrinh = (TextView) findViewById(R.id.tv_hanhtrinh);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listPoints = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            String latitude = String.valueOf(location.getLatitude());
                            String longitude = String.valueOf(location.getLongitude());
                            String speed = String.valueOf(location.getSpeed());
                            //edtDiemdon.setText("TĐ "+latitude+" "+longitude);
                            current_location = new LatLng(location.getLatitude(),location.getLongitude());
                            edtDiemdon.setText("Vị trí hiện tại");

                            listPoints.add(current_location);

                            if(current_location != null) mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup))
                                    .position(current_location)
                            );

                        }
                    }
                });
        btnDatxe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPoints.clear();
                mMap.clear();
                listPoints.add(current_location);
                LatLng latLng = mMap.getCameraPosition().target;
                listPoints.add(latLng);

                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(current_location);
                markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup));
                mMap.addMarker(markerOptions1);

                MarkerOptions markerOptions2 = new MarkerOptions();
                markerOptions2.position(latLng);
                markerOptions2.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dropoff));
                mMap.addMarker(markerOptions2);

                edtDiemdon.setText("Vị trí hiện tại");
                String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

            }
        });

/*
        btnDatxe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listPoints.size()==2) ConvertTextToSpeech("Bạn đã đặt xe thành công. Lái xe sẽ chủ động liên hệ với bạn.");
                else if(listPoints.size()==1) ConvertTextToSpeech("Vui lòng chọn điểm trả khách trước khi Đặt xe.");
                else ConvertTextToSpeech("Vui lòng chọn điểm đón và trả khách trước khi Đặt xe.");



            }
        });
*/

    }

    public void moveCurrentLocation(View view){
        if( current_location != null )  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, DEFAULT_ZOOM));
    }
    public void searchLocation(View view){
        Intent intent = new Intent(this,SearchLocationActivity.class);
        startActivity(intent);
        //ConvertTextToSpeech("Chọn điểm đón sau đó bấm xác nhận");
    }
    //Chuyển văn bản thành giọng nói
    @Override
    public void onDestroy(){
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //Chuyển văn bản thành giọng nói
    private void ConvertTextToSpeech(String text){
        if(text == null) {
            tts.speak("Ứng dụng đã sãn sàng.", TextToSpeech.QUEUE_FLUSH, null);
        }else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public static void setStartAdd(String add) {
        StartAdd = add;
    }
    public static void setEndAdd(String add) {
        EndAdd = add;
    }    public static void setDistance(String distance) {
        Distance = distance;
    }
    public static void setDuration(String duration) {
        Duration = duration;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        //mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
                if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select

                    listPoints.add(latLng);
                    //Create marker
                    MarkerOptions markerOptions = new MarkerOptions();
                if(latLng != null) markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup));
                    //String OrgAddUrl = getAddUrl(listPoints.get(0));
                    //edtDiemdon.setText(" " +OrgAddUrl);
                    //edtDiemdon.setText("TĐ : " + listPoints.get(0).latitude + " - " + listPoints.get(0).longitude);
                    edtDiemdon.setText(" ");
                    edtDiemtra.setText(" ");
                    ConvertTextToSpeech("Hãy chọn điểm trả khách trên bản đồ");
                } else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dropoff));
                }
                mMap.addMarker(markerOptions);

                if (listPoints.size() == 2) {
                    //Create the URL to get request from first marker to second marker
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                    //String DesAddUrl = getAddUrl(listPoints.get(1));
                    //edtDiemtra.setText(EndAdd);
                }

            }
        });
        //Thêm vị trí x
        LatLng carloc = new LatLng(21.543721,105.8672493);
        MarkerOptions markerOptionsCar = new MarkerOptions()
                .position(carloc)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_grabcar))
                .rotation(45);
        mMap.addMarker(markerOptionsCar);
        markerOptionsCar.rotation(30);
        //ConvertTextToSpeech("Ứng dụng đã sẵn sàng. Hãy chọn lộ trình chuyến đi.");

    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param +"&language=vi&region=VN";
        return url;
    }
    private String getAddUrl(LatLng origin){
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + origin.latitude + "," + origin.longitude + "&sensor=true";
        return url;
    }
    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }

    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm được tuyến đường!", Toast.LENGTH_SHORT).show();
            }
            edtDiemdon.setText(StartAdd);
            edtDiemtra.setText(EndAdd);
            Locale localeVN = new Locale("vi","VN");
            NumberFormat vn = NumberFormat.getInstance(localeVN);
            double kc = Double.parseDouble(Distance)/1000;
            double giatien = kc*8000;
            if(giatien<=25000) giatien = 20000;
            String giatien2 = vn.format(giatien);
            String thongbao = "Khoảng cách: "+vn.format(kc)+"km - Thời gian: "+Duration +"\nGiá tạm tính: "+ giatien2 +" VND";
            tvHanhtrinh.setText(thongbao);
            tvHanhtrinh.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(), "Giá tạm tính: "+ giatien2 +" VND", Toast.LENGTH_LONG).show();
            //Chuyển văn bản thành giọng nói
            ConvertTextToSpeech("Đón khách tại "+edtDiemdon.getText().toString()+" và trả khách tại "+edtDiemtra.getText().toString()+". "+thongbao);
        }
    }

}
