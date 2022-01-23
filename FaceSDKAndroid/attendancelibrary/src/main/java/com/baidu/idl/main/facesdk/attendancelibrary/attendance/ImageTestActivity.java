package com.baidu.idl.main.facesdk.attendancelibrary.attendance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.attendancelibrary.R;
import com.baidu.idl.main.facesdk.attendancelibrary.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.attendancelibrary.setting.AttendanceSettingActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.GsonUtils;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageTestActivity extends Activity implements View.OnClickListener  {

    private static Canvas canvas;
    private String access_token;
    private ImageView testImage;
    private static Paint paint;
    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_image_test);
        initView();


    }

    private void initView() {

        // 返回
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);


        Bundle extraData = getIntent().getExtras();

        String imgStr = extraData.getString("result");
        System.out.println("imgStr in imageTest is: "+ imgStr);

        Bitmap bitmap = base64ToBitmap(imgStr);
        Bitmap icon = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(icon);
        access_token = extraData.getString("access_token");
        paint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        paint.setColor(Color.YELLOW);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        getLandMarks(imgStr, bitmap, icon);
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



    public void getLandMarks(final String imgStr, final Bitmap bitmap, final Bitmap icon) {

        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    // 请求url
                    String url = "https://aip.baidubce.com/rest/2.0/face/v1/landmark";

                    Map<String, Object> map = new HashMap<>();
                    map.put("appid", "Mu8HELp2MU7cnXW4MwHE1Ulq");
                    map.put("image", imgStr);
                    map.put("face_field", "landmark150");
                    map.put("image_type", "BASE64");

                    String param = GsonUtils.toJson(map);
                    System.out.println("param:");
                    System.out.println(param);
                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String result = HttpUtil.post(url, access_token, "application/json", param);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject response = jsonObject.getJSONObject("result");
                    JSONArray face_list = response.getJSONArray("face_list");
                    JSONObject landmarks = face_list.getJSONObject(0).getJSONObject("landmark150");
                    System.out.println("landmarks of the face:" );
                    System.out.println(landmarks);

                    MarkFace(landmarks);

                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          testImage = findViewById(R.id.test_image);
                                          testImage.setVisibility(ImageView.VISIBLE);
                                          testImage.setImageBitmap(compoundBitmap(bitmap, icon));
                                      }
                                  });

                    System.out.println("post result:");
                    System.out.println(result);
                } catch (Exception e) {
                    System.err.printf("获取token失败！");
                    e.printStackTrace(System.err);
                }

            }
        }).start();
    }


    public static Bitmap compoundBitmap(Bitmap downBitmap,Bitmap upBitmap)
    {
        Bitmap mBitmap = downBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //如果遇到黑色，则显示downBitmap里面的颜色值，如果不是则显示upBitmap里面的颜色值
        //循环获得bitmap所有像素点
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();
        //首先保证downBitmap和 upBitmap是一致的高宽大小
        if(mBitmapWidth==upBitmap.getWidth() && mBitmapHeight==upBitmap.getHeight())
        {
            for (int i = 0; i < mBitmapHeight; i++) {
                for (int j = 0; j < mBitmapWidth; j++) {
                    //获得Bitmap 图片中每一个点的color颜色值
                    //将需要填充的颜色值如果不是
                    //在这说明一下 如果color 是全透明 或者全黑 返回值为 0
                    //getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000
                    //而不透明黑色是0xFF000000 如果不计算透明部分就都是0了
                    int color = upBitmap.getPixel(j, i);
                    //将颜色值存在一个数组中 方便后面修改
                    if (color == Color.YELLOW) {
                        mBitmap.setPixel(j, i, Color.YELLOW);  //将白色替换成透明色
                    }
                }
            }
        }
//        downBitmap.recycle();
//        upBitmap.recycle();
        return mBitmap;
    }



    public static String MarkFace(JSONObject landmark150) throws JSONException {

        Map<String, Integer> groupMap = group(landmark150);
        Set<String> groupSetArray = groupMap.keySet();
        for (String key : groupSetArray) {
            int getCount = groupMap.get(key);
            String nowKey = key;
            if (getCount > 1) {
                nowKey = key + "_" + 1;
            }
            Location faceLocation = GsonUtils.fromJson(landmark150.getString(nowKey),Location.class);
            int x_1 =  (int) faceLocation.getX();
            int y_1 = (int) faceLocation.getY();
            canvas.drawLine(x_1, y_1, x_1, y_1, paint);
            System.err.println(x_1+" "+y_1+" "+x_1+" "+y_1+"  key1 = "+key);
            int count = groupMap.get(key);
            if (count > 1) {

                for (int i = 2; i <= count; i++) {
                    String next_key = key + "_" + i;
                    Location nowFaceLocation= GsonUtils.fromJson(landmark150.getString(next_key),Location.class);

                    int x_2 =  (int) nowFaceLocation.getX();
                    int y_2 = (int) nowFaceLocation.getY();
                    //标记关键点
                    canvas.drawLine(x_2,y_2,x_2,y_2,paint);
                    //人脸关键点连接
                    canvas.drawLine(x_1,y_1,x_2,y_2,paint);
                    System.err.println(x_1+" "+y_1+" "+x_2+" "+y_2+"  key1 = "+key+"   == 	key2:" +next_key);
                    //重置连接点到当前连接点
                    x_1 =  (int) nowFaceLocation.getX();
                    y_1 = (int) nowFaceLocation.getY();
                }
            }
        }

        return "Finished MarkFace";
    }



    /**
     * @func 人脸器官分组
     * @author lqy
     * @param landmark150
     * @return Map<String,Integer> : key = 位置名，value = 连接个数
     */
    public static Map<String,Integer> group(JSONObject landmark150) {
        Map<String,Integer> groupMap = new HashMap<>();

        Iterator iterator = landmark150.keys();
        while(iterator.hasNext()){
//            System.out.println("key ---  " + iterator.next().toString());
            String key = iterator.next().toString();
            //以下划线分组
            String[] arr = key.split("_");
            //查询该字段中是否存在数字（是否存在多个序列字段）
            Pattern pat = Pattern.compile("\\d");
            String strrr = arr[arr.length - 1].substring(0,1);
            Matcher mat = pat.matcher(strrr);
            Integer count = 1;
            if (mat.matches()) {
                key = key.replace("_"+arr[arr.length - 1], "");
            }
            //记录该器官组名的连接总数到分组Map
            count = groupMap.get(key);
            count = count == null ? 1 : count + 1;
            groupMap.put(key, count);
        }
        return groupMap;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 返回
        if (id == R.id.btn_back) {
            finish();
            // 设置
        }
    }
}