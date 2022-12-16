package csc.l3.p2022.focardi.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
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
import java.util.List;

import csc.l3.p2022.focardi.myapplication.databinding.ActivityMapsMonthlyBinding;

public class MapsActivityMonthly extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsMonthlyBinding binding;
    private MaterialButton btnPeriode;
    private DatePicker datePickerPeriod;
    private MaterialButton btnAddConsomation;
    private MaterialButton btnMonthly;
    private final  int RADIUSHEATMAP = 170;
    private Spinner spinnerHeure;
    private Spinner spinnerMinutes;
    private Spinner spinnerYear;
    private Spinner spinnerMonth;
    public ArrayList<WeightedLatLng> soleil = new ArrayList<WeightedLatLng>();
    private int year;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        //String year = intent.getStringExtra("year");
        //year = Integer.parseInt(intent.getStringExtra("year")); //if it's a string you stored.
        Bundle bundle = getIntent().getExtras();
        year = bundle.getInt("year");
        System.out.println(year);

        binding = ActivityMapsMonthlyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Button btnBackMap = findViewById(R.id.btnBackMapPrincipalActivity);
        btnBackMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivityMonthly.super.onBackPressed();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapMonthly);

        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng corte = new LatLng(42.307342, 9.158817);
        mMap.setMinZoomPreference(9.0f);
        //mMap.addMarker(new MarkerOptions().position(corte).title("Marker in Corte"));
        addHeatMapMonthly(year,year);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(corte));
    }
    private void addHeatMapMonthly(int yearDebut,int yearFin) {
        List<LatLng> latLngs = null;

        // Get the data: latitude/longitude positions of police stations.
        try {
            latLngs = readItemsYear(yearDebut,yearFin);
        } catch (JSONException | IOException e) {
            Toast.makeText(MapsActivityMonthly.this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
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
    private List<LatLng> readItemsYear(int yearDebut,int yearFin) throws JSONException, IOException {

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
            api.urlapi = new String("https://re.jrc.ec.europa.eu/api/MRcalc?lat="+api.latitude+"&lon="+api.longitude+"&startyear="+yearDebut+"&endyear="+yearFin+"&horirrad=1&avtemp=1&outputformat=json");
            api.start();

            double lng = coordonnees.getDouble(0);
            double lat = coordonnees.getDouble(1);
            try {
                api.join();
                //soleil.put(String.valueOf(api.latitude)+","+String.valueOf(api.longitude),tojson(api.reponseApi.substring(4)));
                soleil.add(new WeightedLatLng(new LatLng(lat, lng),tojsonMonthly(api.reponseApi.substring(4))));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.add(new LatLng(lat, lng));
        }
        return result;
    }
    public double tojsonMonthly(String futurJson) throws JSONException {

        double somme = 0;
        JSONObject jsonObject = new JSONObject(futurJson);
        JSONObject array = jsonObject.getJSONObject("outputs");
        JSONArray mois = array.getJSONArray("monthly");


        for (int i = 0; i < mois.length(); i++) {
            JSONObject object = mois.getJSONObject(i);
            //System.out.println("COUOCU" + object);

            double interessant = object.getDouble("H(h)_m");

            somme +=interessant;
        }

        double result = somme/mois.length()-1;

        return(result);

    }
}
