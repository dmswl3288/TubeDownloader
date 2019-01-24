package com.hejcompany.administrator.tubedownloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //  Dialog dialog;
    CustomCircleProgressDialog ccpd;

    // hide navigation bar 네비게이션 바 제거
    public void hideNavigationBar(){
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    void showCustomDialog() {
        ccpd = new CustomCircleProgressDialog(MainActivity.this);
        ccpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ccpd.setCancelable(false); // 주변 클릭 터치시 프로그래서 사라지지 않게 하기.

        ccpd.show();
        // dialog = new Dialog(MainActivity.this, R.style.loading_dialog_style);
        //  ProgressBar pb = new ProgressBar(MainActivity.this);
        //  pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#bb0000"), android.graphics.PorterDuff.Mode.SRC_IN);
        // dialog.addContentView(pb, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // dialog.show();
    }

    void dismissCustomDialog(){
        //dialog.dismiss();
        ccpd.dismiss();
    }

    static public String youtubeSearch="Tube Downloader";
    static DrawableManager DM = new DrawableManager();
    final String serverKey = "AIzaSyDAXx2E0a6_0FskH8o9Qd5n9oVw36ZLrQ4";
    ArrayList<SearchData> sData = new ArrayList<SearchData>();
    AsyncTask<?,?,?> searchTask;

    String editText = "MV";
    private Toolbar toolBar;

    @Override
    protected void onResume(){
        super.onResume();

        // navigation bar 숨기기
        hideNavigationBar();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // navigation bar 숨기기
        hideNavigationBar();

        //------------------------------------Toolbar----------------------------
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");   // 로고이미지만 나타나도록 빈문자열을 설정해둔다.
        // Toolbar 설정
        setSupportActionBar(toolBar);  // 툴바를 액션바와 같게 만들어 준다.
        //-----------------------------------------------------------------------

        new searchTask().execute();  // 바로 첫 화면 실행
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // 검색 버튼 클릭했을 때 searchview 길이 꽉차게 늘려주기
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("검색어를 입력하세요");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {    // EditText 입력 후 검색버튼 누른 경우

                editText = s;
                youtubeSearch = s;
                searchTask = new searchTask().execute();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                System.out.println(s);
                return false;
            }
        });
        return true;
    }

    // 검색 버튼 클릭시 실행되는 클래스
    private class searchTask extends AsyncTask<Void, Void, Void> {

        // ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // progressDialog.setMessage("로딩중입니다...");

            showCustomDialog();
            //progressDialog.show();  // show Dialog
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                paringJsonData(getUtube());

            } catch (JSONException e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            ListView searchlist = (ListView) findViewById(R.id.searchlist);

            StoreListAdapter mAdapter = new StoreListAdapter(MainActivity.this,
                    R.layout.activity_start, sData);

            searchlist.setAdapter(mAdapter);

            // progressDialog.dismiss();
            dismissCustomDialog();

            // navigation bar 숨기기
            hideNavigationBar();
        }
    }

    public JSONObject getUtube() {

        editText = editText.replace(" ", "");   // 문자열의 공백 없애기

        HttpGet httpGet = new HttpGet("https://www.googleapis.com/youtube/v3/search?"
                + "part=snippet&q=" + editText
                + "&key="+ serverKey+"&maxResults=50");  //EditText에 입력된 값으로 겁색을 합니다. 공백이 없어야 검색됨

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

    // 파싱을 하면 여러가지 값을 얻을 수 있는데 필요한 값들을 세팅하셔서 사용하시면 됩니다.
    String vodid = "";
    private void paringJsonData(JSONObject jsonObject) throws JSONException {

        sData.clear();
        JSONArray contacts = jsonObject.getJSONArray("items");

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            String kind = c.getJSONObject("id").getString("kind"); // 종류를 체크하여 playlist도 저장

            if(kind.equals("youtube#video")){
                vodid = c.getJSONObject("id").getString("videoId");
                // 동영상
                // 아이디
                // 값입니다.
                // 재생시
                // 필요합니다.
            }
            else{
                vodid = c.getJSONObject("id").getString("playlistId"); //유튜브
            }
            // 유튜브 제목
            String title = c.getJSONObject("snippet").getString("title");
            String changString = "";
            String tName = "";

            try {
                // 한글이 깨져서 인코딩
                changString = new String(title.getBytes("8859_1"), "utf-8");

                // 타이틀 길이가 50이상이면 끊기
                if(changString.length() >= 50){
                    tName = changString.substring(0,50)+"...";
                }
                else{
                    tName =  changString;
                }
                // temp으로 다시 재저장하기
                changString = tName;

            } catch (UnsupportedEncodingException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // 등록날짜
            String date = c.getJSONObject("snippet").getString("publishedAt").substring(0, 10); // 등록날짜
            date = date.replace('-','.');  // 2018-02-04를 2018.02.04로 대체
            //썸내일 이미지 URL값
            String imgUrl = c.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url"); // 썸내일 이미지 URL값
            Log.d("Youtube", "제목 : " + title);

            // 받아온 비디오id값 추가
            sData.add(new SearchData(vodid, changString, imgUrl, date));

        }
    }

    public class StoreListAdapter extends ArrayAdapter<SearchData> {

        private ArrayList<SearchData> items;
        SearchData fInfo;

        public StoreListAdapter(Context context, int textViewResourseId, ArrayList<SearchData> items) {

            super(context, textViewResourseId, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {// listview

            // 출력
            View v = convertView;
            fInfo = items.get(position);      // 리스트에 해당 위치 아이템의 Data를 가리킴

            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = vi.inflate(R.layout.listview_start, null);
            ImageView img = (ImageView) v.findViewById(R.id.img);

            String url = fInfo.getUrl();
            String sUrl = "";
            String eUrl = "";
            sUrl = url.substring(0, url.lastIndexOf("/") + 1);
            eUrl = url.substring(url.lastIndexOf("/") + 1, url.length());

            try {
                eUrl = URLEncoder.encode(eUrl, "EUC-KR").replace("+", "%20"); // 한글로 인코딩
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String new_url = sUrl + eUrl;

            DM.fetchDrawableOnThread(new_url, img);  //비동기 이미지 로더
            v.setTag(position);
            // 리스트 아이템을 터치했을 때 발생
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();  // 선택된 인덱스 값 가져오기
                    /*Intent intent = new Intent(MainActivity.this, StartActivity.class);
                    intent.putExtra("id", items.get(pos).getVideoID());
                    */
                    //리스트 터치시 재생하는 엑티비티로 이동합니다. 동영상 아이디를 넘겨줍니다..
                    // startActivity(intent);
                    // 여기서 동영상 재생하지 않고 동영상 정보 나타내주는 클래스(PreviewActivity.class)로 이동하기
                    Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                    intent.putExtra("previewItemUrl",items.get(pos).getUrl());  // (이미지) url
                    intent.putExtra("previewItemTitle", items.get(pos).getTitle());  // 타이틀 보내기
                    intent.putExtra("previewItemDate", items.get(pos).getPublishedAt());  // date 보내기
                    intent.putExtra("previewVideoID", items.get(pos).getVideoID());  // videoID 보내기

                    startActivity(intent);
                }
            });

            ((TextView) v.findViewById(R.id.title)).setText(fInfo.getTitle());       // Set title
            ((TextView) v.findViewById(R.id.date)).setText(fInfo.getPublishedAt());  // Set date
            return v;
        }
    }
}
