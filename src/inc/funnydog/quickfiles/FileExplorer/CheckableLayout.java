package inc.funnydog.quickfiles.FileExplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Checkable;
import android.widget.FrameLayout;

public class CheckableLayout extends FrameLayout implements Checkable {
    private boolean mChecked;

    public interface OnCheckedListener {
        void onAction();
    }
    
    public CheckableLayout(Context context) {
        super(context);
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    public void setChecked(boolean checked) {
        
        if(mChecked != checked) {
            mChecked = checked;
        }
        
        //this.setBackgroundColor(checked ? color.holo_blue_dark : color.tr);
        
        setBackgroundDrawable(checked ? getResources().getDrawable(
                android.R.color.holo_blue_dark) : null);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

}
