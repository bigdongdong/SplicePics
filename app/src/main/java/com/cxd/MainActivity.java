package com.cxd;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cxd.chinesechess.databinding.ActivityMainBinding;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((mBinding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());

    }

    @Override
    protected void onResume() {
        super.onResume();

        PictureSelector.create(MainActivity.this)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .setMinSelectNum(2)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        Intent intent = new Intent(MainActivity.this,PreviewActivity.class);
                        intent.putParcelableArrayListExtra("list",result);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

    }
}
