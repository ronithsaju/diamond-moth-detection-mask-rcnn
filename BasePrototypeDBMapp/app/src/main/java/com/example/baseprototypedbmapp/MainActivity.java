package com.example.baseprototypedbmapp;

//THIS APPLICATION IS A WORK IN PROGRESS

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baseprototypedbmapp.ml.Linear;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    private ImageView imgView;
    private Button select, capture, predict;
    private TextView textView;
    public static final int CAMERA_ACTION_CODE = 200; //request code to call the camera function, used in the capture event below
    private Bitmap img;
    private Bitmap mrcnnimage;
    Interpreter tflite;
    private int inputSize = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //objects for the UI
        imgView = findViewById(R.id.imageView);  //image display
        select = findViewById(R.id.selectButton); //select button
        capture = findViewById(R.id.captureButton); //camera button
        predict = findViewById(R.id.predictButton); //predict button
        textView = findViewById(R.id.textView); //textbox for the display of the binary classifier

        //event for when the selection button is clicked on the app
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(Intent.ACTION_GET_CONTENT);
                intent = intent.setType("image/*");
                startActivityForResult(intent, 100);
                textView.setText("\n");
            }
        });

        //event for when the prediction button is clicked on the app
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 *Binary Classifier (fully working)
                 */


                img = Bitmap.createScaledBitmap(img, 150, 150, true); //set input bitmap as 150x150

                try {
                    Linear model = Linear.newInstance(getApplicationContext());  //create instance for Binary Classifier model

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 150, 150, 3}, DataType.FLOAT32);
                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(img);
                    ByteBuffer byteBuffer = tensorImage.getBuffer();
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Linear.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    // Releases model resources if no longer used.
                    model.close();

                    //determine insect as DBM or non-DBM using the confidence score predicted by the model
                    if (outputFeature0.getFloatArray()[0] >= 0.5)
                        textView.setText("non-DBM\n");              //set the textbox in the app to display result
                    else if (outputFeature0.getFloatArray()[0] < 0.5)
                        textView.setText("DBM\n");
                } catch (IOException e) {
                    // TODO Handle the exception
                }

            }
        });

        //event for when the camera button is clicked on the app
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initialize the camera function
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //set request code to sent signal to start the mobile phone's camera function, refer to the onActivity result at the bottom
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_ACTION_CODE);
                }
            }
        });
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //this code will run once the request code is 100, which is after the user click the select button, this request is sent from the select click event
        if (requestCode == 100) {
            imgView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //this code will run when the request code is the corresponding values, after the capture button is clicked, signalled from the capture click event above
        if (requestCode == CAMERA_ACTION_CODE &&  resultCode == RESULT_OK) {
            assert data != null;  //check if the data is not empty
            Bundle bundle = data.getExtras();
            Bitmap finalPhoto = (Bitmap) bundle.get("data");
            imgView.setImageBitmap(finalPhoto); //display the captured image on the application
            img = Bitmap.createScaledBitmap(finalPhoto, 150, 150,true);
            //rescaled to 150x150 for the prototype using only the Binary Classifier
            //change accordingly when Mask R-CNN model is fully operational
            //for the capturing of a trap image to be passed into the Mask R-CNN model instead of prepared for the Binary Classifier in this prototype
        }
    }
}

