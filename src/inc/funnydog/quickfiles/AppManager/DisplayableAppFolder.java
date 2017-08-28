package inc.funnydog.quickfiles.AppManager;

import java.io.File;

import inc.funnydog.quickfiles.DB.ApkGroup;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFolder;

public class DisplayableAppFolder extends DisplayableFolder {
    
    public ApkGroup getApkGroup() {
        return _apkGroup;
    }

    private ApkGroup _apkGroup;
    
    public DisplayableAppFolder(ApkGroup apkGroup) {
        _apkGroup = apkGroup;
    }
    
    @Override
    public String getSize() {
        return "";
    }

    @Override
    public File getParent() {
        return null;
    }

    @Override
    public String getName() {
        return _apkGroup.Name;
    }

    public String getDescription() {
        return _apkGroup.Description;
    }
    @Override
    public String getLastModifiedTime() {
        return getDescription();
    }

}
