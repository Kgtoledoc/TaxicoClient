package com.ibm.taxicoclient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class ServicioTask extends AsyncTask<Void, Void, String>  {
    private final Double lat;
    private final Double lon;
    // Variables del hilo

    private Context httpContext; //Context
    ProgressDialog progressDialog; // dialogo cargando
    public String resultadoApi="";
    public String linkRequestAPI="";//link para consumir el servicio REST

    // Constructor del Hilo
    public ServicioTask(Context ctx, String linkAPI, Double lat, Double lon){
        this.httpContext=ctx;
        this.linkRequestAPI=linkAPI;
        this.lat = lat;
        this.lon = lon;

    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = ProgressDialog.show(httpContext, "Procesando Solicitud", "Por favor, espere...");
    }

    @Override
    protected String doInBackground(Void... params){
        String result = null;

        String wsURL = linkRequestAPI;
        URL url = null;
        try {
            // Create the connection with the API
            url = new URL(wsURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // Create the JSON Object for send in POST
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("Lat: ", lat);
            postDataParams.put("Long: ", lon);

            // Define the conection parameters
            urlConnection.setReadTimeout(15000 /*Miliseconds*/);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            // Obtain the request result
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();

            int resposeCode = urlConnection.getResponseCode(); // Conection OK
            if(resposeCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuffer sh = new StringBuffer("");
                String linea = "";
                while((linea = in.readLine())!= null){
                    sh.append(linea);
                    break;
                }
                in.close();
                result = sh.toString();
            }
            else {
                result = new String ("Error: " + resposeCode);
            }


        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        progressDialog.dismiss();
        resultadoApi=s;
        Toast.makeText(httpContext, resultadoApi, Toast.LENGTH_LONG).show(); // Mostrar una notificacion con el resultado del request
    }

    //Transformar JSON Object a String
    public String getPostDataString(JSONObject params) throws Exception{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);

            if(first){
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }




}
