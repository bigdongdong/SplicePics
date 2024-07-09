package com.cxd.splice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpliceView extends View {
    private List<Pair<Bitmap,Rect>> mBitmapPairs = new ArrayList<>();
    private ArrayList<LocalMedia> mResults;
    private SpliceHelper.Mode mMode = SpliceHelper.Mode.Vertical;
    private int mDrewBitmapCount = 0;

    public SpliceView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public SpliceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public SpliceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public SpliceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    @SuppressLint("CheckResult")
    public final void set(SpliceHelper.Mode mode , ArrayList<LocalMedia> results){
        if(getMeasuredWidth() == 0){
            post(new Runnable() {
                @Override
                public void run() {
                    set(mode,results);
                }
            });
            return;
        }

        if(mBitmapPairs != null){
            mBitmapPairs.clear();
        }

        mMode = mode;
        mResults = results;
        mDrewBitmapCount = 0;

        final int width = getResultWidth();
        final int height = getResultHeight();

        //获取每个图片的bitmap
        for (LocalMedia localMedia : mResults){
            String path = localMedia.getPath();
            Glide.with(getContext())
                    .asBitmap()
                    .load(path)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //根据View尺寸，对rect进行调整
                            float hRatio =  getMeasuredWidth() * 1.0f / width ;
                            float vRatio =  getMeasuredHeight() * 1.0f / height ;
                            final float ratio = Math.min(hRatio,vRatio);

                            int x = 0;
                            int y = 0;
                            for (Pair<Bitmap, Rect> pair : mBitmapPairs) {
                                switch (mMode){
                                    case Vertical:
                                        x = 0;
                                        y += pair.second.height();
                                        break;
                                    case Horizontal:
                                        x += pair.second.width();
                                        y = 0;
                                        break;
                                    case Grid:
                                        break;
                                }
                            }

                            Rect rect = new Rect(x,y, (int) (x+resource.getWidth()*ratio), (int) (y+resource.getHeight()*ratio));
                            mBitmapPairs.add(new Pair<>(resource,rect));
                            mDrewBitmapCount++;

                            if(mDrewBitmapCount == mBitmapPairs.size()){
                                invalidate();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    public final Bitmap get(){
        int width = getResultWidth();
        int height = getResultHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        mBitmapPairs.forEach(new Consumer<Pair<Bitmap, Rect>>() {
            int x = 0;
            int y = 0;
            Rect src;
            Rect dest;

            @Override
            public void accept(Pair<Bitmap, Rect> pair) {
                Bitmap bitmap = pair.first;
                src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
                dest = new Rect(x,y,x + bitmap.getWidth(),y+bitmap.getHeight());
                canvas.drawBitmap(bitmap, src, dest,null) ;

                switch (mMode){
                    case Vertical:
                        x = 0;
                        y += bitmap.getHeight();
                         break;
                    case Horizontal:
                        x += bitmap.getWidth();
                        y = 0;
                        break;
                    case Grid:
                        break;
                }
            }
        });
        return resultBitmap;
    }

    private int getResultWidth(){
        int width = 0;
        //计算尺寸
        for (LocalMedia localMedia : mResults){
            switch (mMode){
                case Vertical:
                    width = Math.max(width,localMedia.getWidth());
                    break;
                case Horizontal:
                    width += localMedia.getWidth();
                    break;
                case Grid:
                    break;
            }
        }
        return width;
    }

    private int getResultHeight(){
        int height = 0;
        //计算尺寸
        for (LocalMedia localMedia : mResults){
            switch (mMode){
                case Vertical:
                    height += localMedia.getHeight();
                    break;
                case Horizontal:
                    height = Math.max(height,localMedia.getHeight());
                    break;
                case Grid:
                    break;
            }
        }
        return height;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmapPairs.isEmpty()){
            return;
        }

        mBitmapPairs.forEach(new Consumer<Pair<Bitmap, Rect>>() {
            int x = 0;
            int y = 0;
            Rect src;
            Rect dest;
            @Override
            public void accept(Pair<Bitmap, Rect> pair) {
                Bitmap bitmap = pair.first;
                src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
                dest = pair.second;
                canvas.drawBitmap(bitmap, src, dest,null);

                switch (mMode){
                    case Vertical:
                        x = 0;
                        y += dest.height();
                        break;
                    case Horizontal:
                        x += dest.width();
                        y = 0;
                        break;
                    case Grid:
                        break;
                }
            }
        });
    }
}
