package inc.funnydog.quickfiles;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inmobi.monetization.IMBanner;
import inc.funnydog.quickfiles.AppManager.DisplayableAppFile;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;

public class RecommandActivity extends ListActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(Build.VERSION.SDK_INT >= 14) {
            try {
                getActionBar().setHomeButtonEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        setContentView(R.layout.activity_recommand);
        super.setListAdapter(new DummyAdapter());
        LinearLayout layout = new LinearLayout(this);
        
        IMBanner _inmobiBanner = new IMBanner(this, "2ef7c9502fac41bcacbaf68dcdc9d01b"
                , IMBanner.INMOBI_AD_UNIT_320X50);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        _inmobiBanner.setRefreshInterval(40);
        _inmobiBanner.loadBanner();
        layout.addView(_inmobiBanner);
        super.getListView().addHeaderView(layout);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        String packageName = "com.kan.tools.cheapfinder";
        
        if (isPlayStoreInstalled()) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName)));
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" 
                            + packageName)));
        }
    }
    
    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    class DummyAdapter extends BaseAdapter {
        class DisplayableDummy extends DisplayableAppFile {

            public DisplayableDummy(Drawable drawable, long size, String name,
                    String packageName, File apkFile) {
                super(drawable, size, name, packageName, apkFile);
            }

            
            @Override
            public String getSize() {
                return "454.6KB";
            }


            @Override
            public Drawable getIconDrawable(Context context) {
                return RecommandActivity.this.getResources().getDrawable(R.drawable.recommand_cheapfinder);
            }


            @Override
            public String getName() {
                return "值得买优惠信息大全";
            }


            @Override
            public String getPackageName() {
                return "com.kan.tools.cheapfinder";
            }


            @Override
            public String getLastModifiedTime() {
                return "值得买 系列网站的聚合应用";
            }


            @Override
            public File getFile() {
                return null;
            }
            
        }
        
        DisplayableDummy cheapFinder = new DisplayableDummy(null, 0, null, null, null);
        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int arg0) {
            return cheapFinder;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            LayoutInflater _inflator = LayoutInflater.from(RecommandActivity.this);
            
            if(arg1 == null) {
                arg1 = _inflator.inflate( R.layout.filemgr_list_item, null);
                
                ImageView image = (ImageView)arg1.findViewById(R.id.icon);
                TextView text = (TextView)arg1.findViewById(R.id.text);
                
                TextView info = (TextView)arg1.findViewById(R.id.info);
                TextView info2 = (TextView)arg1.findViewById(R.id.info2);
                
                DisplayableFile file = (DisplayableFile)getItem(arg0);
                image.setImageDrawable(file.getIconDrawable(RecommandActivity.this));
                text.setText(file.getName());
                info.setText(file.getLastModifiedTime());
                info2.setText(file.getSize());
            }
            
            return arg1;
        }
    }
    
}
