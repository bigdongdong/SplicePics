package com.cxd;


import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.cxd.chinesechess.databinding.ActivityPreviewBinding;
import com.cxd.splice.SpliceHelper;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

@SuppressLint("CheckResult")
public class PreviewActivity extends AppCompatActivity {
    private ActivityPreviewBinding mBinding;
    private ArrayList<LocalMedia> mResults;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((mBinding = ActivityPreviewBinding.inflate(getLayoutInflater())).getRoot());

        mBinding.ivMode3.setVisibility(View.GONE);

        mResults = getIntent().getParcelableArrayListExtra("list");
        if(mResults == null || mResults.size() < 4){
            mBinding.ivMode3.setVisibility(View.GONE);
        }

        initViews();
        setListeners();
    }

    private void initViews(){}
    private void setListeners(){
        mBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final View.OnClickListener selectListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.ivMode1.setSelected(false);
                mBinding.ivMode2.setSelected(false);
                mBinding.ivMode3.setSelected(false);
                v.setSelected(true);

                if(v == mBinding.ivMode1){
                    onSelect(1);
                }else if(v == mBinding.ivMode2){
                    onSelect(2);
                }else if(v == mBinding.ivMode3){
                    onSelect(3);
                }
            }
        };
        mBinding.ivMode1.setOnClickListener(selectListener);
        mBinding.ivMode2.setOnClickListener(selectListener);
        mBinding.ivMode3.setOnClickListener(selectListener);
        mBinding.ivMode1.callOnClick();

        mBinding.ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
    }

    private void onSelect(int index /*1,2,3*/){
        SpliceHelper.Mode mode;
        switch (index){
            default:
            case 1:
                mode = SpliceHelper.Mode.Vertical;
                break;
            case 2:
                mode = SpliceHelper.Mode.Horizontal;
                break;
            case 3:
                mode = SpliceHelper.Mode.Grid;
                break;
        }

        mBinding.sv.set(mode,mResults);
    }

    private void onSave(){
        Bitmap bitmap = mBinding.sv.get();

        // 创建图片文件
        String imageFileName = "Image_"+ System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SplicePics");

        // 获取ContentResolver
        ContentResolver resolver = PreviewActivity.this.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            // 将Bitmap转换成JPEG格式并写入文件
            OutputStream out = resolver.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // 通知系统相册更新
//            MediaStore.Images.Media.insertImage(resolver, bitmap, imageFileName, null);

            Toast.makeText(this, "保存在："+uri, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}