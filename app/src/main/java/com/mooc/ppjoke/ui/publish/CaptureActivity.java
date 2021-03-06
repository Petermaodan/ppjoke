package com.mooc.ppjoke.ui.publish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.ActivityLayoutCaptureBinding;
import com.mooc.ppjoke.view.RecordView;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CaptureActivity extends AppCompatActivity {


    public static final int REQ_CAPTURE=10001;
    private ActivityLayoutCaptureBinding mBinding;
    private static final String[] PERMISSION=new String[]{Manifest.permission.CAMERA};
    private static final int PERMISSION_CODE=1000;
    private boolean takingPicture;
    private CameraX.LensFacing mLensFacing=CameraX.LensFacing.BACK;
    private int rotation= Surface.ROTATION_0;
    private Size resolution=new Size(1280,720);
    private Rational rational=new Rational(9,16);
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private TextureView textureView;
    private String outputFilePath;

    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";


    public static void startActivityForResult(Activity activity){
        Intent intent=new Intent(activity,CaptureActivity.class);
        activity.startActivityForResult(intent,REQ_CAPTURE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= DataBindingUtil.setContentView(this,R.layout.activity_layout_capture);
        ActivityCompat.requestPermissions(this,PERMISSION,PERMISSION_CODE);
        mBinding.recordView.setOnRecordListener(new RecordView.onRecordListener() {
            @Override
            public void onClick() {
                takingPicture=true;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                mBinding.captureTips.setVisibility(View.INVISIBLE);
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        onFileSaved(file)

                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });
    }

    private void showErrorToast(String message){
        if (Looper.myLooper()==Looper.getMainLooper()){
            Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show();
        }else {
            runOnUiThread(()-> Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show());
        }
    }


    private void onFileSaved(File file) {
        outputFilePath=file.getAbsolutePath();
        String mimeType=takingPicture?"image/jpeg":"video/mp4";
        MediaScannerConnection.scanFile(this,new String[]{outputFilePath},new String[]{mimeType},null);
        PreviewActivity
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PreviewActivity.REQ_PREVIEW&&resultCode==RESULT_OK){
            Intent intent=new Intent();
            intent.putExtra(RESULT_FILE_PATH,outputFilePath);
            //????????????????????????????????????????????? ??????????????????????????????
            intent.putExtra(RESULT_FILE_WIDTH,resolution.getHeight());
            intent.putExtra(RESULT_FILE_HEIGHT,resolution.getWidth());
            intent.putExtra(RESULT_FILE_TYPE,!takingPicture);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    //????????????????????????????????????????????????
    @SuppressLint("RestrictedApi")
    private void bindCameraX(){
        CameraX.unbindAll();

        //?????????????????????????????????????????????(?????????????????????)????????????
        boolean hasAvailableCameraId=false;
        try {
            hasAvailableCameraId=CameraX.hasCameraWithLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }

        if (!hasAvailableCameraId){
            showErrorToast("??????????????????cameraId!,???????????????????????????????????????");
            finish();
            return;
        }
        //?????????????????????????????????cameraId.?????????????????????"0"????????????"1"
        String cameraIdForLensFacing=null;
        try {
            cameraIdForLensFacing=CameraX.getCameraFactory().cameraIdForLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(cameraIdForLensFacing)){
            showErrorToast("??????????????????cameraId!,???????????????????????????????????????");
            finish();
            return;
        }

        PreviewConfig config=new PreviewConfig.Builder()
                //???????????????
                .setLensFacing(mLensFacing)
                //????????????
                .setTargetRotation(rotation)
                //?????????
                .setTargetResolution(resolution)
                //?????????
                .setTargetAspectRatio(rational)
                .build();
        preview=new Preview(config);

        //???????????????imageCapture??????????????????????????????VideoCapture????????????????????????
        imageCapture=new ImageCapture(new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(rational)
                .setTargetResolution(resolution)
                .setLensFacing(mLensFacing)
                .setTargetRotation(rotation).build());

        videoCapture = new VideoCapture(new VideoCaptureConfig.Builder()
                .setTargetRotation(rotation)
                .setLensFacing(mLensFacing)
                .setTargetResolution(resolution)
                .setTargetAspectRatio(rational)
                //????????????
                .setVideoFrameRate(25)
                //bit???
                .setBitRate(3 * 1024 * 1024).build());

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                textureView=mBinding.textureView;
                ViewGroup parent= (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView,0);

                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });
        //?????????????????????????????????????????????
        List<UseCase> newUseList=new ArrayList<>();
        newUseList.add(preview);
        newUseList.add(imageCapture);
        newUseList.add(videoCapture);
        //??????????????????????????? ????????????????????????????????????????????????????????????????????? ??????????????????usecase

        Map<UseCase,Size> resolutions=CameraX.getSurfaceManager().getSuggestedResolutions(cameraIdForLensFacing,null,newUseList);
        Iterator<Map.Entry<UseCase,Size>> iterator=resolutions.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<UseCase,Size> next=iterator.next();
            UseCase useCase=next.getKey();
            Size value=next.getValue();
            Map<String,Size> update=new HashMap<>();
            update.put(cameraIdForLensFacing,value);
            useCase.updateSuggestedResolution(update);
        }
        CameraX.bindToLifecycle(this,preview,imageCapture,videoCapture);



    }
}