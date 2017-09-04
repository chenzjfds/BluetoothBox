package com.actions.bluetoothbox;

import android.graphics.drawable.ColorDrawable;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actions.ibluz.manager.BluzManagerData;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static android.support.test.internal.util.Checks.checkNotNull;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by chenxiangjie on 2016/9/5.
 */

public class CustomMatcher {

    public static Matcher<View> withBgColor(final int color) {
        Checks.checkNotNull(color);
        return new BoundedMatcher<View, ViewGroup>(ViewGroup.class) {
            @Override
            public boolean matchesSafely(ViewGroup row) {
                return color == ((ColorDrawable) row.getBackground()).getColor();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("with bg color: ");
            }
        };
    }

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

}
