package com.baidu.idl.main.facesdk.attendancelibrary.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.attendancelibrary.BaseActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.R;
import com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.PreferencesManager;
import com.baidu.idl.main.facesdk.license.BDFaceLicenseAuthInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AttendanceSettingActivity extends BaseActivity implements View.OnClickListener {

    private ImageView gateSetttingBack;
    private LinearLayout gateFaceDetection;
    private LinearLayout gateConfigQualtify;
    private LinearLayout gateHuotiDetection;
    private LinearLayout gateFaceRecognition;
    private LinearLayout gateLensSettings;
    private View gatePictureOptimization;
    private View gateLogSettings;
    private TextView tvSettingQualtify;
    private TextView logSettingQualtify;
    private TextView tvSettingLiviness;
    private LinearLayout configVersionMessage;
    private TextView tvSettingEffectiveDate;
    private FaceAuth faceAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_setting);
        init();
    }

    private void init() {
        faceAuth = new FaceAuth();
        // 返回
        gateSetttingBack = findViewById(R.id.gate_settting_back);
        gateSetttingBack.setOnClickListener(this);
        // 人脸检测
        gateFaceDetection = findViewById(R.id.gate_face_detection);
        gateFaceDetection.setOnClickListener(this);
        // 质量检测
        gateConfigQualtify = findViewById(R.id.gate_config_qualtify);
        gateConfigQualtify.setOnClickListener(this);
        // 活体检测
        gateHuotiDetection = findViewById(R.id.gate_huoti_detection);
        gateHuotiDetection.setOnClickListener(this);
        // 人脸识别
        gateFaceRecognition = findViewById(R.id.gate_face_recognition);
        gateFaceRecognition.setOnClickListener(this);
        // 镜头设置
        gateLensSettings = findViewById(R.id.gate_lens_settings);
        gateLensSettings.setOnClickListener(this);
        // 图像优化
        gatePictureOptimization = findViewById(R.id.gate_picture_optimization);
        gatePictureOptimization.setOnClickListener(this);
        // 日志设置
        gateLogSettings = findViewById(R.id.gate_log_settings);
        gateLogSettings.setOnClickListener(this);
        // 版本信息
        configVersionMessage = findViewById(R.id.configVersionMessage);
        configVersionMessage.setOnClickListener(this);
        tvSettingQualtify = findViewById(R.id.tvSettingQualtify);
        logSettingQualtify = findViewById(R.id.logSettingQualtify);
        tvSettingLiviness = findViewById(R.id.tvSettingLiviness);

        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SingleBaseConfig.getBaseConfig().isLog()) {
            logSettingQualtify.setText("开启");
        } else {
            logSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            tvSettingQualtify.setText("开启");
        } else {
            tvSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isLivingControl()) {
            tvSettingLiviness.setText("开启");
        } else {
            tvSettingLiviness.setText("关闭");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        BDFaceLicenseAuthInfo bdFaceLicenseAuthInfo = faceAuth.getAuthInfo(this);
        Date dateLong = new Date(bdFaceLicenseAuthInfo.expireTime * 1000L);
        String dateTime = simpleDateFormat.format(dateLong);

        tvSettingEffectiveDate.setText("有效期至" + dateTime);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.gate_settting_back) {
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setType(SingleBaseConfig.getBaseConfig().getType());
            finish();
        } else if (id == R.id.gate_face_detection) {
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendanceMinFaceActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_config_qualtify) {
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendanceConfigQualtifyActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_huoti_detection) {
            Intent intent = new Intent(AttendanceSettingActivity.this, FaceLivinessTypeActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_face_recognition) {
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendanceFaceDetectActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_lens_settings) {
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendanceLensSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.configVersionMessage) {
            Intent intent = new Intent(AttendanceSettingActivity.this, VersionMessageActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_picture_optimization){
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendancePictureOptimizationActivity.class);
            startActivity(intent);
        } else if (id == R.id.gate_log_settings) {
            Intent intent = new Intent(AttendanceSettingActivity.this, AttendanceLogSettingActivity.class);
            startActivity(intent);
        }
    }
}