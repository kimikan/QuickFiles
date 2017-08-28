package inc.funnydog.quickfiles.Helpers;

import java.io.File;
import java.util.Stack;


public class NavigationHistory {

    private Stack<File> _history = new Stack<File>();
    private NavigationHistory() {
    }
    
    private static NavigationHistory _instance = 
            new NavigationHistory();
    public static NavigationHistory instance() {
        return _instance;
    }
    
    public synchronized File top() {
        if(_history.empty())
            return null;
        return _history.peek();
    }
    
    public synchronized void push(File file) {
        _history.push(file);
    }
    
    public synchronized File pop() {
        if(_history.empty()) 
            return null;
        return _history.pop();
    }
    
    public synchronized void clear() {
        _history.clear();
    }
}
