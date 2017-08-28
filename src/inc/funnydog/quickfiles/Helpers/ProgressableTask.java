package inc.funnydog.quickfiles.Helpers;


import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.WindowManager;

import inc.funnydog.quickfiles.R;


public abstract class ProgressableTask<T,T1,T2> extends AsyncTask<T,T1,T2> {
    
    private TransparentProgressDialog _dialog;
    
    public ProgressableTask(Context context) {
        _dialog = new TransparentProgressDialog(context);
        //_dialog.setContentView(R.layout.dialog_progress);
        
    }
    
    @Override
    protected abstract T2 doInBackground(T... params);
    
    @Override
    protected void onCancelled() {
        super.onCancelled();
        _dialog.dismiss();
    }

    @Override
    protected void onCancelled(T2 result) {
        super.onCancelled(result);
        _dialog.dismiss();
    }

    @Override
    protected void onPostExecute(T2 result) {
        super.onPostExecute(result);
        _dialog.dismiss();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        _dialog.show();
    }

    @Override
    protected void onProgressUpdate(T1... values) {
        super.onProgressUpdate(values);
    }
}
/*
class TransparentProgressDialog extends ProgressDialog {
    public TransparentProgressDialog(Context context) {
        super(context, R.style.TransparentProgressDialog);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
}*/


class TransparentProgressDialog extends Dialog {
        
    public TransparentProgressDialog(Context context) {
        super(context, R.style.TransparentProgressDialog);
        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        /*LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = 
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ProgressBar bar = new ProgressBar(context
                , null, android.R.attr.progressBarStyleSmallInverse);
        
        layout.addView(bar, params);
        addContentView(layout, params);*/
        setContentView(R.layout.dialog_progress);
    }
}
