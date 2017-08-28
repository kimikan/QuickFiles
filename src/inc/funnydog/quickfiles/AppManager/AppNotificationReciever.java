package inc.funnydog.quickfiles.AppManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import inc.funnydog.quickfiles.Helpers.ICallback;

public class AppNotificationReciever
             extends BroadcastReceiver {   
    public static ICallback callback = null;
    
    @Override   
    public void onReceive(Context context, Intent intent) {   
              
        if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) 
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())
                ){  
            if(callback != null) {
                callback.onFinished(null);
            }
        }
           
    }   
}   
