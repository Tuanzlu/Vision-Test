package com.baidu.idl.face.main.drivermonitor.activity.drivermonitor;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.drivermonitor.activity.DrivermonitorBaseActivity;
import com.baidu.idl.face.main.drivermonitor.callback.CameraDataCallback;
import com.baidu.idl.face.main.drivermonitor.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.drivermonitor.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.drivermonitor.camera.CameraPreviewManager;
import com.baidu.idl.face.main.drivermonitor.listener.SdkInitListener;
import com.baidu.idl.face.main.drivermonitor.manager.FaceSDKManager;
import com.baidu.idl.face.main.drivermonitor.model.DriverInfo;
import com.baidu.idl.face.main.drivermonitor.model.LivenessModel;
import com.baidu.idl.face.main.drivermonitor.model.SingleBaseConfig;
import com.baidu.idl.face.main.drivermonitor.setting.DriverMonitorSettingActivity;
import com.baidu.idl.face.main.drivermonitor.utils.BitmapUtils;
import com.baidu.idl.face.main.drivermonitor.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.drivermonitor.utils.ToastUtils;
import com.baidu.idl.face.main.drivermonitor.view.PreviewTexture;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.drivermonitor.R;
import com.baidu.idl.main.facesdk.model.BDFaceGazeInfo;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * author : shangrong
 * date : 2020-02-17 10:07
 * description :
 */
public class DriverMonitorActivityDrivermonitor extends DrivermonitorBaseActivity implements View.OnClickListener {

    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private LinearLayout dmPreLiner;
    private LinearLayout dmRelLiner;
    private TextView dmPreTx;
    private RelativeLayout dmRelLinerMsg;
    private TextView dmReleaseMsg;
    private TextView dmDetectCost;
    private TextView dmDetecLivetCost;
    private TextView dmDetecLivetScore;
    private TextView dmGazeDetectCost;
    private TextView dmDriverDetectCost;
    private TextView dmDriverCallScore;
    private TextView dmDriverSmokeScore;
    private TextView dmDriverDrinkScore;
    private TextView dmDriverEatScore;
    private TextView dmDriverNormalScore;
    private ImageView dmRgbIv;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private TextureView irCameraPreviewView;
    private Paint paint;
    private Paint paintBg;
    private RectF rectF;
    private ExecutorService es3 = Executors.newSingleThreadExecutor();
    private Future future3;
    private BDFaceGazeInfo bdFaceGazeInfo;
    private TextureView mDrawDetectFaceView;
    private TextureView irPreviewView;
    // RGB+IR ??????
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;
    private int mCameraNum;
    // ?????????????????????
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    private TextView previewText;
    private TextView developText;
    private ImageView btnSetting;
    private ImageView previewView;
    private ImageView developView;
    private ImageView btn_back;
    private boolean isRelease = false;
    private DriverInfo driverInfo;
    private long driverEndTime;
    private ImageView driverIsRGBLive;
    private LinearLayout driverDebugMsg;
    private TextView homeBaiduTv;
    private Bitmap successBitmap;
    private Bitmap failBitmap;
    private Bitmap normalBitmap;
    private RelativeLayout dmRlNir;
    private int mLiveType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_nir_drivermonitor);
        init();
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
                    ToastUtils.toast(DriverMonitorActivityDrivermonitor.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(DriverMonitorActivityDrivermonitor.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    public void init() {
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();

        dmRlNir = findViewById(R.id.dmRlNir);
        homeBaiduTv = findViewById(R.id.home_baiduTv);
        dmRgbIv = findViewById(R.id.dmRgbIv);

        driverDebugMsg = findViewById(R.id.driverDebugMsg);
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);

        dmDriverCallScore = findViewById(R.id.dmDriverCallScore);
        dmDriverSmokeScore = findViewById(R.id.dmDriverSmokeScore);
        dmDriverDrinkScore = findViewById(R.id.dmDriverDrinkScore);
        dmDriverEatScore = findViewById(R.id.dmDriverEatScore);
        dmDriverNormalScore = findViewById(R.id.dmDriverNormalScore);
        // ???????????????IR ????????????
        irPreviewView = findViewById(R.id.ir_camera_preview_view);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irPreviewView.setRotationY(180);
        }
        // ????????????
        mLiveType = SingleBaseConfig.getBaseConfig().getType();

        dmPreLiner = findViewById(R.id.dmPreLiner);
        dmRelLiner = findViewById(R.id.dmRelLiner);

        dmPreTx = findViewById(R.id.dmPreTx);
        dmRelLinerMsg = findViewById(R.id.dmRelLinerMsg);
        dmDetectCost = findViewById(R.id.dmDetectCost);
        dmDetecLivetCost = findViewById(R.id.dmDetecLivetCost);
        dmDetecLivetScore = findViewById(R.id.dmDetecLivetScore);
        dmGazeDetectCost = findViewById(R.id.dmGazeDetectCost);
        dmDriverDetectCost = findViewById(R.id.dmDriverDetectCost);

        mAutoCameraPreviewView = findViewById(R.id.fa_auto);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);
        irCameraPreviewView = findViewById(R.id.ir_camera_preview_view);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irCameraPreviewView.setRotationY(180);
        }

        dmReleaseMsg = findViewById(R.id.dmReleaseMsg);

        previewText = findViewById(R.id.preview_text);
        developText = findViewById(R.id.develop_text);
        previewView = findViewById(R.id.preview_view);
        developView = findViewById(R.id.develop_view);
        previewText = findViewById(R.id.preview_text);
        developText = findViewById(R.id.develop_text);
        btnSetting = findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
        btn_back = findViewById(R.id.btn_back);

        driverIsRGBLive = findViewById(R.id.driverIsRGBLive);


        previewText.setTextColor(Color.parseColor("#FFFFFF"));

        previewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dmRlNir.setAlpha(0);
                isRelease = false;
                homeBaiduTv.setVisibility(View.VISIBLE);
                dmRelLiner.setVisibility(View.GONE);
                dmPreLiner.setVisibility(View.VISIBLE);
                previewText.setTextColor(Color.parseColor("#FFFFFF"));
                developText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.VISIBLE);
                developView.setVisibility(View.GONE);
                dmRelLinerMsg.setVisibility(View.GONE);
            }
        });

        developText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dmRlNir.setAlpha(1);
                isRelease = true;
                homeBaiduTv.setVisibility(View.GONE);
                dmRelLiner.setVisibility(View.VISIBLE);
                dmPreLiner.setVisibility(View.GONE);
                developText.setTextColor(Color.parseColor("#FFFFFF"));
                previewText.setTextColor(Color.parseColor("#d3d3d3"));
                previewView.setVisibility(View.GONE);
                developView.setVisibility(View.VISIBLE);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(DriverMonitorActivityDrivermonitor.this, "SDK????????????????????????????????????",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }
        });

        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irPreviewView);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreview();
    }


    private void startCameraPreview() {
        if (mCameraNum < 2) {
            Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                // ?????????????????????
                // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
                // ?????????????????????
                // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
                // ??????USB?????????
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() !=  -1){
                    CameraPreviewManager.getInstance().
                            setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
                }else {
                    CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
                }

                CameraPreviewManager.getInstance().startPreview(this, mAutoCameraPreviewView,
                        RGB_WIDTH, RGB_HEIGHT, new CameraDataCallback() {
                            @Override
                            public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                                dealRgb(rgbData);
                            }
                        });
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                }else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParams = irPreviewView.getLayoutParams();
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
                irPreviewView.setLayoutParams(layoutParams);
                mPreview[1].setCamera(mCamera[1],  RGB_WIDTH , RGB_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (Exception e) {

            }
        }
    }

    private void checkData() {
        if (rgbData != null ) {
            FaceSDKManager.getInstance().onAttrDetectCheck(rgbData, null, null, RGB_HEIGHT,
                    RGB_WIDTH, 2, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(final LivenessModel livenessModel) {

                            if (isRelease) {
                                showDetectImage(livenessModel);
                                showFaceMessage(livenessModel);
                            } else {
                                showFaceMessage(livenessModel);
                            }

                            // ??????????????????
                            if (future3 != null && !future3.isDone()) {
                                return;
                            }

                            future3 = es3.submit(new Runnable() {
                                @Override
                                public void run() {
                                    if (livenessModel == null) {
                                        return;
                                    }
                                    long gazeStartTime = System.currentTimeMillis();
                                    bdFaceGazeInfo = FaceSDKManager.getInstance().gazeDetect(livenessModel);
                                    long gazeEndTime = System.currentTimeMillis() - gazeStartTime;

                                    if (irData != null) {
                                        long driverStartTime = System.currentTimeMillis();
                                        driverInfo = FaceSDKManager.getInstance()
                                                .driverMonitorDetect(irData, RGB_WIDTH, RGB_HEIGHT);
                                        driverEndTime = System.currentTimeMillis() - driverStartTime;
                                    } else {
                                        driverEndTime = 0;
                                    }
                                    showDriverMessage(bdFaceGazeInfo, driverInfo, gazeEndTime, driverEndTime);

                                }
                            });
                        }

                        @Override
                        public void onTip(int code, String msg) {

                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            if (isRelease) {
                                showFrame(livenessModel);
                            } else {
                                showFrame(null);
                            }
                        }
                    });
        }
    }

    /**
     * ???????????????
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());

                paint.setColor(Color.parseColor("#00baf2"));
                paintBg.setColor(Color.parseColor("#00baf2"));

                paint.setStyle(Paint.Style.STROKE);
                paintBg.setStyle(Paint.Style.STROKE);
                // ????????????
                paint.setStrokeWidth(8);
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAntiAlias(true);
                paintBg.setAlpha(90);

                if (faceInfo.width > faceInfo.height) {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2, paint);
                    }

                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(mAutoCameraPreviewView.getWidth() - rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2, paint);
                    }
                }

                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }
    private void dealRgb(byte[] data) {
        rgbData = data;
        checkData();
    }

    public void dealIr(final byte[] data) {
        irData = data;
//        checkData();
    }

    @Override
    protected void onPause() {

        CameraPreviewManager.getInstance().stopPreview();

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

        super.onPause();
    }

    public void isShowMessage(final boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    dmRelLinerMsg.setVisibility(View.VISIBLE);
                } else {
                    dmRelLinerMsg.setVisibility(View.GONE);
                }
            }
        });
    }

    public void showFaceMessage(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel != null) {

                    if (mLiveType == 0) {
                        driverIsRGBLive.setVisibility(View.GONE);
                    } else {
                        driverIsRGBLive.setVisibility(View.VISIBLE);
                        if (livenessModel.getRgbLivenessScore() > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                            successBitmap = BitmapFactory.decodeResource(getResources(),
                                    R.mipmap.ic_icon_develop_success);
                            driverIsRGBLive.setImageBitmap(successBitmap);
                        } else {
                            failBitmap = BitmapFactory.decodeResource(getResources(),
                                    R.mipmap.ic_icon_develop_fail);
                            driverIsRGBLive.setImageBitmap(failBitmap);
                        }
                    }

                    dmDetectCost.setText("???????????????" + livenessModel.getRgbDetectDuration() + "ms");
                    dmDetecLivetCost.setText("RGB?????????????????????" + livenessModel.getRgbLivenessDuration() + "ms");
                    dmDetecLivetScore.setText("RGB???????????????" + livenessModel.getRgbLivenessScore());
                } else {
                    normalBitmap = BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_image_video);
                    dmRgbIv.setImageBitmap(normalBitmap);
                    driverIsRGBLive.setVisibility(View.GONE);
                    dmDetectCost.setText("???????????????" + "0ms");
                    dmDetecLivetCost.setText("RGB?????????????????????" + "0ms");
                    dmDetecLivetScore.setText("???????????????" + "0");
                    if (isRelease) {
                        dmRelLinerMsg.setVisibility(View.GONE);
                    } else {
                        driverDebugMsg.setVisibility(View.GONE);
                    }

                    dmReleaseMsg.setText("");
                    dmGazeDetectCost.setText("????????????????????????" + "0ms");
                    dmDriverDetectCost.setText("?????????????????????" + "0ms");
                    dmDriverCallScore.setText("??????????????????" + "0");
                    dmDriverDrinkScore.setText("???????????????" + "0");
                    dmDriverEatScore.setText("??????????????????" + "0");
                    dmDriverSmokeScore.setText("???????????????" + "0");
                    dmDriverNormalScore.setText("???????????????" + "0");
                }
            }
        });
    }

    public void showDriverMessage(final BDFaceGazeInfo bdFaceGazeInfo, final DriverInfo driverInfo
            , final long gazeTime, final long driveTime) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dmGazeDetectCost.setText("????????????????????????" + gazeTime + "ms");
                dmDriverDetectCost.setText("?????????????????????" + driveTime + "ms");
                if (bdFaceGazeInfo == null && driverInfo == null) {
                    if (isRelease) {
                        dmRelLinerMsg.setVisibility(View.GONE);
                    } else {
                        driverDebugMsg.setVisibility(View.GONE);
                    }
                    dmReleaseMsg.setText("");
                } else {
                    StringBuilder drivierMsg = new StringBuilder();
                    if (bdFaceGazeInfo != null) {
                        if (bdFaceGazeInfo.leftEyeGaze !=
                                BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_FRONT ||
                                bdFaceGazeInfo.rightEyeGaze !=
                                        BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_FRONT) {
                            drivierMsg.append("??????????????????,");
                            if (driverInfo == null) {
                                dmReleaseMsg.setText("??????????????????");
                            }
                        }
                    } else {
                        dmReleaseMsg.setText("");
                    }
                    if (driverInfo != null && driverInfo.getBdFaceDriverMonitorInfo() != null) {

                        if (driverInfo.getBdFaceDriverMonitorInfo().calling > 0.5) {
                            drivierMsg.append("?????????,");
                        }
                        if (driverInfo.getBdFaceDriverMonitorInfo().drinking > 0.5) {
                            drivierMsg.append("??????,");
                        }
                        if (driverInfo.getBdFaceDriverMonitorInfo().eating > 0.5) {
                            drivierMsg.append("?????????,");
                        }
                        if (driverInfo.getBdFaceDriverMonitorInfo().smoking > 0.5) {
                            drivierMsg.append("??????,");
                        }

                        String detailsMsg = drivierMsg.toString();
                        if (detailsMsg.length() != 0) {
                            if (detailsMsg.endsWith(",")) {
                                detailsMsg = detailsMsg.substring(0, detailsMsg.length() - 1);
                            }
                            if (isRelease) {
                                dmRelLinerMsg.setVisibility(View.VISIBLE);
                            } else {
                                driverDebugMsg.setVisibility(View.VISIBLE);
                            }
                            dmPreTx.setText(detailsMsg);
                            dmReleaseMsg.setText(detailsMsg);
                        }

                        dmDriverCallScore.setText("??????????????????" + driverInfo.getBdFaceDriverMonitorInfo().calling);
                        dmDriverDrinkScore.setText("???????????????" + driverInfo.getBdFaceDriverMonitorInfo().drinking);
                        dmDriverEatScore.setText("??????????????????" + driverInfo.getBdFaceDriverMonitorInfo().eating);
                        dmDriverSmokeScore.setText("???????????????" + driverInfo.getBdFaceDriverMonitorInfo().smoking);
                        dmDriverNormalScore.setText("???????????????" + driverInfo.getBdFaceDriverMonitorInfo().normal);
                    } else {
                        dmPreTx.setText("");
                        if (bdFaceGazeInfo == null && driverInfo == null) {
                            dmReleaseMsg.setText("");
                        }
                        dmDriverCallScore.setText("??????????????????" + "0");
                        dmDriverDrinkScore.setText("???????????????" + "0");
                        dmDriverEatScore.setText("??????????????????" + "0");
                        dmDriverSmokeScore.setText("???????????????" + "0");
                        dmDriverNormalScore.setText("???????????????" + "0");
                    }
                }
            }
        });
    }

    public void showDetectImage(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel != null) {
                    dmRgbIv.setImageBitmap(BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance()));
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(DriverMonitorActivityDrivermonitor.this, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(DriverMonitorActivityDrivermonitor.this, DriverMonitorSettingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
