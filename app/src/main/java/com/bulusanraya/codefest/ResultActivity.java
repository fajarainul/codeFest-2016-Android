package com.bulusanraya.codefest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;/*

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
*/

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class ResultActivity extends AppCompatActivity {
    String respJSON;
    ProgressDialog progressDialog;
    TextView txtPercentage;
    long totalSize = 0;
    List<String> mUrls = new ArrayList<String>();
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //get all View
        //progressBar = (ProgressBar)findViewById(R.id.progressbar);
        txtPercentage = (TextView)findViewById(R.id.txtPercentage);
        gridView = (GridView) findViewById(R.id.grid_view);


        Intent intent = getIntent();

        respJSON = intent.getStringExtra("RESPONSE");

        //Toast.makeText(getApplicationContext(), respJSON, Toast.LENGTH_LONG).show();

        progressDialog = ProgressDialog.show(this, null,
                "Please wait...", true);

        new createResult().execute();


       // new UploadFileToServer().execute();

    }

    private class createResult extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("T", mUrls.get(0));
            gridView.setAdapter(new GridViewAdapter(getApplicationContext()));
            gridView.setOnScrollListener(new ScrollListener(getApplicationContext()));
            progressDialog.dismiss();


        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject jObj = new JSONObject(respJSON);
//                JSONObject image = new JSONObject(jObj);

                JSONArray resultArray = jObj.getJSONArray("images");
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject imageJSON = resultArray.getJSONObject(i);
                    // Storing  JSON item in a Variable

                    Log.d("URL "+i, imageJSON.getString("img_url"));
                    mUrls.add(imageJSON.getString("img_url"));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    final class GridViewAdapter extends BaseAdapter {
        private final Context context;
        private final List<String> urls = mUrls;

        public GridViewAdapter(Context context) {
            this.context = context;

        }


        @Override public View getView(int position, View convertView, ViewGroup parent) {
            SquaredImageView view = (SquaredImageView) convertView;
            if (view == null) {
                view = new SquaredImageView(context);
                view.setScaleType(CENTER_CROP);
            }


            // Get the image URL for the current position.
            String url = getItem(position);
            //String url = "http://192.168.43.153/competition/codefest/image/img.jpg";

            Log.d("Toooommn", url);

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context) //
                    .load(url) //
                    .placeholder(R.drawable.placeholder) //
                    .error(R.drawable.error) //
                    .fit() //
                    .tag(context) //
                    .into(view);

            return view;
        }

        @Override public int getCount() {
            return urls.size();
        }

        @Override public String getItem(int position) {
            return urls.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }
    }


}

