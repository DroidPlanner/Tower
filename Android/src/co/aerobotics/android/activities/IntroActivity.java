package co.aerobotics.android.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import co.aerobotics.android.R;
import co.aerobotics.android.fragments.intro.SampleSlide;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;


public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SampleSlide.newInstance(R.layout.slide1));
        addSlide(SampleSlide.newInstance(R.layout.slide_preflight));
        addSlide(SampleSlide.newInstance(R.layout.slide_inflight));
        addSlide(SampleSlide.newInstance(R.layout.slide_afterflight));
        setIndicatorColor(getResources().getColor(R.color.primary_dark_blue), getResources().getColor(R.color.background_grey));
        setColorDoneText(getResources().getColor(R.color.primary_dark_blue));
        setNextArrowColor(getResources().getColor(R.color.primary_dark_blue));
        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        IntroActivity.this.startActivity(intent);
        finish();
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
