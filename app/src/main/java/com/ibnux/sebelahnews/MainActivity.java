package com.ibnux.sebelahnews;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    ImageView imgAuthor, imgPilihan;
    EditText txtAuthor, txtTgl, txtDisklaimer,txtJudul,txtCerita;
    Button btnShare, btnSave,btnFolder;
    LinearLayout layoutberita;
    String folderName = "sebelahNews";
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
        txtJudul = findViewById(R.id.txtJudul);
        txtCerita = findViewById(R.id.txtCerita);
        btnFolder = findViewById(R.id.btnFolder);

        SharedPreferences sp = getSharedPreferences("pengaturan",0);
        txtAuthor.setText(sp.getString("author","Admin Sebelah"));
        txtDisklaimer.setText(sp.getString("disklaimer","*Gambar hanyalah ilustrasi semata, sangat tolol jika dipercaya"));

        Calendar cal = Calendar.getInstance();

        txtTgl.setText(cal.get(Calendar.DAY_OF_MONTH)+" "+bulan[cal.get(Calendar.MONTH)]+" "+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+" DC");

        btnShare.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgAuthor.setOnClickListener(this);
        imgPilihan.setOnClickListener(this);
        btnFolder.setOnClickListener(this);
        txtJudul.setOnLongClickListener(this);
        txtCerita.setOnLongClickListener(this);


        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode==1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(MainActivity.this, "Permission granted to read/write your External storage", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied to read/write your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnSave) {
            txtJudul.requestFocus();
            txtJudul.clearFocus();
            saveBitMap();
        } else if (v == btnShare) {
            txtJudul.requestFocus();
            txtJudul.clearFocus();
            shareFile(saveBitMap());
        } else if (v == imgAuthor) {
            if (author == 0) {
                author = 1;
                imgAuthor.setImageResource(R.drawable.cewek);
            } else {
                author = 0;
                imgAuthor.setImageResource(R.drawable.cowok);
            }
        } else if (v == imgPilihan) {
            ImagePicker.Companion.with(this).start(222);
        }else if(v==btnFolder){
            openFolder();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == txtJudul) {
            resizeFont(txtJudul);
            return true;
        } else if (v == txtCerita) {
            resizeFont(txtCerita);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==222 && resultCode == RESULT_OK){
            CropImage.activity(data.getData())
                    .setAspectRatio(4,3)
                    .start(this);
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgPilihan.setImageURI(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void openFolder(){
        String path = "file://" + new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName).getPath();
        Log.i("TAG", path);
        Uri mydir = Uri.parse(path);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(mydir, "resource/folder");
            startActivity(intent);
        }catch (Exception e1){
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(mydir,
                        DocumentsContract.Document.MIME_TYPE_DIR);
                startActivity(intent);
            }catch (Exception e2){
                try{
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(mydir,"*/*");
                    startActivity(intent);
                }catch (Exception e3){
                    Toast.makeText(this, "Tidak ada aplikasi yang bisa buka folder", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void resizeFont(final EditText editText){

        float px = editText.getTextSize();
        int sp = (int)(px / getResources().getDisplayMetrics().scaledDensity);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ukuran huruf");
        builder.setIcon(R.mipmap.ic_launcher);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_PHONE);
        input.setText(sp+"");
        input.setSelectAllOnFocus(true);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String hasil = input.getText().toString();
                try{
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,Float.parseFloat(hasil));
                }catch (Exception e){
                    //bodoamat
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    // https://stackoverflow.com/questions/17985646/android-sharing-files-by-sending-them-via-email-or-other-apps
    private void shareFile(Uri file) {
        try {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.putExtra(Intent.EXTRA_STREAM,file);
            intentShareFile.setData(file);
            startActivity(Intent.createChooser(intentShareFile, "Share Hoaks"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal share hoaks", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue share the image.");
        }
    }

    // https://stackoverflow.com/questions/10374547/how-to-convert-a-linearlayout-to-image
    private Uri saveBitMap() {
        getSharedPreferences("pengaturan",0).edit()
                .putString("author",txtAuthor.getText().toString())
                .putString("disklaimer",txtDisklaimer.getText().toString())
                .apply();

        String filename = folderName.toLowerCase() + "_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;
        Uri imageUri = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,  Environment.DIRECTORY_PICTURES+"/" + folderName);
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).toString() + File.separator + folderName;
                File file = new File(imagesDir);
                if (!file.exists()) {
                    file.mkdir();
                }
                File img = new File(imagesDir, filename);
                fos = new FileOutputStream(img);
                imageUri = FileProvider.getUriForFile(this, getPackageName(), img);
            }
            Log.i("TAG", imageUri.getPath());
            Bitmap bitmap = getBitmapFromView(layoutberita);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            scanGallery(imageUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan Gambar", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue saving the image.");
        }

        return imageUri;
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
    private void scanGallery(String filename) {
        if (Build.VERSION.SDK_INT < 19) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + filename)));
        }
        else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + filename)));
        }
    }



}