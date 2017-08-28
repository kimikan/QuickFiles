package inc.funnydog.quickfiles.BookMarks;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;

import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.BookMark;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;

public class BookMarkDisplayableFile extends DisplayableFile {
    BookMark _bookMark;
    public BookMarkDisplayableFile(BookMark bookMark) {
        _bookMark = bookMark;
    }
    
    @Override
    public String getSize() {
        return "";
    }

    @Override
    public Drawable getIconDrawable(Context context) {
        return super.getIconDrawable(context);
    }
    
    @Override
    public int getIconRes() {
        
        File file = getFile();
        if(file != null) {
            if(file.exists() && file.isDirectory()) {
                return R.drawable.filemgr_folder;
            }
        }
        return super.getIconRes();
    }
    
    @Override
    public File getFile() {
        File file = new File(getLastModifiedTime());
        
        return file;
    }
    
    @Override
    public File getParent() {
        return null;
    }
    
    @Override
    public String getName() {
        return _bookMark.BookmarkName;
    }
    
    @Override
    public String getLastModifiedTime() {
        return _bookMark.BookmarkPath;
    }
    
}
