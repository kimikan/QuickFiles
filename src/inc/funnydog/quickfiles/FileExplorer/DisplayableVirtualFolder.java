package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;

import inc.funnydog.quickfiles.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DisplayableVirtualFolder extends DisplayableFolder {

    public DisplayableVirtualFolder(File file) {
        super(file);
    }
    
    @Override
    public String getName() {
        return "..";
    }

    @Override
    public String getLastModifiedTime() {
        return "";
    }

    @Override
    public Drawable getIconDrawable(Context context) {
        
        return context.getResources().getDrawable(R.drawable.filemgr_up_folder);
    }

}
