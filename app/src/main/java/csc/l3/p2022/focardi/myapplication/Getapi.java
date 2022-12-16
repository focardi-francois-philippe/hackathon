package csc.l3.p2022.focardi.myapplication;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Getapi extends Thread{

    public String urlapi;
    public String reponseApi;
    public double longitude;
    public double latitude;

    Getapi(double latitude, double longitude) {
        // this.urlapi = url;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void run() {
        URL url = null;
        try {
            url = new URL(urlapi);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String inputLine = null;
        while (true) {
            try {
                if (!((inputLine = in.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.reponseApi+=inputLine;

        }
    }
}