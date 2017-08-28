package inc.funnydog.quickfiles.FileExplorer;

import java.util.List;

public class ClipBoard {

    private ClipBoard() {
    }

    private static ClipBoard _instance = new ClipBoard();
    public static ClipBoard instance() {
        return _instance;
    }
    
    public void setFiles(List<DisplayableFile> files,  boolean isCut) {
        _files = files;
        _isCut = isCut;
    }
    
    public List<DisplayableFile> getFiles() {
        return _files;
    }
    
    public boolean isCut() {
        return _isCut;
    }

    private List<DisplayableFile> _files;
    
    private boolean _isCut = false;
}
