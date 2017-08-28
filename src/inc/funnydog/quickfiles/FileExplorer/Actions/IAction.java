package inc.funnydog.quickfiles.FileExplorer.Actions;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;

public interface IAction {
    
    void onAction(List<DisplayableFile> files, Object...objects);
    
    String getDescription();
    
    Drawable getDrawableIcon(Context context);
}
