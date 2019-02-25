package com.blogofyb.forum.activities;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesManager {
    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        if (activities.remove(activity) && !activity.isFinishing()) {
            activity.finish();
        }
    }

    public static void finishAllActivities() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static Activity getPrior() {
        return activities.get(activities.size() - 2);
    }
}
