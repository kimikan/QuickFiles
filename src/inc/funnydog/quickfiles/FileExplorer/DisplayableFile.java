package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;

import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.Helpers.FastBitmapDrawable;

public class DisplayableFile {

    public static final int FILE = 0;
    public static final int FOLDER = 1;
    public static final int VIRTUAL_FOLDER = 2;
    
    private File _file;
    
    private boolean isChecked;
    
    protected DisplayableFile() {
    }
    
    public DisplayableFile(File file) {
        _file = file;
    }
     
    public File getFile() {
        return _file;
    }
    
    public String getSize() {
        return FileHelper.readableFileSize(_file.length());
    }
    
    public static DisplayableFile createFile(File file) {
        if(file.isDirectory()) {
            return createFile(file, FOLDER);
        } else {
            return createFile(file, FILE);
        }
    }
    
    public static DisplayableFile createFile (File file, int type) {
        
        if ( type == FILE ) {
            return new DisplayableFile(file);
        } else if ( type == FOLDER ){
            return new DisplayableFolder(file);
        } else {
            return new DisplayableVirtualFolder(file);
        }
    }
    
    public File getParent() {
        return _file.getParentFile();
    }
    
    public String getName() {
        return _file.getName();
    }

    public String getLastModifiedTime() {
        long filetime = _file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss", Locale.getDefault()); 
        String ftime =  formatter.format(new Date(filetime)); 
        return ftime;
    }

    String getFileName() {
        File file = getFile();
        if(file != null) {
            return file.getName();
        }
        return getName();
    }
    
    public void dispose() {
        if(_drawable != null) {
            try {
                FastBitmapDrawable fastDrawable = (FastBitmapDrawable)_drawable;
                fastDrawable.getBitmap().recycle();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private Bitmap getImageThumbnail(File imagePath, int width, int height) {  
        Bitmap bitmap = null;  
        BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        // 获取这个图片的宽和高，注意此处的bitmap为null  
        bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), options);  
        options.inJustDecodeBounds = false; // 设为 false  
        // 计算缩放比  
        int h = options.outHeight;  
        int w = options.outWidth;  
        int beWidth = w / width;  
        int beHeight = h / height;  
        int be = 1;  
        if (beWidth < beHeight) {  
            be = beWidth;  
        } else {  
            be = beHeight;  
        }  
        if (be <= 0) {  
            be = 1;  
        }  
        options.inSampleSize = be;  
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
        bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), options);  
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
        return bitmap;  
    } 
    
    public void extractDrawable(Context context) {
        if(_drawable != null)
            return;
        
        String extension = getExtension(getFileName());
        if(extension == null || extension.length() <= 0)
            return;
        
        if(isPictureFile(extension)) {
            try     
            {
                final int THUMBNAIL_SIZE = 48;
                Bitmap newBitmap = getImageThumbnail(getFile(), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                _drawable = new FastBitmapDrawable(newBitmap);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        } else if(extension.endsWith(".apk")){
            _drawable = getApkIcon(context, getFile().getAbsolutePath());
        }
    }
    
    Drawable _drawable = null;
    public Drawable getIconDrawable(Context context) {
        if(_drawable != null)
            return _drawable;
    
        return context.getResources().getDrawable(getIconRes());
    }
    
    public Drawable getApkIcon(Context context, String path) {   
        try {
            android.content.pm.PackageManager pm = context.getPackageManager();      
            android.content.pm.PackageInfo info = pm.getPackageArchiveInfo(path, android.content.pm.PackageManager.GET_ACTIVITIES);      
            if(info != null){      
                android.content.pm.ApplicationInfo appInfo = info.applicationInfo;
    
                if(Build.VERSION.SDK_INT >= 8){
                    appInfo.sourceDir = path;
                    appInfo.publicSourceDir = path;
                }
    
                return appInfo.loadIcon(pm);
            }    
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private boolean isPictureFile(String extension) {
 
         return ".bmp.jpg.png.gif.tif.".indexOf(extension + ".") >= 0;
    }
    
    public String getExtension(String name) {
        if(name == null)
            return null;
        int index = name.lastIndexOf((int)'.');
        if(index >= 0) {
            name = name.substring(index);
            if(name != null) {
                name = name.toLowerCase(Locale.US);
            }
        }
        return name;
    }
    
    public int getIconRes() {
        String name = getFileName();
        if(name == null)
            return R.drawable.filetype_document;
        int index = name.lastIndexOf((int)'.');
        if(index >= 0)
            name = name.substring(index);
        
        if(name != null) {
            name = name.toLowerCase(Locale.US);

            if(".wmv.avi.mp4.rm.rmvb.mkv.".indexOf(name + ".") >= 0) {
                return R.drawable.filetype_video;
            } else if(".zip.gz.".indexOf(name + ".") >= 0) {
                return R.drawable.filetype_zip;
            } else if(".pdf.".indexOf(name + ".") >= 0) {
                return R.drawable.filetype_pdf;
            } else if(isPictureFile(name + ".")) {
                return R.drawable.filetype_picture;
            } else if(".mp3.midi.aac.au.".indexOf(name + ".") >= 0) {
                return R.drawable.filetype_music;
            }
        }

        return R.drawable.filetype_document;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

}
