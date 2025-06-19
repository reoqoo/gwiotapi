package com.gw.reoqoo;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;

import org.hamcrest.Matcher;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class ClickChildAction {

    public static ViewAction clickChildWithId(@IdRes final int childId) {

        final Matcher<View> childMatcher = withId(childId);

        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), hasDescendant(childMatcher));
            }

            @Override
            public String getDescription() {
                return "Click on a child view " + childMatcher;
            }

            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(childId);
                if (child != null) {
                    child.performClick();
                } else {
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new IllegalArgumentException("Didn't find any view " + childMatcher))
                            .build();
                }
            }
        };
    }
}