package com.baidu.idl.main.facesdk.paymentlibrary.activity.payment;


import android.content.Context;
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
import android.os.Handler;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.paymentlibrary.R;
import com.baidu.idl.main.facesdk.paymentlibrary.activity.BaseActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.api.FaceApi;
import com.baidu.idl.main.facesdk.paymentlibrary.callback.CameraDataCallback;
import com.baidu.idl.main.facesdk.paymentlibrary.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.paymentlibrary.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.paymentlibrary.paymentcamera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.paymentlibrary.paymentcamera.CameraPreviewManager;
import com.baidu.idl.main.facesdk.paymentlibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.paymentlibrary.model.LivenessModel;
import com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.paymentlibrary.model.User;
import com.baidu.idl.main.facesdk.paymentlibrary.setting.PaymentSettingActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.DensityUtils;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.FileUtils;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.ToastUtils;


public class FaceRGBPaymentActivity extends BaseActivity implements View.OnClickListener {

    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private Context mContext;
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private boolean isCheck = false;
    private boolean isTime = true;
    private long searshTime;
    private boolean isCompareCheck = false;
    private boolean isNeedCamera = true;
    private TextureView mDrawDetectFaceView;
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    // ????????????????????????????????????x?????????y????????????width
    private float[] pointXY = new float[4];

    private TextView preText;
    private ImageView previewView;
    private RelativeLayout preViewRelativeLayout;

    private TextView deveLop;
    private RelativeLayout deveLopRelativeLayout;
    private ImageView developView;
    private TextView preToastText;
    private TextView detectSurfaceText;
    private ImageView isCheckImage;
    private float mRgbLiveScore;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mNum;
    private TextView mTvLive;
    private TextView mTvLiveScore;
    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;
    private RelativeLayout progressLayout;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView progressBarView;
    private RelativeLayout payHintRl;
    private boolean payHint = false;
    private boolean mIsOnClick = false;
    private ImageView isMaskImage;
    private RelativeLayout detectRegLayout;
    private ImageView detectRegImageItem;
    private ImageView isCheckImageView;
    private TextView detectRegTxt;
    private boolean mIsPayHint = true;
    private User mUser;
    private boolean count = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_paymentlibrary);
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
            relativeLayout.setLayoutParams(params);
        }

    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
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
                    ToastUtils.toast(mContext, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(mContext, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    /**
     * View
     */
    private void initView() {
        // ??????????????????
        relativeLayout = findViewById(R.id.all_relative);
        // ???????????????RGB ????????????
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.isDraw = true;
        // ????????????
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);

        // ??????
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // ??????
        ImageView mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);

        // ***************????????????*************
        // ?????????
        preText = findViewById(R.id.preview_text);
        preText.setOnClickListener(this);
        preText.setTextColor(Color.parseColor("#ffffff"));
        previewView = findViewById(R.id.preview_view);
        // ????????????
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        preToastText = findViewById(R.id.pre_toast_text);
        progressLayout = findViewById(R.id.progress_layout);
        progressBarView = findViewById(R.id.progress_bar_view);
        // ?????????????????????
        payHintRl = findViewById(R.id.pay_hintRl);
        detectRegLayout = findViewById(R.id.detect_reg_layout);
        detectRegImageItem = findViewById(R.id.detect_reg_image_item);
        isMaskImage = findViewById(R.id.is_mask_image);
        isCheckImageView = findViewById(R.id.is_check_image_view);
        detectRegTxt = findViewById(R.id.detect_reg_txt);

        // ***************????????????*************
        // ?????????
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLop.setTextColor(Color.parseColor("#a9a9a9"));
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        // ????????????
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        isCheckImage = findViewById(R.id.is_check_image);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        detectSurfaceText.setVisibility(View.GONE);
        // ????????????
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // ????????????
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // ??????RGB ????????????
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.GONE);
        // ?????????????????????
        mNum = findViewById(R.id.tv_num);
        mNum.setText(String.format("?????? ??? %s ?????????", FaceApi.getInstance().getmUserNum()));
        // ????????????
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB??????
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
        // ????????????
        mTvFeature = findViewById(R.id.tv_feature_time);
        // ??????
        mTvAll = findViewById(R.id.tv_feature_search_time);
        // ?????????
        mTvAllTime = findViewById(R.id.tv_all_time);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);


    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
        // TODO ??? ????????????
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        }
        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // ???????????????????????????????????????
                        if (isNeedCamera) {
                            FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                    height, width, mLiveType, new FaceDetectCallBack() {
                                        @Override
                                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                                            if (mAutoCameraPreviewView.isDraw) {
                                                // ????????????
                                                checkCloseDebugResult(livenessModel);
                                            } else {
                                                // ????????????
                                                checkOpenDebugResult(livenessModel);
                                            }
                                        }

                                        @Override
                                        public void onTip(int code, String msg) {
                                        }

                                        @Override
                                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                            // ???????????????
                                            if (!mAutoCameraPreviewView.isDraw) {
                                                showFrame(livenessModel);
                                            }


                                        }
                                    });
                        }
                    }
                });
    }

    // ***************????????????????????????*************
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    if (isTime) {
                        isTime = false;
                        searshTime = System.currentTimeMillis();
                    }
                    long endSearchTime = System.currentTimeMillis() - searshTime;
                    if (endSearchTime < 5000) {
                        preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                        preToastText.setText("???????????????????????????");
                        progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    } else {
                        payHint(null);
                    }
                    return;
                }

                isTime = true;
                pointXY[0] = livenessModel.getFaceInfo().centerX;
                pointXY[1] = livenessModel.getFaceInfo().centerY;
                pointXY[2] = livenessModel.getFaceInfo().width;
                pointXY[3] = livenessModel.getFaceInfo().width;
                FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mAutoCameraPreviewView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
                float leftLimitX = AutoTexturePreviewView.circleX - AutoTexturePreviewView.circleRadius;
                float rightLimitX = AutoTexturePreviewView.circleX + AutoTexturePreviewView.circleRadius;
                float topLimitY = AutoTexturePreviewView.circleY - AutoTexturePreviewView.circleRadius;
                float bottomLimitY = AutoTexturePreviewView.circleY + AutoTexturePreviewView.circleRadius;
                if (pointXY[0] - pointXY[2] / 2 < leftLimitX
                        || pointXY[0] + pointXY[2] / 2 > rightLimitX
                        || pointXY[1] - pointXY[3] / 2 < topLimitY
                        || pointXY[1] + pointXY[3] / 2 > bottomLimitY) {
                    preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                    preToastText.setText("???????????????????????????");
                    progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    return;
                }
                preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                preToastText.setText("???????????????...");
                progressBarView.setImageResource(R.mipmap.ic_loading_blue);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (count) {
                            count = false;
                            payHint(livenessModel);
                        }
                    }
                }, 2 * 500);  // ??????1?????????
            }
        });
    }

    //  ***************????????????????????????*************
    private void checkOpenDebugResult(final LivenessModel livenessModel) {

        // ?????????????????????UI??????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    isCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("???????????? ???%s ms", 0));
                    mTvLive.setText(String.format("RGB?????????????????? ???%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB???????????? ???%s", 0));
                    mTvFeature.setText(String.format("?????????????????? ???%s ms", 0));
                    mTvAll.setText(String.format("?????????????????? ???%s ms", 0));
                    mTvAllTime.setText(String.format("????????? ???%s ms", 0));
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    image.destory();
                }
                if (mLiveType == 0) {
                    User user = livenessModel.getUser();
                    if (user == null) {
                        mUser = null;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#fec133"));
                            textCompareStatus.setText("???????????????");
                        }
                    } else {
                        mUser = user;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                            textCompareStatus.setText("????????????");
                        }
                    }

                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        }

                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#fec133"));
                            textCompareStatus.setText("???????????????");
                        }

                    } else {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                        User user = livenessModel.getUser();
                        if (user == null) {
                            mUser = null;
                            if (isCompareCheck) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#fec133"));
                                textCompareStatus.setText("???????????????");
                            }
                        } else {
                            mUser = user;
                            if (isCompareCheck) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                textCompareStatus.setText("????????????");
                            }
                        }
                    }
                }
                mTvDetect.setText(String.format("???????????? ???%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB?????????????????? ???%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB???????????? ???%s", livenessModel.getRgbLivenessScore()));
                mTvFeature.setText(String.format("?????????????????? ???%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("?????????????????? ???%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("????????? ???%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        // ??????
        int id = v.getId();
        if (id == R.id.btn_back) {
            if (mIsOnClick) {
                progressLayout.setVisibility(View.VISIBLE);
                payHintRl.setVisibility(View.GONE);
                preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                preToastText.setText("???????????????????????????");
                progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                isNeedCamera = true;
                count = true;
                mIsOnClick = false;
            } else {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(mContext, "SDK????????????????????????????????????",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }

            // ??????
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK????????????????????????????????????",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, PaymentSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            if (payHintRl.getVisibility() == View.VISIBLE) {
                return;
            }
            mAutoCameraPreviewView.isDraw = true;
            previewView.setVisibility(View.VISIBLE);
            preText.setTextColor(Color.parseColor("#ffffff"));
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            preToastText.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.VISIBLE);
            progressBarView.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);

            layoutCompareStatus.setVisibility(View.GONE);
            developView.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            deveLopRelativeLayout.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            isCheckImage.setVisibility(View.GONE);
            mDrawDetectFaceView.setVisibility(View.GONE);
            isCompareCheck = false;
            isCheck = false;
            mIsPayHint = true;
            count = true;
        } else if (id == R.id.develop_text) {
            isNeedCamera = true;
            mIsOnClick = false;
            mIsPayHint = false;
            mAutoCameraPreviewView.isDraw = false;
            previewView.setVisibility(View.GONE);
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            preViewRelativeLayout.setVisibility(View.GONE);
            preToastText.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);
            payHintRl.setVisibility(View.GONE);

            developView.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            isCheckImage.setVisibility(View.VISIBLE);
            mDrawDetectFaceView.setVisibility(View.VISIBLE);
            isCompareCheck = true;
            isCheck = true;
            count = false;
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
                // ???????????????
                FaceOnDrawTexturViewUtil.drawFaceColor(mUser, paint, paintBg, model);
                // ???????????????
                FaceOnDrawTexturViewUtil.drawCircle(canvas, mAutoCameraPreviewView,
                        rectF, paint, paintBg, faceInfo);
                // ??????canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    private void payHint(final LivenessModel livenessModel) {
        if (livenessModel == null && mIsPayHint) {
            isMaskImage.setImageResource(R.mipmap.ic_mask_fail);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_fail_sweat);
            detectRegTxt.setTextColor(Color.parseColor("#FECD33"));
            detectRegTxt.setText("????????????");
            progressLayout.setVisibility(View.GONE);
            payHintRl.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //   todo somthing here
                    progressLayout.setVisibility(View.VISIBLE);
                    payHintRl.setVisibility(View.GONE);
                    isTime = true;
                }
            }, 3 * 1000);  // ??????3?????????
            return;
        }
        if (mIsPayHint && livenessModel.getUser() == null) {
            // todo: ????????????
            BDFaceImageInstance bdFaceImageInstance = livenessModel.getBdFaceImageInstance();
            Bitmap instaceBmp = BitmapUtils.getInstaceBmp(bdFaceImageInstance);
            isMaskImage.setImageResource(R.mipmap.ic_mask_fail);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_fail_sweat);
            detectRegImageItem.setImageBitmap(instaceBmp);
            detectRegTxt.setTextColor(Color.parseColor("#FECD33"));
            detectRegTxt.setText("?????????????????????");
            progressLayout.setVisibility(View.GONE);
            payHintRl.setVisibility(View.VISIBLE);
            isNeedCamera = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //   todo somthing here
                    progressLayout.setVisibility(View.VISIBLE);
                    payHintRl.setVisibility(View.GONE);
                    payHint = false;
                    isNeedCamera = true;
                    count = true;
                }
            }, 3 * 1000);  // ??????3?????????

        }
        if (mIsPayHint && livenessModel.getUser() != null) {
            // todo: ????????????kk
            payHintRl.setVisibility(View.VISIBLE);
            String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                    + "/" + livenessModel.getUser().getImageName();
            Bitmap userBitmap = BitmapFactory.decodeFile(absolutePath);
            detectRegImageItem.setImageBitmap(userBitmap);
            isMaskImage.setImageResource(R.mipmap.ic_mask_success);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_success_star);
            detectRegTxt.setTextColor(Color.parseColor("#00BAF2"));
            detectRegTxt.setText(livenessModel.getUser().getUserName() + " ????????????");
            isNeedCamera = false;
            mIsOnClick = true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
