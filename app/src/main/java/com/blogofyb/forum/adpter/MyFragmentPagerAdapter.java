package com.blogofyb.forum.adpter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.blogofyb.forum.fragments.ForumFragment;
import com.blogofyb.forum.fragments.MessageFragment;
import com.blogofyb.forum.fragments.SubscribeFragment;
import com.blogofyb.forum.fragments.ZoneFragment;

import java.util.ArrayList;
import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        fragments.add(new ForumFragment());
        fragments.add(new SubscribeFragment());
        fragments.add(new MessageFragment());
        fragments.add(new ZoneFragment());
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }



    @Override
    public int getCount() {
        return fragments.size();
    }
}
