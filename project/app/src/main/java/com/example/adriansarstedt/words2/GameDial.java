package com.example.adriansarstedt.words2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameDial extends LinearLayout {

    TextView tv;
    ArcView av;
    ImageView iv;
    View bg, fc, md;
    Context ctx;

    int shrinkDuration = 10000, growDuration = 1000;

    ArcShrinkAnimation asa, ada;
    ArcGrowAnimation aga;
    ValueAnimator SaturationAnimator;
    Animation flipStart, flipEnd, imageFlipStart, imageFlipEnd, focus, shake;

    Bitmap dr, drOriginal;

    Handler messageHandler;


    public GameDial(Context context) {
        super(context);
        initializeViews(context);
        ctx = context;
    }

    public GameDial(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
        ctx = context;
    }

    public GameDial(Context context,
                           AttributeSet attrs,
                           int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
        ctx = context;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.game_dial, this);
    }


    @Override
    protected void onFinishInflate() {

        tv = (TextView) this.findViewById(R.id.tv);
        av = (ArcView) this.findViewById(R.id.av);
        iv = (ImageView) this.findViewById(R.id.iv);
        bg = this.findViewById(R.id.bg);
        fc = this.findViewById(R.id.fc);
        md = this.findViewById(R.id.gd);

        Typeface custom_font_hairline = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Thin.ttf");

        tv.setTypeface(custom_font_hairline);

        setupAnimations();

        super.onFinishInflate();
    }

    public void startTimer(final Runnable endAction) {
        asa.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation){
                endAction.run();
            }
        });

        av.startAnimation(asa);
    }

    public void regenerate(int newScore, String newAnimal) {
        drOriginal = BitmapFactory.decodeResource(ctx.getResources(),
                ctx.getResources().getIdentifier(newAnimal.toLowerCase() + "imagesmall", "drawable",
                        getContext().getPackageName()));

        aga.updateAngle();
        av.startAnimation(aga);
        updateDisplay(String.valueOf(newScore), drOriginal, 80);

    }

    public void discovery(int newScore, String newAnimal) {
    }

    public void displayMessage(final String message, EditText highlightText, Button helpButton, final Bitmap tmpDr) {

        final String oldText = tv.getText().toString();
        final Bitmap oldBitmap = ((BitmapDrawable)iv.getBackground()).getBitmap();

        if (highlightText != null) {
            highlightText.selectAll();
            highlightText.startAnimation(shake);
        }

        if (helpButton != null) {
            helpButton.startAnimation(shake);
        }

        updateDisplay(message, tmpDr, 40);

        messageHandler.removeCallbacksAndMessages(null);
        messageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (tmpDr != null) {
                    updateDisplay(oldText, oldBitmap, 80);
                } else {
                    updateDisplay(oldText, null, 80);
                }


                messageHandler.removeCallbacks(this);
            }
        }, 2000);

        md.startAnimation(focus);
    }

    public void updateDisplay(final String newText, final Bitmap newImage, final int newTextSize) {
        flipStart.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (newImage != null) {
                    iv.startAnimation(imageFlipStart);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tv.setTextSize(newTextSize);
                tv.setText(newText);
                tv.startAnimation(flipEnd);

                if (newImage != null) {
                    iv.setImageBitmap(newImage);
                    iv.startAnimation(imageFlipEnd);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        tv.startAnimation(flipStart);
    }

    public void setShrinkTime(int t) {
        shrinkDuration = t;
    }

    public void setGrowTime(int t) {
        growDuration = t;
    }

    public void setupAnimations() {
        asa = new ArcShrinkAnimation(av);
        asa.setDuration(shrinkDuration);

        SaturationAnimator = ValueAnimator.ofFloat(0f, 1f);
        SaturationAnimator.setDuration(2000);
        SaturationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dr = Globals.toGrayscale(drOriginal, SaturationAnimator.getAnimatedFraction());
                iv.setImageBitmap(dr);
            }
        });

        aga = new ArcGrowAnimation(av);
        aga.setDuration(growDuration);

        aga.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) { av.startAnimation(asa); }
        });

        ada = new ArcShrinkAnimation(av);
        ada.setDuration(1000);

        ada.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){
                Animation Focus = AnimationUtils.loadAnimation(getContext(),
                        R.anim.grow_shrink);
                md.startAnimation(Focus);
            }

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                SaturationAnimator.start();
                aga.updateAngle();
                av.startAnimation(aga);
            }
        });

        messageHandler = new Handler();
        imageFlipStart = AnimationUtils.loadAnimation(getContext(),
                R.anim.flip_start);
        imageFlipEnd = AnimationUtils.loadAnimation(getContext(),
                R.anim.flip_end);
        flipStart = AnimationUtils.loadAnimation(getContext(),
                R.anim.flip_start);
        flipEnd = AnimationUtils.loadAnimation(getContext(),
                R.anim.flip_end);
        focus = AnimationUtils.loadAnimation(getContext(),
                R.anim.grow_shrink);
        shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
    }
}