package com.mobileanwendungen.drawingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.mobileanwendungen.drawingapp.controllers.BluetoothController;
import com.mobileanwendungen.drawingapp.utils.UIHelper;
import com.mobileanwendungen.drawingapp.views.BluetoothActivity;
import com.mobileanwendungen.drawingapp.views.MainActivity;
import com.mobileanwendungen.drawingapp.wrapper.BluetoothDevices;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.anything;

public class MainActivityTest {
    private static final String TAG = "cust.MainActivityTest";

    private MainActivity mainActivity;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void prepare() {
        mainActivity = activityRule.getActivity();
    }

    @Test
    public void testConnect() {
        Intents.init();
        // open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.connect)).perform(click());
        intended(hasComponent(BluetoothActivity.class.getName()));
    }

    @Test
    public void testDialog() {
        onView(withId(R.id.lineWidth)).perform(click());
        onView(withText(R.string.set_width))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        /*onView(withClassName(Matchers.equalTo(SeekBar.class.getName())))
                .inRoot(isDialog())
                .perform(setProgress(30));*/
        //View root = mainActivity.findViewById(android.R.id.content).getRootView();
        //ImageView imageView = root.findViewById(R.id.imageViewId);
        //ArrayList<View> list = new ArrayList<>();
        //root.findViewsWithText(list, mainActivity.getString(R.string.set_width), View.FIND_VIEWS_WITH_TEXT);

        //SeekBar seekBar = mainActivity.findViewById(R.id.widthSeekBar);
        //ImageView imageView = seekBar.getRootView().findViewById(R.id.imageViewId);
        //ImageView widthImageView = mainActivity.findViewById(R.id.imageViewId);

        ViewInteraction previous = onView(withId(R.id.imageViewId)).inRoot(isDialog());



        //Bitmap bitmap = ((BitmapDrawable)root.getDrawable()).getBitmap();
        onView(withId(R.id.widthSeekBar))
                .inRoot(isDialog())
                .perform(setProgress(30));

        ViewInteraction current = onView(withId(R.id.imageViewId))
                .inRoot(isDialog());
                /*.check( (view, noViewException) -> {
                    Bitmap newBitmap = ((BitmapDrawable)((ImageView)view).getDrawable()).getBitmap();
                    previous.check( (oldView, oldViewException) -> {
                        Bitmap oldBitmap = ((BitmapDrawable)((ImageView)oldView).getDrawable()).getBitmap();
                        oldBitmap.sameAs(newBitmap);
                    });
                    view.equals(previous);
                });*/

        // doesn't work!

        Assert.assertFalse(current.equals(previous));
        //Bitmap newBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        //Assert.assertFalse(bitmap.sameAs(newBitmap));
    }

    @Test
    public void testDialogDismissed() {
        onView(withId(R.id.lineWidth)).perform(click());
        onView(withId(R.id.widthDialogButton)).perform(click());

        onView(withId(R.id.lineWidth))
                .check(matches(isDisplayed()));
    }

    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }
            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }
}
