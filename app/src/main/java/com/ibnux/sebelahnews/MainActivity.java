package com.ibnux.sebelahnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imgAuthor, imgPilihan;
    EditText txtAuthor, txtTgl, txtDisklaimer;
    Button btnShare, btnSave;
    LinearLayout layoutberita;
    String folderName = "SebelahNews";
    int author = 0;
    String[] bulan = new String[]{"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgAuthor = findViewById(R.id.imgAuthor);
        imgPilihan = findViewById(R.id.imgPilihan);
        txtAuthor = findViewById(R.id.txtAuthor);
        txtTgl = findViewById(R.id.txtTgl);
        btnShare = findViewById(R.id.btnShare);
        btnSave = findViewById(R.id.btnSave);
        layoutberita = findViewById(R.id.layoutberita);
        txtDisklaimer = findViewById(R.id.txtDisklaimer);

        SharedPreferences sp = getSharedPreferences("pengaturan",0);
        txtAuthor.setText(sp.getString("author","Admin Sebelah"));
        txtDisklaimer.setText(sp.getString("disklaimer","*Gambar hanyalah ilustrasi semata, sangat tolol jika dipercaya"));

        Calendar cal = Calendar.getInstance();

        txtTgl.setText(cal.get(Calendar.DAY_OF_MONTH)+" "+bulan[cal.get(Calendar.MONTH)]+" "+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+" DC");

        btnShare.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgAuthor.setOnClickListener(this);
        imgPilihan.setOnClickListener(this);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode==1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted to read/write your External storage", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied to read/write your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnSave) {
            saveBitMap();
        } else if (v == btnShare) {
            shareFile(saveBitMap());
        } else if (v == imgAuthor) {
            if(author==0){
                author=1;
                imgAuthor.setImageResource(R.drawable.cewek);
            }else{
                author=0;
                imgAuthor.setImageResource(R.drawable.cowok);
            }
        } else if (v == imgPilihan) {
        }
    }

    // https://stackoverflow.com/questions/17985646/android-sharing-files-by-sending-them-via-email-or-other-apps
    private void shareFile(File file) {
        try {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
            intentShareFile.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("file://" + file.getAbsolutePath()));
            startActivity(Intent.createChooser(intentShareFile, "Share File"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal share Gambar", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue share the image.");
        }
    }

    // https://stackoverflow.com/questions/10374547/how-to-convert-a-linearlayout-to-image
    private File saveBitMap() {
        getSharedPreferences("pengaturan",0).edit()
                .putString("author",txtAuthor.getText().toString())
                .putString("disklaimer",txtDisklaimer.getText().toString())
                .apply();

        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if (!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() + File.separator + folderName.toLowerCase() + "_" + System.currentTimeMillis() + ".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(layoutberita);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
            Toast.makeText(this, "Sukses menyimpan Gambar", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan Gambar", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery(pictureFile.getAbsolutePath());
        return pictureFile;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    // used for scanning gallery
    private void scanGallery(String path) {
        try {
            MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}