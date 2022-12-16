package csc.l3.p2022.focardi.myapplication;

import static java.lang.Math.abs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import csc.l3.p2022.focardi.myapplication.databinding.ActivityMapsBinding;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private MaterialButton btnPeriode;
    private DatePicker datePickerPeriod;
    private MaterialButton btnAddConsomation;
    private MaterialButton btnMonthly;
    private final  int RADIUSHEATMAP = 170;
    private Spinner spinnerHeure;
    private Spinner spinnerMinutes;
    private Spinner spinnerYear;
    private Spinner spinnerMonth;
    public double x;
    public double angle;
    public double y;
    public double z;
    public FusedLocationProviderClient fusedLocationClient;
    public double latitude;
    public double longitude;
    public Button btnGps;
    public double irradiationCourante = 0.0;
public  static int REQUEST_CODE = 100;
    //public HashMap<String,Double> soleil = new HashMap<String,Double>();
    public ArrayList<WeightedLatLng> soleil = new ArrayList<WeightedLatLng>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Obtenez les lectures du capteur de champ magnétique à partir de l'objet SensorEvent
                float magneticFieldX = event.values[0];
                float magneticFieldY = event.values[1];
                float magneticFieldZ = event.values[2];

                if (abs((abs(z)-abs(magneticFieldZ)))>1){
                    z = magneticFieldZ;
                    System.out.println("OUI");

                    System.out.println("OUAH "+ magneticFieldZ);
                    System.out.println("CALCUL "+ (abs(z)-abs(magneticFieldZ)) );

                    angle = (Math.atan2(event.values[0], event.values[1])) * 180/Math.PI;
                    System.out.println("ANGLE "+ angle);
                }


                // Utilisez les valeurs du champ magnétique pour mettre à jour l'interface utilisateur ou effectuer d'autres calculs
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        btnPeriode = binding.btnPeriode;
        btnAddConsomation = binding.btnAddConsomation;
        btnMonthly = binding.btnMonthly;
        btnGps = binding.btnCoordonnesGps;
        btnPeriode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code à exécuter lorsque l'utilisateur clique sur le bouton
                //Toast.makeText(getApplicationContext(), "Bouton cliqué !", Toast.LENGTH_SHORT).show();
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.dialog_periode);
                dialog.setTitle("Periode");
                /*
                dialog.setCancelable(true);
                dialog.setView(R.layout.dialog_periode);*/
                datePickerPeriod = dialog.findViewById(R.id.date_dialog);
                spinnerHeure = dialog.findViewById(R.id.SpnHeure);
                spinnerMinutes = dialog.findViewById(R.id.SpnMinute);
                String[] itemsHeure = new String[24];
                String[] itemsMinutes = new String[60];
                for (int i = 0;i<24;i++)
                {
                    if(i < 10)
                    {
                        itemsHeure[i] = "0"+i;
                    }
                    else
                    {
                        itemsHeure[i] = (String.valueOf(i));
                    }

                }
                for (int i = 0;i<60;i++)
                {
                    if(i < 10)
                    {
                        itemsMinutes[i] = "0"+i;
                    }
                    else
                    {
                        itemsMinutes[i] = (String.valueOf(i));
                    }
                }

                ArrayAdapter<String> adapterHeure = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, itemsHeure);
                ArrayAdapter<String> adapterMinutes = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, itemsMinutes);

                spinnerHeure.setAdapter(adapterHeure);
                spinnerMinutes.setAdapter(adapterMinutes);
                Button btnAnnuler = dialog.findViewById(R.id.btnAnnulerDialog);
                Button btnValider = dialog.findViewById(R.id.btnValiderDialog);
                btnAnnuler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                btnValider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int heures = Integer.parseInt(spinnerHeure.getSelectedItem().toString());
                        int minutes = Integer.parseInt(spinnerMinutes.getSelectedItem().toString());
                        System.out.println(heures);
                        System.out.println(minutes);
                    }
                });
                dialog.show();
            }
        });
        btnAddConsomation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.dialog_conso);
                dialog.setTitle("Ajouter consomation");
                Button btnValiderConso = dialog.findViewById(R.id.btnValiderConso);

                btnValiderConso.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editTextConso = dialog.findViewById(R.id.edit_text_conso);
                        int conso = Integer.parseInt(editTextConso.getText().toString());

                    }
                });
                dialog.show();
            }
        });
        btnMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.dialog_monthly);
                dialog.setTitle("Mois");

                spinnerYear = dialog.findViewById(R.id.SpnYear);
                //spinnerMonth = dialog.findViewById(R.id.SpnMonth);
                String[] itemsYear = new String[7];
                String[] itemsMonth = new String[12];
                int yearDebut = 2010;

                for (int i = 0;i<=6;i++)
                {

                        itemsYear[i] = String.valueOf(yearDebut);
                        yearDebut++;
                }
                for (int i = 1;i<13;i++)
                {
                    if(i < 10)
                    {
                        itemsMonth[i-1] = "0"+(i);
                    }
                    else
                    {
                        itemsMonth[i-1] = (String.valueOf(i));
                    }
                }

                ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, itemsYear);
                ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, itemsMonth);

                spinnerYear.setAdapter(adapterYear);
                //spinnerMonth.setAdapter(adapterMonth);
                Button btnValiderMonthly = dialog.findViewById(R.id.btnValiderDialogMonthly);
                Button btnCancelMonthly = dialog.findViewById(R.id.btnAnnulerDialogMonthly);
                btnValiderMonthly.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int month =  Integer.parseInt(spinnerMonth.getSelectedItem().toString());
                        int year =  Integer.parseInt(spinnerYear.getSelectedItem().toString());
                        Intent myIntent = new Intent(MapsActivity.this, MapsActivityMonthly.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        myIntent.putExtra("year", year); //Optional parameters
                        dialog.cancel();
                        startActivity(myIntent);
                        //finish();

                    }
                });
                btnCancelMonthly.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        btnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                longitude = 42;
                latitude = 9.14;
                Getapi gps = new Getapi(latitude,longitude);
                gps.urlapi = "https://re.jrc.ec.europa.eu/api/seriescalc?lat="+latitude+"&lon="+longitude+"&pvcalculation=1&peakpower=1&loss=14&angle="+angle+"&aspect="+(abs(z)-180)+"+&startyear=2016&endyear=2016&outputformat=json";

                gps.start();
                try {
                    gps.join();
                    irradiationCourante= tojson(gps.reponseApi.substring(4));
                    Toast.makeText(MapsActivity.this, String.valueOf(irradiationCourante), Toast.LENGTH_LONG).show();
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mapFragment.getMapAsync(this);
    }
    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location !=null){

                                latitude = location.getLatitude();
                                longitude = location.getLongitude();


                            }

                        }
                    });


        }else
        {

            askPermission();
            getLastLocation();

        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng corte = new LatLng(42.307342, 9.158817);
        mMap.setMinZoomPreference(9.0f);
        //mMap.addMarker(new MarkerOptions().position(corte).title("Marker in Corte"));
        addHeatMap();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(corte));
    }

    private void addHeatMap() {
        List<LatLng> latLngs = null;

        // Get the data: latitude/longitude positions of police stations.
        try {
            latLngs = readItems();
        } catch (JSONException | IOException e) {
            Toast.makeText(MapsActivity.this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
            //System.out.println("Erreur");
        }
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };
        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider.Builder provider = new HeatmapTileProvider.Builder();
        HeatmapTileProvider provider2;
        provider.weightedData(soleil);
        //provider.data(latLngs);
        provider2 =  provider.build();
        provider2.setRadius(RADIUSHEATMAP);
        // Add a tile overlay to the map, using the heat map tile provider&
        TileOverlay overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider2));
    }


    private List<LatLng> readItems() throws JSONException, IOException {

        List<LatLng> result = new ArrayList<>();
        InputStream inputStream = getAssets().open("corse.json");
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        String json = new String(buffer,"UTF-8");

        JSONObject jsonObject = new JSONObject(json);
        JSONArray array = jsonObject.getJSONArray("features");
        final int NBRPOINTS = 2;//array.length()
        for (int i = 0; i < NBRPOINTS; i++) {
            JSONObject object = array.getJSONObject(i);
            JSONArray coordonnees = object.getJSONObject("geometry").getJSONArray("coordinates");
            //System.out.println(coordonnees);
            Getapi api = new Getapi(coordonnees.getDouble(1),coordonnees.getDouble(0));
            api.urlapi = new String("https://re.jrc.ec.europa.eu/api/seriescalc?lat="+api.latitude+"&lon="+api.longitude+"&pvcalculation=1&peakpower=1&loss=14&angle=45&aspect=90&startyear=2016&endyear=2016&outputformat=json");
            api.start();

            double lng = coordonnees.getDouble(0);
            double lat = coordonnees.getDouble(1);
            try {
                api.join();
                //soleil.put(String.valueOf(api.latitude)+","+String.valueOf(api.longitude),tojson(api.reponseApi.substring(4)));
                soleil.add(new WeightedLatLng(new LatLng(lat, lng),tojson(api.reponseApi.substring(4))));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.add(new LatLng(lat, lng));
        }
        return result;
    }

    public double tojson(String futurJson) throws JSONException {

        double somme = 0;
        JSONObject jsonObject = new JSONObject(futurJson);
        JSONObject array = jsonObject.getJSONObject("outputs");
        JSONArray heures = array.getJSONArray("hourly");


        for (int i = 0; i < heures.length()-1; i++) {
            JSONObject object = heures.getJSONObject(i);
            //System.out.println("COUOCU" + object);

            double interessant = object.getDouble("G(i)");

            somme +=interessant;
        }

        double result = somme/heures.length()-1;

        return(result);

    }



}