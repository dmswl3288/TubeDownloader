package com.hejcompany.administrator.tubedownloader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

/**
 * Created by Administrator on 2018-11-01.
 */

public class PreviewActivity extends AppCompatActivity {
    private Toolbar toolBar;
    static DrawableManager DM = new DrawableManager();

    String youtubeLink = "http://youtube.com/watch?v=";

    // if (codec == 0), execute MP3 download     else if (codec == 1), execute MP4 download
    int codec = 0;

    ImageView previewImg;
    ImageButton playButton;
    TextView previewTitle;
    TextView previewDate;

    Button mp3DownloadBtn;
    Button mp4DownloadBtn;

    String videoID = "";
    String previewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_start);

        int REQUEST_CODE=1;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        //------------------------------------Toolbar----------------------------
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        // Toolbar 설정
        toolBar.setTitleTextColor(Color.parseColor("#000000"));  // 제목 검정색
        toolBar.setTitle(MainActivity.youtubeSearch);    // 상단에 검색어 설정
        setSupportActionBar(toolBar);  // 툴바를 액션바와 같게 만들어 준다.
        //-----------------------------------------------------------------------
        // preview_start.xml 파일과 연결
        previewImg =  (ImageView) findViewById(R.id.previewImg);
        playButton = (ImageButton) findViewById(R.id.playButton);   // play Button
        previewTitle =  (TextView) findViewById(R.id.previewTitle);
        previewDate = (TextView) findViewById(R.id.previewDate);

        mp3DownloadBtn = (Button) findViewById(R.id.mp3download);   // mp3 Download Button
        mp4DownloadBtn = (Button) findViewById(R.id.mp4download);

        // 클릭된 리스트 정보를 가져오기
        Intent gt = getIntent();
        previewUrl = gt.getStringExtra("previewItemUrl");   // Url 가져오기
        previewTitle.setText(gt.getStringExtra("previewItemTitle"));  // title
        previewDate.setText(gt.getStringExtra("previewItemDate"));    // Date

        videoID = gt.getStringExtra("previewVideoID");  // videoID 가져오기

        String sUrl = "";
        String eUrl = "";
        sUrl = previewUrl.substring(0, previewUrl.lastIndexOf("/") + 1);
        eUrl = previewUrl.substring(previewUrl.lastIndexOf("/") + 1, previewUrl.length());

        // youtubeLink =  "http://youtube.com/watch?v=KMRLzSQorKO"
        youtubeLink = youtubeLink+videoID;

        try {
            eUrl = URLEncoder.encode(eUrl, "EUC-KR").replace("+", "%20"); // 한글로 인코딩
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String new_url = sUrl + eUrl;

        DM.fetchDrawableOnThread(new_url, previewImg);  //비동기 프리뷰 이미지 로더


        // playButton 클릭시 재생 -> StartActivity.class 파일로 이동 id 값도 보내기
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreviewActivity.this, StartActivity.class);
                intent.putExtra("id", videoID);

                startActivity(intent);
            }
        });

        mp3DownloadBtn.setOnClickListener(new View.OnClickListener() {    // mp3 Button 클릭시 실행되는 함수 .m4a
            @Override
            public void onClick(View v) {                // 음악만 다운로드 .m4a

                codec = 0;
                dialogShow();
            }
        });

        mp4DownloadBtn.setOnClickListener(new View.OnClickListener() {      // mp4 Button 클릭시 실행되는 함수 .mp4
            @Override
            public void onClick(View v) {                   // 동영상 다운로드   .mp4

                codec = 1;
                dialogShow();
            }
        });
    }

    // dialog EditText 이름 변경 창 띄우기
    void dialogShow(){

        final EditText edittext = new EditText(this);

        // 기본 설정으로 본래 타이틀 제목 가져오기
        edittext.setText(previewTitle.getText().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이름변경");
        builder.setMessage("파일이름을 변경해 보세요");
        builder.setView(edittext);
        builder.setPositiveButton("다운로드",
                new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                          // mp3다운로드 버튼 누르고 이름 변경후 다운로드시 실행
                        Toast.makeText(getApplicationContext(), "다운로드중 입니다.", Toast.LENGTH_LONG).show();
                        new YouTubeExtractor(getApplicationContext()) {

                            @Override
                            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                                //mainProgressBar.setVisibility(View.GONE);

                                if (ytFiles == null) {
                                    // Something went wrong we got no urls. Always check this.
                                    finish();
                                    return;
                                }

                                // Iterate over itags
                                for (int i = 0, itag; i < ytFiles.size(); i++) {
                                    itag = ytFiles.keyAt(i);
                                    // ytFile represents one file with its url and meta data
                                    YtFile ytFile = ytFiles.get(itag);

                                    if(codec == 0) {  // mp3
                                        if (ytFile.getFormat().getHeight() == -1) {   // Height = -1이면 오디오
                                            //Log.d("DL Link", "Audio CODEC : "+ytFile.getFormat().getAudioBitrate());
                                            if (ytFile.getFormat().getAudioBitrate() != -1) {   // AudioBitrate()가 -1이면 소리 X
                                                Log.d("DL Link", "Number Index : " + i); // i==6
                                                Log.d("DL LINK", "itag :" + itag);    // .m4a    itag == 140
                                                //getYoutubeDownloadUrl(vMeta.getTitle(), ytFile); // 함수호출
                                                getYoutubeDownloadUrl(edittext.getText().toString(), ytFile); // 함수호출
                                                break;
                                            }
                                        }
                                    }
                                    else if(codec == 1){  // mp4
                                        if (ytFile.getFormat().getHeight() >= 360) {   // 720은 거의 없음, 화질 360부터..
                                            Log.d("DL Link", "Audio CODEC : "+ytFile.getFormat().getAudioBitrate());   // 오디오코덱 확인
                                            if(ytFile.getFormat().getAudioBitrate() != -1) {   // AudioBitrate()가 -1이면 소리 X
                                                Log.d("DL Link", "Number Index : " + i); // i==1
                                                Log.d("DL LINK", "itag :" + itag);    // .mp4   itag == 18
                                                getYoutubeDownloadUrl(edittext.getText().toString(), ytFile); // 함수호출
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }.extract(youtubeLink, true, false);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    private void getYoutubeDownloadUrl(final String videoTitle, final YtFile ytfile) {

        String filename;
        if (videoTitle.length() > 55) {
            filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
        } else {
            filename = videoTitle + "." + ytfile.getFormat().getExt();
        }
        filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
        downloadFromUrl(ytfile.getUrl(), videoTitle, filename);

        /*Log.d("Download Link", "download filename : "+filename);
        //Log.d("Download Link", "download LINK : "+previewTitle.getText().toString()+filename);
        Log.d("Download Link", "download LINK : "+previewTitle.getText().toString() + filename);
        Log.d("DL Link", "download LINKedIn: "+ytfile.getUrl());*/
        //===========new DownloadFile().execute(ytfile.getUrl());     // Start to download files
    }

    // Downloading youtubeUrl
    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        File filePath = new File(Environment.getExternalStorageDirectory() + "/Tube Downloader");  // 폴더 추가

        if(!filePath.exists()){
            filePath.mkdirs();   // 폴더가 없다면 생성
        }

        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName); 원본
        request.setDestinationInExternalPublicDir("/Tube Downloader", fileName);

        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
