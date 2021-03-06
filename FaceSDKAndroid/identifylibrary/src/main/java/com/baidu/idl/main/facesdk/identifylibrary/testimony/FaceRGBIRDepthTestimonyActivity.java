package com.baidu.idl.main.facesdk.identifylibrary.testimony;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.identifylibrary.BaseOrbbecActivity;
import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.identifylibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.identifylibrary.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.identifylibrary.camera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.identifylibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.identifylibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.identifylibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.setting.IdentifySettingActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.DensityUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ImageUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.ToastUtils;
import com.baidu.idl.main.facesdk.identifylibrary.view.PreviewTexture;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;
import org.openni.android.OpenNIView;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class FaceRGBIRDepthTestimonyActivity extends BaseOrbbecActivity implements OpenNIHelper.DeviceOpenListener,
        View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int DEPTH_NEED_PERMISSION = 33;
    private volatile boolean secondFeatureFinished = false;
    private volatile boolean firstFeatureFinished = false;

    // RGB????????????????????????
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth????????????????????????
    private static final int DEPTH_WIDTH = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private static final int DEPTH_HEIGHT = SingleBaseConfig.getBaseConfig().getDepthHeight();

    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private Context mContext;
    private RelativeLayout depthRl;

    private TextView testimonyPreviewTv;
    private TextView testimonyDevelopmentTv;
    private ImageView testimonyPreviewLineIv;
    private ImageView testimonyDevelopmentLineIv;

    // ???????????????
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    private AutoTexturePreviewView rgbTexture;
    private TextureView draw_detect_face_view;

    // ???????????????
    private Device mDevice;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;

    // ???????????????????????????
    private boolean initOk = false;
    // ???????????????????????????
    private boolean exit = false;
    private Object sync = new Object();

    // ?????????????????????
    private static int cameraType;
    private Thread thread;
    private OpenNIView mDepthGLView;
    private TextureView irTexture;

    // ?????????????????????
    private volatile byte[] rgbData;
    private volatile byte[] depthData;
    private volatile byte[] irData;
    private ImageView rgb_depth_test_view;

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];


    private static final int PICK_PHOTO_FRIST = 100;

    private TextView tv_depth_live_time;
    private TextView tv_depth_live_score;
    private RelativeLayout kaifaRelativeLayout;
    private ImageView depthAddIv;
    private TextView depthUpload_filesTv;
    private RelativeLayout depthShowRl;
    private TextView depthShowAgainTv;
    private TextView hintAdainTv;
    private ImageView hintShowIv;
    private ImageView depthShowImg;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;


    // ?????????????????????????????????????????????????????????
    boolean isDevelopment = false;
    private RelativeLayout rgb_depth_test_ll;
    private RelativeLayout depth_test_rl;
    private RelativeLayout test_nir_rl;
    private TextView depthBaiduTv;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private TextView tv_feature_time;
    private TextView tv_feature_search_time;
    private TextView tv_all_time;
    float score = 0;
    private ImageView rgb_test_iv;
    private RelativeLayout developmentAddRl;
    private RelativeLayout hintShowRl;
    private ImageView depth_test_iv;
    private ImageView test_nir_iv;
    private float rgbLiveScore;
    private float depthLiveScore;
    private float nirLiveScore;

    // ?????????????????????
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float depthLivenessScore;
    private float rgbLivenessScore;
    private float nirLivenessScore;
    // ????????????
    private long endCompareTime;
    // ????????????
    private long featureTime;
    private RelativeLayout personButtomLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_face_rgbirdepth_identifylibrary);
        mContext = this;
        PreferencesUtil.initPrefs(this);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();

        initView();
        // ????????????
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // ????????????
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // ?????????????????????????????????
        if (displayHeight < displayWidth) {
            // ?????????
            int height = displayHeight;
            // ?????????
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // ????????????????????????
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // ??????????????????
            params.gravity = Gravity.CENTER;
            depthRl.setLayoutParams(params);
        }

    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    FaceSDKManager.initModelSuccess = true;
                    ToastUtils.toast(FaceRGBIRDepthTestimonyActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceRGBIRDepthTestimonyActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    private void initView() {
        // ???????????????
        depthRl = findViewById(R.id.depth_Rl);
        // ****************title****************
        // ??????
        ImageView testimonyBackIv = findViewById(R.id.btn_back);
        testimonyBackIv.setOnClickListener(this);
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        testimonyDevelopmentTv = findViewById(R.id.develop_text);
        testimonyDevelopmentTv.setOnClickListener(this);
        testimonyDevelopmentLineIv = findViewById(R.id.develop_view);
        // ??????
        ImageView testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);

        // ****************????????????****************
        depthShowRl = findViewById(R.id.testimony_showRl);
        depthShowImg = findViewById(R.id.testimony_showImg);
        // ????????????
        depthShowAgainTv = findViewById(R.id.testimony_showAgainTv);

        depthShowAgainTv.setOnClickListener(this);
        depthBaiduTv = findViewById(R.id.depth_baiduTv);


        // ****************????????????****************
        // ???????????????????????????
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        mDepthGLView.setVisibility(View.VISIBLE);
        depth_test_rl = findViewById(R.id.depth_test_Rl);
        // ???????????????IR ????????????
        irTexture = findViewById(R.id.texture_preview_ir);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irTexture.setRotationY(180);
        }
        // nir
        test_nir_rl = findViewById(R.id.test_nir_Rl);
        test_nir_rl.setVisibility(View.GONE);
        test_nir_iv = findViewById(R.id.test_nir_iv);
        // RGB ??????????????????
        rgb_depth_test_view = findViewById(R.id.rgb_depth_test_view);
        rgb_depth_test_ll = findViewById(R.id.test_rgb_rl);
        // ???????????????RGB ????????????
        rgbTexture = findViewById(R.id.auto_camera_preview_view);
        // ????????????????????????
        draw_detect_face_view = findViewById(R.id.draw_detect_face_view);
        draw_detect_face_view.setOpaque(false);
        draw_detect_face_view.setKeepScreenOn(true);

        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        hintShowIv = findViewById(R.id.hint_showIv);
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);

        // ****************buttom****************
        personButtomLl = findViewById(R.id.person_buttomLl);
        depthAddIv = findViewById(R.id.testimony_addIv);
        depthAddIv.setOnClickListener(this);
        depthUpload_filesTv = findViewById(R.id.testimony_upload_filesTv);

        // ????????????
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();

        // ????????????
        livenessTipsFailRl = findViewById(R.id.testimony_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.testimony_tips_failIv);

        view = findViewById(R.id.mongolia_view);

        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);

        // ???????????????
        tv_depth_live_time = findViewById(R.id.tv_rgb_live_time);
        // ??????????????????
        tv_depth_live_score = findViewById(R.id.tv_rgb_live_score);
        // ??????????????????
        tv_feature_time = findViewById(R.id.tv_feature_time);
        // ??????????????????
        tv_feature_search_time = findViewById(R.id.tv_feature_search_time);
        // ?????????
        tv_all_time = findViewById(R.id.tv_all_time);
        rgb_test_iv = findViewById(R.id.rgb_test_iv);
        developmentAddRl = findViewById(R.id.Development_addRl);
        ImageView DevelopmentAddTv = findViewById(R.id.Development_addIv);
        DevelopmentAddTv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        depth_test_iv = findViewById(R.id.depth_test_iv);

        // RGB ??????
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // depth ??????
        depthLiveScore = SingleBaseConfig.getBaseConfig().getDepthLiveScore();
        // nir ??????
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();

        // ????????????
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irTexture);
        }
    }

    /**
     * ???device ?????????????????????USB ??????
     *
     * @param device
     */
    private void initUsbDevice(UsbDevice device) {

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }
        this.mDevice = null;
        // Find mDevice ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.mDevice = Device.open();
                break;
            }
        }

        if (this.mDevice == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void startCameraPreview() {
        // ?????????????????????
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // ?????????????????????
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        CameraPreviewManager.getInstance().startPreview(this, rgbTexture,
                RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        if (depthShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                            firstFeatureFinished = false;
                            // ????????????
                            dealRgb(rgbData);
                        } else {
                            rgb_depth_test_view.setImageResource(R.mipmap.ic_image_video);
                            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                            animator.setDuration(3000);
                            view.setBackgroundColor(Color.parseColor("#ffffff"));
                            animator.start();
                        }
                    }
                });

//        boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
//        if (isRGBDisplay) {
//            showDetectImage(rgbData);
//        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        // ??????
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
            // ??????
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // ??????????????????
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
            // ????????????
        } else if (id == R.id.preview_text) {
            if (depthShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = false;
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            rgb_depth_test_ll.setVisibility(View.GONE);
            depth_test_rl.setVisibility(View.GONE);
            personButtomLl.setVisibility(View.VISIBLE);
            kaifaRelativeLayout.setVisibility(View.GONE);
            depthBaiduTv.setVisibility(View.VISIBLE);
            mDepthGLView.setVisibility(View.INVISIBLE);
            irTexture.setAlpha(0);
            test_nir_rl.setVisibility(View.GONE);
            // ????????????
        } else if (id == R.id.develop_text) {
            if (depthShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = true;
            testimonyDevelopmentTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewLineIv.setVisibility(View.GONE);
            testimonyDevelopmentLineIv.setVisibility(View.VISIBLE);
            rgb_depth_test_ll.setVisibility(View.VISIBLE);
            depth_test_rl.setVisibility(View.VISIBLE);
            personButtomLl.setVisibility(View.GONE);
            kaifaRelativeLayout.setVisibility(View.VISIBLE);
            depthBaiduTv.setVisibility(View.GONE);
            mDepthGLView.setVisibility(View.VISIBLE);
            irTexture.setAlpha(1);
            test_nir_rl.setVisibility(View.VISIBLE);
        } else if (id == R.id.testimony_addIv) {
            // ??????????????????
            secondFeatureFinished = false;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
        } else if (id == R.id.testimony_showAgainTv) {
            // ??????????????????
            secondFeatureFinished = false;
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent1, PICK_PHOTO_FRIST);
        } else if (id == R.id.hint_adainTv) {
            // ??????????????????
            secondFeatureFinished = false;
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
        } else if (id == R.id.Development_addIv) {
            // ??????????????????
            secondFeatureFinished = false;
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        }
    }

    // ???????????????
    private int mCameraNum;
    // RGB+IR ??????
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                }else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParams = irTexture.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = w;
                    layoutParams.width = h;
                    // ??????90?????????270?????????????????????
                } else {
                    layoutParams.height = h;
                    layoutParams.width = w;
                }
                irTexture.setLayoutParams(layoutParams);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
                // ?????????????????????
                startCameraPreview();
                // ????????? ???????????????
                exit = false;
                mOpenNIHelper = new OpenNIHelper(this);
                mOpenNIHelper.requestDeviceOpen(this);
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        CameraPreviewManager.getInstance().stopPreview();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
            mOpenNIHelper = null;
        }
        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCameraNum >= 2) {
                    if (mCamera[i] != null) {
                        mCamera[i].setPreviewCallback(null);
                        mCamera[i].stopPreview();
                        mPreview[i].release();
                        mCamera[i].release();
                        mCamera[i] = null;
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
            mOpenNIHelper = null;
        }
    }


    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }

    private void dealIr(byte[] data) {
        irData = data;
        checkData();
    }

    private void dealDepth(byte[] data) {
        depthData = data;
        checkData();
    }

    private synchronized void checkData() {
        if (rgbData != null && depthData != null && irData != null) {
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, depthData, RGB_HEIGHT,
                    RGB_WIDTH, 4, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(final LivenessModel livenessModel) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // ????????????
                                    checkCloseDebugResult(livenessModel);
                                    // ????????????
                                    checkOpenDebugResult(livenessModel);
                                }


//                            }
                            });
                        }

                        @Override
                        public void onTip(int code, String msg) {
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
            rgbData = null;
            depthData = null;
            irData = null;
        }

    }

    /**
     * ???????????????????????????????????????
     *
     * @return
     */
    private Bitmap getBitmap() {
        Intent intent = getIntent();
        byte[] imageBitmaps = intent.getByteArrayExtra("imageBitmap");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBitmaps, 0, imageBitmaps.length);
        if (bitmap != null) {
            return bitmap;
        }
        return null;
    }

    /**
     * bitmap -???????????????
     *
     * @param bitmap
     * @param feature
     * @param index
     */
    private void syncFeature(final Bitmap bitmap, final byte[] feature
            , final int index, boolean isFromPhotoLibrary) {
        float ret = -1;
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);

        FaceInfo[] faceInfos = null;
        int count = 10;
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        while (count != 0) {
            faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                    .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
            count--;
            if (faceInfos != null) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // ??????????????????
        if (faceInfos != null && faceInfos.length > 0) {
            // ?????????????????????
            isFace = false;
            // ???????????????????????????
            depthShowImg.setVisibility(View.VISIBLE);
            hintShowIv.setVisibility(View.VISIBLE);
            // ??????????????????????????????????????????????????????
//            if (qualityCheck(faceInfos[0], isFromPhotoLibrary)) {
            ret = FaceSDKManager.getInstance().getFaceFeature().feature(BDFaceSDKCommon.FeatureType.
                    BDFACE_FEATURE_TYPE_ID_PHOTO, rgbInstance, faceInfos[0].landmarks, feature);
            if (ret == 128 && index == 2) {
                secondFeatureFinished = true;
            }
            if (ret == 128) {
                toast("??????" + index + "??????????????????");
                developmentAddRl.setVisibility(View.GONE);
                depthUpload_filesTv.setVisibility(View.GONE);
                depthAddIv.setVisibility(View.GONE);
                hintShowRl.setVisibility(View.VISIBLE);
                depthShowRl.setVisibility(View.VISIBLE);
            } else {
                toast("???????????????????????????");
            }
        } else {
            isFace = true;
            // ???????????????????????????
            depthShowImg.setVisibility(View.GONE);
            hintShowIv.setVisibility(View.GONE);
            developmentAddRl.setVisibility(View.GONE);
            depthUpload_filesTv.setVisibility(View.GONE);
            depthAddIv.setVisibility(View.GONE);
            hintShowRl.setVisibility(View.VISIBLE);
            depthShowRl.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ???????????????????????????????????????????????????sdk???????????????????????????????????????????????????????????????????????????????????????
     *
     * @param rgb
     */
    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rgb_depth_test_view.setVisibility(View.VISIBLE);
                rgb_depth_test_view.setImageBitmap(bitmap);
            }
        });

        // ???????????????????????????????????????????????????????????????????????????
        rgbInstance.destory();

    }


    @Override
    public void onDeviceOpened(UsbDevice device) {
        initUsbDevice(device);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
                if (cameraType == 1) {
                    if (x == DEPTH_HEIGHT && y == DEPTH_WIDTH && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                } else {
                    if (x == DEPTH_WIDTH && y == DEPTH_HEIGHT && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                }

            }
            startThread();
        }
    }

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onDeviceNotFound() {

    }

    /**
     * ??????????????????????????????
     */
    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(mDepthStream);
                mDepthStream.start();
                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        if (mDepthStream != null) {
                            mDepthGLView.update(mDepthStream);
                            VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                            ByteBuffer depthByteBuf = videoFrameRef.getData();
                            if (depthByteBuf != null) {
                                int depthLen = depthByteBuf.remaining();
                                byte[] depthByte = new byte[depthLen];
                                depthByteBuf.get(depthByte);
                                dealDepth(depthByte);
                            }
                            videoFrameRef.release();
                        }
                    }

                }
            }
        };

        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
//                    syncFeature(bitmap, secondFeature, 2, true);
                    // ???????????????
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature, this);
                    depthShowImg.setVisibility(View.VISIBLE);
                    hintShowIv.setVisibility(View.VISIBLE);
                    depthShowImg.setImageBitmap(bitmap);
                    hintShowIv.setImageBitmap(bitmap);

                    if (ret != -1) {
                        isFace = false;
                        // ??????????????????????????????????????????????????????
                        if (ret == 128) {
                            secondFeatureFinished = true;
                        }
                        if (ret == 128) {
                            toast("????????????????????????");
                            developmentAddRl.setVisibility(View.GONE);
                            depthUpload_filesTv.setVisibility(View.GONE);
                            depthAddIv.setVisibility(View.GONE);
                            hintShowRl.setVisibility(View.VISIBLE);
                            depthShowRl.setVisibility(View.VISIBLE);
                        } else {
                            ToastUtils.toast(mContext, "????????????????????????");
                        }
                    } else {
                        isFace = true;
                        // ???????????????????????????
                        depthShowImg.setVisibility(View.GONE);
                        hintShowIv.setVisibility(View.GONE);
                        developmentAddRl.setVisibility(View.GONE);
                        depthUpload_filesTv.setVisibility(View.GONE);
                        depthAddIv.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        depthShowRl.setVisibility(View.VISIBLE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // ????????????
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    livenessTipsFailRl.setVisibility(View.GONE);

                    if (testimonyPreviewLineIv.getVisibility() == View.VISIBLE) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                        animator.setDuration(3000);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                view.setBackgroundColor(Color.parseColor("#ffffff"));
                            }
                        });
                        animator.start();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long startCompareTime = System.currentTimeMillis();
                            score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO,
                                    livenessModel.getFeature(), secondFeature, true);
                            endCompareTime = System.currentTimeMillis() - startCompareTime;
                            if (isDevelopment == false) {
                                layoutCompareStatus.setVisibility(View.GONE);
                                livenessTipsFailRl.setVisibility(View.VISIBLE);
                                if (isFace == true) {
                                    livenessTipsFailTv.setText("???????????????????????????");
                                    livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                    livenessTipsPleaseFailTv.setText("????????????????????????");
                                    livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                } else {
                                    rgbLivenessScore = livenessModel.getRgbLivenessScore();
                                    depthLivenessScore = livenessModel.getDepthLivenessScore();
                                    nirLivenessScore = livenessModel.getIrLivenessScore();
                                    if (rgbLivenessScore > rgbLiveScore &&
                                            depthLivenessScore > depthLiveScore &&
                                            nirLivenessScore > nirLiveScore) {
                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                            livenessTipsFailTv.setText("??????????????????");
                                            livenessTipsFailTv.setTextColor(
                                                    Color.parseColor("#FF00BAF2"));
                                            livenessTipsPleaseFailTv.setText("????????????");
                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_success);
                                        } else {
                                            livenessTipsFailTv.setText("?????????????????????");
                                            livenessTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            livenessTipsPleaseFailTv.setText("???????????????????????????");
                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        }
                                    } else {
                                        livenessTipsFailTv.setText("?????????????????????");
                                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                        livenessTipsPleaseFailTv.setText("???????????????????????????");
                                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    // ????????????
    private void checkOpenDebugResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    rgb_test_iv.setVisibility(View.GONE);
                    depth_test_iv.setVisibility(View.GONE);
                    test_nir_iv.setVisibility(View.GONE);
                    rgb_depth_test_view.setImageResource(R.mipmap.ic_image_video);
                    tv_depth_live_time.setText(String.format("??????????????????%s", 0));
                    tv_depth_live_score.setText(String.format("?????????????????????%s ms", 0));
                    tv_feature_time.setText(String.format("?????????????????????%s ms", 0));
                    tv_feature_search_time.setText(String.format("?????????????????????%s ms", 0));
                    tv_all_time.setText(String.format("????????????%s ms", 0));

                } else {
                    BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                    if (image != null) {
                        rgb_depth_test_view.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                        image.destory();
                    }
                    tv_depth_live_time.setText(String.format("??????????????????%s", score));
                    tv_depth_live_score.setText(String.format("?????????????????????%s ms", livenessModel.getDepthtLivenessDuration()));
                    //  ??????????????????
                    if (firstFeature == null || secondFeature == null) {
                        return;
                    }
                    if (rgbLivenessScore < rgbLiveScore || depthLivenessScore < depthLiveScore
                            || nirLivenessScore < nirLiveScore) {
                        tv_feature_time.setText(String.format("?????????????????????%s ms", 0));
                        tv_feature_search_time.setText(String.format("?????????????????????%s ms", 0));
                        long l = livenessModel.getRgbDetectDuration() +
                                livenessModel.getDepthtLivenessDuration() +
                                livenessModel.getIrDetectDuration();
                        tv_all_time.setText(String.format("????????????%s ms", l));
                    } else {
                        tv_feature_time.setText(String.format("?????????????????????%s ms", featureTime));
                        tv_feature_search_time.setText(String.format("?????????????????????%s ms",
                                endCompareTime));
                        long l = livenessModel.getRgbDetectDuration() +
                                livenessModel.getDepthtLivenessDuration() +
                                livenessModel.getIrDetectDuration() +
                                featureTime + endCompareTime;
                        tv_all_time.setText(String.format("????????????%s ms", l));
                    }
                    if (isDevelopment) {
                        livenessTipsFailRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        rgbLivenessScore = livenessModel.getRgbLivenessScore();
                        if (rgbLivenessScore < rgbLiveScore) {
                            rgb_test_iv.setVisibility(View.VISIBLE);
                            rgb_test_iv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            rgb_test_iv.setVisibility(View.VISIBLE);
                            rgb_test_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }

                        depthLivenessScore = livenessModel.getDepthLivenessScore();
                        if (depthLivenessScore < depthLiveScore) {
                            depth_test_iv.setVisibility(View.VISIBLE);
                            depth_test_iv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            depth_test_iv.setVisibility(View.VISIBLE);
                            depth_test_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }

                        nirLivenessScore = livenessModel.getIrLivenessScore();
                        if (nirLivenessScore < nirLiveScore) {
                            test_nir_iv.setVisibility(View.VISIBLE);
                            test_nir_iv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            test_nir_iv.setVisibility(View.VISIBLE);
                            test_nir_iv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }


                        if (rgbLivenessScore > rgbLiveScore && depthLivenessScore > depthLiveScore
                                && nirLivenessScore > nirLiveScore) {
                            if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                textCompareStatus.setText("????????????");
                            } else {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                textCompareStatus.setText("????????????");
                            }
                        } else {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            textCompareStatus.setText("????????????");
                        }
                    }


                }
            }
        });
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * ???????????????
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = draw_detect_face_view.lockCanvas();
                if (canvas == null) {
                    draw_detect_face_view.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    draw_detect_face_view.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    draw_detect_face_view.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        rgbTexture, model.getBdFaceImageInstance());
                if (score < SingleBaseConfig.getBaseConfig().getIdThreshold()
                        || rgbLivenessScore < SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                        || depthLivenessScore < SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                    paint.setColor(Color.parseColor("#FEC133"));
                    paintBg.setColor(Color.parseColor("#FEC133"));
                } else {
                    if (isFace == true) {
                        paint.setColor(Color.parseColor("#FEC133"));
                        paintBg.setColor(Color.parseColor("#FEC133"));
                    } else {
                        paint.setColor(Color.parseColor("#00baf2"));
                        paintBg.setColor(Color.parseColor("#00baf2"));
                    }
                }
                paint.setStyle(Paint.Style.STROKE);
                paintBg.setStyle(Paint.Style.STROKE);
                // ????????????
                paint.setStrokeWidth(8);
                // ?????????????????????????????????
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAlpha(90);
                // ?????????????????????????????????
                paintBg.setAntiAlias(true);
                if (faceInfo.width > faceInfo.height) {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(rgbTexture.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(rgbTexture.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.width() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2, paint);
                    }

                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(rgbTexture.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(rgbTexture.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.height() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2, paint);
                    }
                }
                // ??????canvas
                draw_detect_face_view.unlockCanvasAndPost(canvas);
            }
        });
    }
}
