package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;
import java.util.Stack;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewHelper {

    public interface IViewCallback {
        void onClick(Context context, File file);
    }
    
    private static View getView(final Context context, 
            final File file, final IViewCallback back) {
        
        TextView textView = new TextView(context);
        textView.setTextSize(22);
        //textView.setTypeface(null, Typeface.BOLD);
        String fileName = file.getName();
        if(file.getAbsolutePath().equals("/")) {
            fileName = "root";
        }
        
        textView.setText(back == null ? fileName
                : Html.fromHtml("<u>"+ fileName +"</u>"));
        
        if(back != null) {
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    back.onClick(context, file);
                }
                
            });
        }
        
        return textView;
    }
    
    private static View getDivider(Context context) {
        TextView textView = new TextView(context);
        textView.setText(" > ");
        return textView;
    }
    
    public static void buildNavigationPane(Context context, 
            LinearLayout layout, File file, IViewCallback back) {
        if(context == null || file == null)
            return;
        
        layout.removeAllViews();
        
        Stack<File> parents = new Stack<File>();
        File parent = file.getParentFile();
        while(parent != null) {
            parents.add(parent);
            parent = parent.getParentFile();
        }
        
        while(!parents.empty()) {
            parent = parents.pop();
            View view = getView(context, parent, back);
            layout.addView(view);
            layout.addView(getDivider(context));
        }
        
        layout.addView(getView(context, file, null));
        
    }
}
