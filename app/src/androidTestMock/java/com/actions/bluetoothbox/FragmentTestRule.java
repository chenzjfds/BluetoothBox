package com.actions.bluetoothbox;

import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.junit.Assert;

public class FragmentTestRule<F extends Fragment, A extends FragmentActivity> extends ActivityTestRule<A> {

    private final Class<F> mFragmentClass;
    private F mFragment;

    public FragmentTestRule(final Class<F> fragmentClass,
                             final Class<A> activityClass) {
        super(activityClass, true, false);
        mFragmentClass = fragmentClass;
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();

        getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Instantiate and insert the fragment into the container layout
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            mFragment = mFragmentClass.newInstance();
                            System.out.println(mFragmentClass.getSimpleName());
                            transaction.replace(R.id.container, mFragment);
                            transaction.commit();
                        } catch (InstantiationException e) {
                            Assert.fail(String.format("%s: Could not insert %s into TestActivity: %s",
                                    getClass().getSimpleName(),
                                    mFragmentClass.getSimpleName(),
                                    e.getMessage()));
                        } catch (IllegalAccessException e) {
                            Assert.fail(String.format("%s: Could not insert %s into TestActivity: %s",
                                    getClass().getSimpleName(),
                                    mFragmentClass.getSimpleName(),
                                    e.getMessage()));
                        }
                    }
                }
        );
    }

    public F getFragment() {
        return mFragment;
    }
} 