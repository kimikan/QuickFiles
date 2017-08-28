package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;

import inc.funnydog.quickfiles.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DisplayableFolder extends DisplayableFile {

    protected DisplayableFolder() {}
    
    public DisplayableFolder(File file) {
        super(file);
    }

    @Override
    public String getSize() {
        return "";
    }

    @Override
    public Drawable getIconDrawable(Context context) {
        
        return context.getResources().getDrawable(R.drawable.filemgr_folder);
    }

    @Override
    public int getIconRes() {
        return R.drawable.filemgr_folder;
    }

    
}
