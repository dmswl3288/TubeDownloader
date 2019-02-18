package com.hejcompany.administrator.tubedownloader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2019-02-18.
 */

public class GetDuration extends AppCompatActivity {

    public static String duration = "";
    String vodid;
    String serverKey;

    public GetDuration(String vodid, String serverKey){   // 생성자
        super();
        this.vodid = vodid;
        this.serverKey = serverKey;

        // 생성자에서 AsyncTask 실행
        new searchDuration().execute();
    }

    private class searchDuration extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // show Dialog
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getDurationItem();

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.d("POST EXE: ", "finish");

        }
    }

    public void getDurationItem() throws JSONException {

        JSONObject contentDetailsObject = getVideoDuration();
        JSONArray items = contentDetailsObject.getJSONArray("items");

        for (int j = 0; j < items.length(); j++) {
            JSONObject d = items.getJSONObject(j);
            // 동영상 길이 duration
            duration = d.getJSONObject("contentDetails").getString("duration");

        }

        Log.d("DURATION TIME: ", duration);

    }

    public JSONObject getVideoDuration() {

        HttpGet httpGet = new HttpGet("https://www.googleapis.com/youtube/v3/videos?"
                + "id=" + vodid
                + "&key="+ serverKey + "&part=contentDetails" + "&maxResults=50");  //EditText에 입력된 값으로 겁색을 합니다. 공백이 없어야 검색됨

        // part(snippet), q(검색값) , key(서버키)
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {

            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject;
    }
}
