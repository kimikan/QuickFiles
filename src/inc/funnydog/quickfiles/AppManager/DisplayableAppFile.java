package inc.funnydog.quickfiles.AppManager;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;

import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;
import inc.funnydog.quickfiles.FileExplorer.FileHelper;

public class DisplayableAppFile extends DisplayableFile {

    private Drawable _drawable;
    private String _name;
    private long _size;
    private File _apkFile;
    private String _packageName;
    
    public DisplayableAppFile(Drawable drawable, long size, String name, 
            String packageName, File apkFile) {
        _drawable = drawable;
        _name = name;
        _size = size;
        _apkFile = apkFile;
        _packageName = packageName;
    }
    
    @Override
    public String getSize() {
        return FileHelper.readableFileSize(_size);
    }

    @Override
    public Drawable getIconDrawable(Context context) {
        return _drawable;
    }
    
    @Override
    public int getIconRes() {
        return super.getIconRes();
    }
    
    @Override
    public File getFile() {
        return _apkFile;
    }
    
    @Override
    public File getParent() {
        return null;
    }
    
    @Override
    public String getName() {
        return _name;
    }
    
    public String getPackageName() {
        return _packageName;
    }
    
    @Override
    public String getLastModifiedTime() {
        return getPackageName();
    }
    /*
    public ApkItem toApkItem() {
        ApkItem item = new ApkItem();
        item.
    }*/
}
