package csc.l3.p2022.focardi.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RawRes;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import csc.l3.p2022.focardi.myapplication.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng corte = new LatLng(42.307342, 9.158817);
        //mMap.setMinZoomPreference(9.0f);
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
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(latLngs).gradient(gradient).build();

        // Add a tile overlay to the map, using the heat map tile provider&
        TileOverlay overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
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
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            JSONArray coordonnees = object.getJSONObject("geometry").getJSONArray("coordinates");
            //System.out.println(coordonnees);
            double lng = coordonnees.getDouble(0);
            double lat = coordonnees.getDouble(1);
            result.add(new LatLng(lat, lng));
        }
        return result;
    }


}