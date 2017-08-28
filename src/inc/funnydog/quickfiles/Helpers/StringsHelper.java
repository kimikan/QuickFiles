package inc.funnydog.quickfiles.Helpers;

import android.content.Context;

import inc.funnydog.quickfiles.MyMobileApplication;

public class StringsHelper {

    public static String getString (int resId) {
        Context context = MyMobileApplication.getAppContext();
        return context.getString(resId);
    }
}
