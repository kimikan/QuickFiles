package inc.funnydog.quickfiles.FileExplorer;

import java.util.List;
import java.util.Stack;

import inc.funnydog.quickfiles.MyMobileApplication;
import inc.funnydog.quickfiles.Helpers.ICallback;


public class MediaExtractor extends Thread {
    
    private Stack<DisplayableFile> _filesCache 
                = new Stack<DisplayableFile>();
    
    private boolean _isRunning = false;
    
    private ICallback _callback = null;
    
    public void setCallback(ICallback _callback) {
        this._callback = _callback;
    }

    private MediaExtractor() {
        
    }
    
    public static void init() {
        _instance = new MediaExtractor();
    }
    
    private static MediaExtractor _instance = null;
    
    public static MediaExtractor instance() {
        if(_instance == null) {
            init();
        }
        return _instance;
    }
    
    public void run() {
        _isRunning = true;
        while(_isRunning) {
            
            try{
                DisplayableFile file = null;
                synchronized (this) {
                    if(!_filesCache.empty()) {
                        file = _filesCache.pop();
                    }
                }
                
                if(file != null) {
                    file.extractDrawable(MyMobileApplication.getAppContext());
                    android.util.Log.i("xx","xxxxxxx" + file.getName());
                    
                    //if(++count % 2 == 0) 
                    {
                        if(_callback != null) {
                            _callback.onFinished(file);
                        }
                    }
                } else {
                    Thread.sleep(50);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public synchronized void enqueue(List<DisplayableFile> files) {
        _filesCache.clear();
        _filesCache.addAll(files);
    }
    
    public synchronized void enqueue(DisplayableFile file) {
        _filesCache.push(file);
    }
    
    public synchronized void internalPause() {
        _filesCache.clear();
    }
    
    public void stopSelf() {
        _isRunning = false;
    }
    
}
