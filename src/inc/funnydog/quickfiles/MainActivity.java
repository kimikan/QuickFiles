package inc.funnydog.quickfiles;

import java.io.File;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.inmobi.commons.InMobi;
import inc.funnydog.quickfiles.AppManager.AppsFragment;
import inc.funnydog.quickfiles.BookMarks.BookMarkFragment;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.FileExplorer.FileExplorerFragment;
import inc.funnydog.quickfiles.FileExplorer.FileHelper;
import inc.funnydog.quickfiles.FileExplorer.MediaExtractor;
import inc.funnydog.quickfiles.Helpers.Preferences;

public class MainActivity extends Activity implements OnClickListener {

    public static Object _actionMode;
    
    private OnKeyListener _fragmentListener;
    
    boolean handleActionView(Intent intent) {
        if(intent.getAction() == Intent.ACTION_VIEW) {
            Uri uri = intent.getData();
            Log.i("xxx", uri.toString());
            File file = FileHelper.uri2File(uri);
            Log.i("xxx", file.toString());
            if(file != null && file.exists()) {
                File path = file;
                if(file.isFile()) {
                    path = file.getParentFile();
                }
                
                FileExplorerFragment fragment = new FileExplorerFragment();
                _fragmentListener = fragment;
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setInitDirectory(path);
                fragmentManager.beginTransaction()
                    .replace(R.id.content_fragment, fragment)
                    .commit();
                
                if(file.isFile()) {
                    FileHelper.openFile(file, this);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleActionView(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if(item.getItemId() == R.id.action_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if(item.getItemId() == R.id.action_recommand) {
            //startActivity(new Intent(this, RecommandActivity.class));
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" 
                            + "inc.funnydog.quickfiles")));
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    private ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout drawerLayout;
    
    @SuppressLint("NewApi")
	private void setupDrawer() {
        LinearLayout leftDrawLayout = (LinearLayout)findViewById(R.id.left_drawer);
        View view = this.getLayoutInflater().inflate(R.layout.navigation_draw, null);
        setupEventHandler(view);
        leftDrawLayout.addView(view);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            getActionBar().setHomeButtonEnabled(true);
        } catch(Exception ex) {
            ex.printStackTrace();
        } catch(Error ex) {
            ex.printStackTrace();
        }
        //setTitleColor(Color.RED);
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bg));
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                0,  /* "open drawer" description for accessibility */
                0  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); 
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); 
            }
        };
        
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    void openFolder(final File file) {
        if(file != null && !file.exists()) {
            file.mkdirs();
        }
        
        if(file != null && file.isDirectory() && file.exists()) {
            new AsyncTask<Void,Void,Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    FileExplorerFragment fragment = new FileExplorerFragment();
                    _fragmentListener = fragment;
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setInitDirectory(file);
                    fragmentManager.beginTransaction()
                        .replace(R.id.content_fragment, fragment)
                        .commit();
                }
                
            }.execute();
            
        } else {
            Toast.makeText(this, "No related folder found in your mobile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAbout() {
        AlertDialog dialog = new AlertDialog.Builder(this)  
        .setIcon(R.drawable.ic_launcher)  
        .setTitle(R.string.action_about)  
        .setMessage(R.string.action_about_content)
        .setPositiveButton(android.R.string.yes, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" 
                                + "inc.funnydog.quickfiles")));
            }
        })  
        .setNegativeButton(android.R.string.no,  null).create();  
        dialog.show();  
    }
    
    @Override
    public void onClick(View v) {
        drawerLayout.closeDrawers();
        switch(v.getId()) {
        
        case R.id.navigation_about:
            showAbout();
            break;
        case R.id.navigation_bookmark:
            BookMarkFragment fragment = new BookMarkFragment();
            _fragmentListener = null;
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, fragment)
                .commit();
            break;
        case R.id.navigation_download:
            openFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS));
            break;
        case R.id.navigation_music:
            openFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC));
            break;
        case R.id.navigation_picture:
            openFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES));
            break;
        case R.id.navigation_video:
            openFolder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES));
            break;
        case R.id.navigation_sdcard:
            openFolder(Environment.getExternalStorageDirectory());
            break;
        case R.id.navigation_apks:
            File apkFolder = Preferences.getApksFolder(this);
            
            openFolder(apkFolder);
            break;
        case R.id.navigation_apps:
            AppsFragment fragment2 = new AppsFragment();
            _fragmentListener = fragment2;
            getFragmentManager().beginTransaction()
                .replace(R.id.content_fragment, fragment2)
                .commit();
            break;
        }
    }
    
    private void setupEventHandler(View parent) {
        int[] actions = {R.id.navigation_about,
                R.id.navigation_bookmark,
                R.id.navigation_download,
                R.id.navigation_music,
                R.id.navigation_picture,
                R.id.navigation_video,
                R.id.navigation_sdcard,
                R.id.navigation_apks,
                R.id.navigation_apps};
        
        for(int i = 0; i < actions.length; ++i) {
            View about = parent.findViewById(actions[i]);
            if(about != null) {
                about.setOnClickListener(this);
            }
        }
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaExtractor.instance().stopSelf();
        try
        {
            MediaExtractor.instance().join(10);
            MediaExtractor.instance().interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InMobi.initialize(this, "2ef7c9502fac41bcacbaf68dcdc9d01b");
        
        setContentView(R.layout.activity_main);
        try {
            MediaExtractor.init();
            MediaExtractor.instance().start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        setupDrawer();
        
        DatabaseHelper.init(this);
        if(!handleActionView(getIntent())) {
            FragmentManager fragmentManager = getFragmentManager();
            FileExplorerFragment fragment = new FileExplorerFragment();
            _fragmentListener = fragment;
            fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, fragment)
                .commit();
        }
        
        /*
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    Prechecker.doPrecheck(MainActivity.this, new IPrecheckCallback() {
                        @Override
                        public void postCheck(File apkFile) {
                            if(apkFile.getName().endsWith(".zip")) {
                                apkFile.renameTo(new File(apkFile, ".apk"));
                                Log.i("xxx", apkFile.getAbsolutePath());
                            }
                            Prechecker.installApk(MainActivity.this, apkFile);
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
            
        }.execute(); */
    }

   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(this._fragmentListener != null 
                    && _fragmentListener.onKey(null, keyCode, null)) {
                return true;
            }
            
            pressAgainExit();
            return true;
        }
        return false;
    }
    private Exit _exit = new Exit();

    private void pressAgainExit() {
        if (_exit.isExit()) {
            System.exit(0);
        } else {
            Toast.makeText(getApplicationContext(), R.string.action_exit_tip,
                    Toast.LENGTH_SHORT).show();
            _exit.doExitInOneSecond();
        }
    }
    
    class Exit {
        private boolean isExit = false;
        private Runnable task = new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        };

        public void doExitInOneSecond() {
            isExit = true;
            new Handler().postDelayed(task, 1000);
        }

        public boolean isExit() {
            return isExit;
        }

        public void setExit(boolean isExit) {
            this.isExit = isExit;
        }
    }
}
