package com.bulusanraya.codefest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    ImageView imgThumbnail;
    TextView textNoImage;
    FloatingActionButton fabAddGallery;
    FloatingActionButton fabAddPhoto;
    Uri uri;

    Button btnSubmit;

    String mCurrentFilePath;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int SELECT_FILE = 99;

    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get All View
        imgThumbnail = (ImageView)findViewById(R.id.img_thumbnail);
        textNoImage = (TextView)findViewById(R.id.textNoFileChoosed);
        fabAddGallery = (FloatingActionButton)findViewById(R.id.btn_add_gallery);
        fabAddPhoto = (FloatingActionButton)findViewById(R.id.btn_add_photo);
        btnSubmit = (Button)findViewById(R.id.btn_submit);

        fabAddGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "KLIKED GALLERY", Toast.LENGTH_SHORT).show();
                getPictureFromGallery();
            }
        });

        fabAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "KLIKED CAMERA", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launchResultActivity();
                uploadImage();

            }
        });

    }

    public void getPictureFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentFilePath = image.getAbsolutePath();
        /*Log.d("PATH", mCurrentFilePath);
        Toast.makeText(getApplicationContext(), mCurrentFilePath, Toast.LENGTH_LONG).show();*/
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getApplicationContext(), "Sorry capture image failed...", Toast.LENGTH_SHORT).show();
            }
            uri = Uri.fromFile(photoFile);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                //Add to photo gallery
                galleryAddPic();

                //set image to imageview (imgThumbnail)
                //setPic();
            }
        }else {
            Toast.makeText(getApplicationContext(), "Your device does not have camera", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
           /* Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            //imgThumbnail.setImageBitmap(imageBitmap);*/
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }catch (IOException e){
                e.printStackTrace();
            }
            setPic();
            textNoImage.setText("");
            btnSubmit.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), uri.toString(), Toast.LENGTH_SHORT).show();
        }else if(requestCode == SELECT_FILE && resultCode == RESULT_OK){
            Uri filePath = data.getData();
            Toast.makeText(getApplicationContext(), filePath.toString(), Toast.LENGTH_LONG).show();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            }catch (IOException e){
                e.printStackTrace();
            }
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                mCurrentFilePath = RealPath.getRealPathFromURI_BelowAPI11(this, data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                mCurrentFilePath = RealPath.getRealPathFromURI_API11to18(this, data.getData());

                // SDK > 19 (Android 4.4)
            else
                mCurrentFilePath = RealPath.getRealPathFromURI_API19(this, data.getData());

            setPic();
            textNoImage.setText("");
            btnSubmit.setVisibility(View.VISIBLE);
            /*Toast.makeText(getApplicationContext(), realPath, Toast.LENGTH_LONG).show();
            Log.d("REAL PATH", realPath);*/
        }
    }

    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Submitting...","Please wait...",false,false);
        String FILE_UPLOAD_URL = "http://192.168.43.153/competition/codefest/upload2.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FILE_UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        //Toast.makeText(MainActivity.this, s , Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("RESPONSE", s);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        Toast.makeText(MainActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("image", image);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentFilePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imgThumbnail.getWidth();
        int targetH = imgThumbnail.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentFilePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmapToShow = BitmapFactory.decodeFile(mCurrentFilePath, bmOptions);
        imgThumbnail.setImageBitmap(bitmapToShow);
    }

    private void launchResultActivity(){
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("PATH", uri.getPath());
        startActivity(intent);
    }
}
