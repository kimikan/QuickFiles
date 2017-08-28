package inc.funnydog.quickfiles.Helpers;

import java.io.File;

import android.app.Activity;
import android.os.Environment;

public class Preferences {

    public static File getApksFolder(Activity context) {
        File apkFolder = new File(Environment.getExternalStorageDirectory(), "QuickFiles");
        if(!apkFolder.exists()) {
            if(!apkFolder.mkdirs()) {
                apkFolder = context.getFilesDir();
            }
        }
        
        return apkFolder;
    }
    
    public static File getRootDir() {
        return _root;
    }
    private static File _root;
    private static File[] _files;
    public static void setFtpSharedFiles(File[] files) {
        _files = files;
        if(files.length > 0) {
            _root = _files[0].getParentFile();
        } else {
            _root = Environment.getRootDirectory();
        }
    }
    
    public static File[] getFtpSharedFiles() {
        return _files;
    }
}
