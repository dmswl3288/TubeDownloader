package com.hejcompany.administrator.tubedownloader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.io.File;
import java.io.IOException;
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

    String _artist;

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

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog, null);

        final EditText songTitle = (EditText) dialoglayout.findViewById(R.id.songtitle);
        // 아티스트 제거하기
        final EditText artist = (EditText) dialoglayout.findViewById(R.id.artist);

        // 기본 설정으로 본래 타이틀 제목 가져오기
        songTitle.setText(previewTitle.getText().toString(), TextView.BufferType.EDITABLE);
        artist.setText("", TextView.BufferType.EDITABLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이름변경");
        builder.setMessage("파일이름을 변경해 보세요");
        builder.setView(edittext);*/
        builder.setPositiveButton("다운로드",
                new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                          // mp3다운로드 버튼 누르고 이름 변경후 다운로드시 실행
                        Toast.makeText(getApplicationContext(), "다운로드중 입니다.", Toast.LENGTH_LONG).show();

                        _artist = artist.getText().toString();  // 아티스트 이름 가져와서 저장
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
                                                getYoutubeDownloadUrl(songTitle.getText().toString(), ytFile); // 함수호출
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
                                                getYoutubeDownloadUrl(songTitle.getText().toString(), ytFile); // 함수호출
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

        /* String pathdata = filePath +"/"+ "Trying to Be Cool.mp3";

        Log.d("DL LINK", "pathdata :" + pathdata);
        RegisterDownloadManagerReciever(this, pathdata);  // pathdata 인자로 보내기 */

    }

    // download가 완료 되었는지 확인 후 metadata 변경
    /*public void RegisterDownloadManagerReciever(Context context, final String pathdata) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    // Do something on download complete

                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            Log.d("DL LINK", "Call edit Metadata");
                            //editMetadata(pathdata);
                            String decoding = "ISO-8859-1";
                            String encoding = "EUC-KR";
                            File file = new File(pathdata);
                            Tag tag = null;
                            try {
                                MP3File mp3 = null;
                                try {
                                    mp3 = (MP3File) AudioFileIO.read(file);
                                } catch (CannotReadException e) {
                                    e.printStackTrace();
                                } catch (TagException e) {
                                    e.printStackTrace();
                                } catch (ReadOnlyFileException e) {
                                    e.printStackTrace();
                                } catch (InvalidAudioFrameException e) {
                                    e.printStackTrace();
                                }
                                AbstractID3v2Tag tag2 = mp3.getID3v2Tag();

                                tag = mp3.getTag();
                                //Log.d("Tag : " , String.valueOf(tag2));
                                Log.d("Title : " , tag.getFirst(FieldKey.TITLE));
                                Log.d("Artist : " , tag.getFirst(FieldKey.ARTIST));
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }

                    }, 3000);    //3초 뒤에
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }*/

    /*
    public void editMetadata(String pathdata){

        File src = new File(pathdata);
        MusicMetadataSet src_set = null;
        try {
            Log.d("DL LINK", "new MyID3().read()");
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            Log.d("DL LINK", "MyID3().read() error");
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "Empty");
        }
        else
        {
            try{  // 정보 가져오기
                IMusicMetadata metadata = src_set.getSimplified();
                String artist = metadata.getArtist();
                String album = metadata.getAlbum();
                String song_title = metadata.getSongTitle();
                //Number track_number = metadata.getTrackNumber();
                Log.i("get song Title", song_title);
                Log.i("get artist", artist);
            }catch (Exception e) {
                e.printStackTrace();
            }
            File dst = new File(pathdata);
            MusicMetadata meta = new MusicMetadata("name");
            meta.setArtist(_artist);     // 아티스트명 변경
            Log.d("set Artist: ", _artist);
            try {
                new MyID3().update(src, src_set, meta);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ID3WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  // write updated metadata
        }
    }*/
}
