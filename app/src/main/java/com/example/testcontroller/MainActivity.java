package com.example.testcontroller;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;

import android.content.res.Configuration;
import android.graphics.Bitmap;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegInputStream;
import com.github.niqdev.mjpeg.MjpegView;
import com.github.niqdev.mjpeg.OnFrameCapturedListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity  implements OnFrameCapturedListener {

    TextView mtv_state;

    Button mb_go_forward;
    Button mb_lotate_left;
    Button mb_lotate_right;
    Button mb_go_backward;
    Button mb_conn_dialog;

    Button mb_take_photo;

    private static final String TAG = "RaspberryPiCamera";
    private final String STREAM_URL = "http://192.168.11.9:8080/?action=stream";
    private MjpegView mjpegView;

    private Bitmap lastPreview = null;

    private final static int RESULT_PICK_IMAGEFILE = 1003;
    private final static int REQUEST_PERMISSION = 1004;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mjpegView = (MjpegView)findViewById(R.id.mjpeg_view);
        mtv_state = (TextView)findViewById(R.id.text_view_state);

        //photo
        mjpegView.setOnFrameCapturedListener(this);

        mb_go_forward = (Button)findViewById(R.id.button_go_forward);
        mb_lotate_left = (Button)findViewById(R.id.button_lotate_left);
        mb_lotate_right = (Button)findViewById(R.id.button_lotate_right);
        mb_go_backward = (Button)findViewById(R.id.button_go_backward);
        mb_conn_dialog = (Button)findViewById(R.id.button_conn_dialog);
        mb_take_photo = (Button)findViewById(R.id.button_take_photo);
        final MainActivity mainActivity = this;
        mb_go_forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SocketClientTask socket = new SocketClientTask(mainActivity);
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 指がタッチした時の処理を記述
                    mtv_state.setText("前進！");
                    mb_lotate_left.setEnabled(false);
                    mb_lotate_right.setEnabled(false);
                    mb_go_backward.setEnabled(false);
                    socket.execute("1");
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチした指が離れた時の処理を記述
                    mtv_state.setText("停止！");
                    mb_lotate_left.setEnabled(true);
                    mb_lotate_right.setEnabled(true);
                    mb_go_backward.setEnabled(true);
                    socket.execute("0");
                }
                return false;
            }
        });

        mb_lotate_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SocketClientTask socket = new SocketClientTask(mainActivity);
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 指がタッチした時の処理を記述
                    mtv_state.setText("左回転！");
                    mb_go_forward.setEnabled(false);
                    mb_lotate_right.setEnabled(false);
                    mb_go_backward.setEnabled(false);
                    socket.execute("4");
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチした指が離れた時の処理を記述
                    mtv_state.setText("停止！");
                    mb_go_forward.setEnabled(true);
                    mb_lotate_right.setEnabled(true);
                    mb_go_backward.setEnabled(true);
                    socket.execute("0");
                }
                return false;
            }
        });

        mb_lotate_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SocketClientTask socket = new SocketClientTask(mainActivity);
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 指がタッチした時の処理を記述
                    mtv_state.setText("右回転！");
                    mb_go_forward.setEnabled(false);
                    mb_lotate_left.setEnabled(false);
                    mb_go_backward.setEnabled(false);
                    socket.execute("3");
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチした指が離れた時の処理を記述
                    mtv_state.setText("停止！");
                    mb_go_forward.setEnabled(true);
                    mb_lotate_left.setEnabled(true);
                    mb_go_backward.setEnabled(true);
                    socket.execute("0");
                }
                return false;
            }
        });

        mb_go_backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SocketClientTask socket = new SocketClientTask(mainActivity);
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 指がタッチした時の処理を記述
                    mtv_state.setText("後退！");
                    mb_go_forward.setEnabled(false);
                    mb_lotate_left.setEnabled(false);
                    mb_lotate_right.setEnabled(false);
                    socket.execute("2");
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチした指が離れた時の処理を記述
                    mtv_state.setText("停止！");
                    mb_go_forward.setEnabled(true);
                    mb_lotate_left.setEnabled(true);
                    mb_lotate_right.setEnabled(true);
                    socket.execute("0");
                }
                return false;
            }
        });
        mb_conn_dialog.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                ConnectionDialogFragment dialog = new ConnectionDialogFragment();
                dialog.show(getFragmentManager(), "ConnectionDialog");
            }
        });
        mb_take_photo.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (lastPreview != null){
                            if(Build.VERSION.SDK_INT >= 23){
                                checkPermission();
                            }else {
                                saveIntent();
                            }
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        loadIpCam();
    }
    private void loadIpCam() {
        final MainActivity mainActivity = this;
        Log.d(TAG, "Connecting");
        Mjpeg.newInstance()
                .open(STREAM_URL, 30)
                .subscribe(new Subscriber<MjpegInputStream>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Subscription Completed");
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        Toast.makeText(mainActivity, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onNext(MjpegInputStream mjpegInputStream) {
                        mjpegView.setSource(mjpegInputStream);
                        mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                        mjpegView.showFps(true);

                    }
                });
    }
    private String getPreference(String key){
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(key, "");
    }
    private DisplayMode calculateDisplayMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE ?
                DisplayMode.FULLSCREEN : DisplayMode.BEST_FIT;
    }
    @Override
    protected void onPause(){
        super.onPause();
        mjpegView.stopPlayback();
    }
    @Override
    public void onFrameCaptured(Bitmap bitmap){
        lastPreview = bitmap;
    }
    //RuntimePermissionCheck
    private  void checkPermission(){
        //すでに許可している
        if (ActivityCompat.checkSelfPermission(this ,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            saveIntent();
        }
        //拒否していた場合
        else {
            requestPermission();
        }
    }
    //許可の設定
    private  void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            Toast toast = Toast.makeText(this,
                    "許可されないとアプリが実行できません",
                    Toast.LENGTH_SHORT);
            toast.show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }
    //ファイル保存の呼び出し
    private  void saveIntent(){
        Log.d("debug","saveIntent()");
        File editFolder = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "IMG");
        editFolder.mkdirs();
        //保存ファイル名
        String fileName = new SimpleDateFormat(
                "ddHHmmss", Locale.US).format(new Date());
        filePath = String.format("%s%s.jpg", editFolder.getPath(),fileName);
        Log.d("debug","filePath"+filePath);
        //画像のファイルパス
        try(FileOutputStream output = new FileOutputStream(filePath)){
            lastPreview.compress(Bitmap.CompressFormat.JPEG,100,output);
            output.flush();
            output.close();
            registerDatabase(filePath);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    //アンドロイドのデータベースへ登録する
    private void registerDatabase(String file){
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        contentValues.put("_data",file);
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }
}

