package inc.funnydog.quickfiles.AppManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import inc.funnydog.quickfiles.FragmentViewBase;
import inc.funnydog.quickfiles.MainActivity;
import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.ApkGroup;
import inc.funnydog.quickfiles.DB.ApkItem;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;
import inc.funnydog.quickfiles.Helpers.ICallback;
import inc.funnydog.quickfiles.Helpers.Preferences;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;
import inc.funnydog.quickfiles.Helpers.StringsHelper;

public class AppsFragment extends FragmentViewBase implements OnKeyListener{
    
    private inc.funnydog.quickfiles.DB.ApkGroup currentGroup = null;
    
    private DatabaseHelper dbHelper = DatabaseHelper.instance();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        loadData();
        getActivity().setTitle(R.string.appmgr_title);
        return view;
    }
    
    @Override
    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
        if(currentGroup != null) {
            backFolder();
            return true;
        }
        
        return false;
    }
    
    
    @Override
    public void onAttach(Activity activity) {
        AppNotificationReciever.callback = new ICallback() {
            @Override
            public void onFinished(Object obj) {
                AppsFragment.this.loadData();
            }
        };
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppNotificationReciever.callback = null;
        android.util.Log.i("xxx", "Detached");
    }

    private DisplayableFile getByApplicationInfo(PackageManager appManager
            , ApplicationInfo info) {
        DisplayableAppFile file = null;
        try {
            if ((info.flags != ApplicationInfo.FLAG_SYSTEM) && info.enabled) {
                if (info.icon != 0 
                        && !info.sourceDir.startsWith("/data/app-private/")) {
    
                    String name = appManager.getApplicationLabel(info).toString();
                    String packageName = info.packageName;
                    Drawable drawable = appManager.getDrawable(info.packageName, info.icon, info);
                    File apkFile = new File(info.sourceDir);
    
                    file = new DisplayableAppFile(drawable, apkFile.length(), name, 
                            packageName, apkFile);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return file;
    }
    
    protected int isPackageInstalled(ArrayList<DisplayableFile> apks, 
            String packageName) {
        for(int i = 0; i < apks.size(); ++i) {
            DisplayableFile item = apks.get(i);

            if(item.getLastModifiedTime().equals(packageName)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private void removeDatabaseItems(ArrayList<DisplayableFile> apks) {
       
        List<ApkItem> items = dbHelper.getItemDataDao().queryForAll();
        for(ApkItem item : items ) {
            
            int index = isPackageInstalled(apks, item.PackageName);
            if(index >= 0) {
                apks.remove(index);
            } else {
                item.isDeleted = true;
            }
        }
        
        for(ApkItem item : items) {
            if(item.isDeleted) {
                dbHelper.getItemDataDao().delete(item);
            }
        }
        //Add groups
        List<ApkGroup> groups = dbHelper.getGroupDataDao().queryForAll();
        for(ApkGroup group : groups) {
            DisplayableAppFolder item = new DisplayableAppFolder(group);
            apks.add(item);
        }
    }
    
    private void removeAllNotInGroup(ArrayList<DisplayableFile> apks, ApkGroup group) {
        QueryBuilder<ApkItem,Integer> uQb = dbHelper.getItemDataDao().queryBuilder();
        ArrayList<DisplayableFile> tempApks = new ArrayList<DisplayableFile>();
        
        try
        {
            List<ApkItem> items = uQb.where().eq(ApkItem.GROUP_COL, currentGroup.Id).query();
            for(ApkItem item : items ) {
                int index = isPackageInstalled(apks, item.PackageName);
                if(index < 0) {
                    item.isDeleted = true;
                } else {
                    tempApks.add(apks.get(index));
                }
            }
            
            for(ApkItem item : items) {
                if(item.isDeleted) {
                    dbHelper.getItemDataDao().delete(item);
                }
            }
            apks.clear();
            apks.addAll(tempApks);
        } catch (SQLException e) {
            e.printStackTrace();
            apks.clear();
        }
    }
    
    private void loadApks(ArrayList<DisplayableFile> apks) {
        PackageManager appManager = getActivity().getPackageManager();
        List<ApplicationInfo> listInfo = appManager.getInstalledApplications(0);

        for (ApplicationInfo info : listInfo) {
            DisplayableFile file = getByApplicationInfo(appManager, info);
            if(file != null) {
                apks.add(file);
            }
        }
        
        if(currentGroup == null) {
            removeDatabaseItems(apks);
        } else {
            removeAllNotInGroup(apks, currentGroup);
        }
        
        Collections.sort(apks, new Comparator<DisplayableFile>() {
            @Override
            public int compare(DisplayableFile arg0, DisplayableFile arg1) {
                if(arg0 instanceof DisplayableAppFolder) {
                    return -1000;
                } else if(arg1 instanceof DisplayableAppFolder) {
                    return 1000;
                }
                
                if(arg0 != null && arg1 != null) {
                    return arg0.getName().compareTo(arg1.getName());
                }
                
                return 0;
            }
        });
    }
    
    private void loadData() {
        if(MainActivity._actionMode != null) {
            ((ActionMode)MainActivity._actionMode).finish();
            MainActivity._actionMode = null;
        }
        
        new ProgressableTask<Void, Void, List<DisplayableFile>> (getActivity()) {
            @Override
            protected List<DisplayableFile> doInBackground(Void... params) {
                ArrayList<DisplayableFile> files = new ArrayList<DisplayableFile>();
                try {
                    loadApks(files);
                    
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
                return files;
            }

            @Override
            protected void onPostExecute(List<DisplayableFile> result) {
                super.onPostExecute(result);
                listFiles(result);
            }
            
        }.execute();
    }
    
    private void backFolder() {
        currentGroup = null;
        getActivity().invalidateOptionsMenu();
        loadData();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.appmanager_view, menu);
        if(this.currentGroup != null) {
            inflater.inflate(R.menu.appmanager_view_back, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_view_switch:
            setListView(!_isListView);
            break;
        case R.id.action_refresh:
            loadData();
            break;
        case R.id.action_recommand:
            break;
        case R.id.action_setting:
            break;
        case R.id.action_appmgr_back:
            backFolder();
            break;
        case R.id.action_new_folder:
            AppManagerHelper.addNewGroup(getActivity(), new ICallback() {
                @Override
                public void onFinished(Object obj) {
                    loadData();
                }
            });
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUpdateModeMenu(MenuInflater menuInflator, Menu menu,
            boolean isViewMode) {
        if(menuInflator == null || menu == null) {
            return ;
        }
        
        menu.clear();
        if(isViewMode) {
            menuInflator.inflate(R.menu.appmanager_view, menu);
            if(this.currentGroup != null) {
                menuInflator.inflate(R.menu.appmanager_view_back, menu);
            }
        } else {
            int selectedCount = super.getSelectedCount();
            if(currentGroup == null) {
                List<DisplayableFile> files = getSelectedFiles();
                boolean isContainApp = false;
                boolean isContainGroup = false;
                for(DisplayableFile file : files) {
                    if(file instanceof DisplayableAppFile) {
                        isContainApp = true;
                    } else {
                        isContainGroup = true;
                    }
                    
                    if(isContainApp && isContainGroup)
                        break;
                }
                
                if(isContainApp && isContainGroup) {
                    
                } else if(isContainApp) {
                    if(selectedCount == 1) {
                        menuInflator.inflate(R.menu.appmanager_edit_outgroup, menu);
                    } else {
                        menuInflator.inflate(R.menu.appmanager_edit_outgroup2, menu);
                    }
                } else if(isContainGroup) {
                    if(selectedCount == 1) {
                        menuInflator.inflate(R.menu.appmanager_edit_group, menu);
                    } else {
                        menuInflator.inflate(R.menu.appmanager_edit_group2, menu);
                    }
                }
            } else {
                if(selectedCount == 1) {
                    menuInflator.inflate(R.menu.appmanager_edit_ingroup, menu);
                } else {
                    menuInflator.inflate(R.menu.appmanager_edit_ingroup2, menu);
                }
            }
        }
    }

    @Override
    protected void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3, boolean isViewMode) {
        if(isViewMode) {
            DisplayableFile file = (DisplayableFile)_adapter.getItem(arg2);
            if(file instanceof DisplayableAppFile) {
                //Open app
                runApp((DisplayableAppFile)file);
            } else {
                try {
                    DisplayableAppFolder folder = (DisplayableAppFolder)file;
                    currentGroup = folder.getApkGroup();
                    //this.onUpdateModeMenu(, menu, isViewMode)
                    getActivity().invalidateOptionsMenu();
                    loadData();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void deleteAppFolder(DisplayableAppFolder folder) {
        if(folder != null) {
            DatabaseHelper dbHelper = DatabaseHelper.instance();
            ApkGroup apkGroup = folder.getApkGroup();
            
            try {
                DeleteBuilder<ApkItem, Integer> deleteBuilder = dbHelper.getItemDataDao().deleteBuilder();
                deleteBuilder.where().eq(ApkItem.GROUP_COL, apkGroup.Id);
                deleteBuilder.delete();
                
                dbHelper.getGroupDataDao().delete(apkGroup);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 111) {
            loadData();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void uninstallApps(final List<DisplayableFile> files) {
        for(DisplayableFile file : files) {
            if(file instanceof DisplayableAppFile) {
                String packageName = ((DisplayableAppFile)file).getPackageName();
                
                Uri packageUri = Uri.parse("package:" + packageName);
                Intent uninstallIntent;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                } else {
                    uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                }
                getActivity().startActivityForResult(uninstallIntent, 111);
            }
        }
        
    }
    
    private void extractApks(final List<DisplayableFile> files) {
        final File apkFolder = Preferences.getApksFolder(getActivity());
        final StringBuffer sb = new StringBuffer();
        new ProgressableTask<Void, Void,Void>(getActivity()){
            @Override
            protected Void doInBackground(Void... params) {
                for(DisplayableFile file : files) {
                    if(file instanceof DisplayableAppFile) {
                        File apkFile = ((DisplayableAppFile)file).getFile();
                        try {
                            FileUtils.copyFileToDirectory(apkFile, 
                                    apkFolder, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            sb.append(file.getName() +" ");
                        }
                    }
                } // end for
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(sb.length() > 0) {
                    String failedString = getActivity().getResources().getString(R.string.appmgr_extractapks_failed);
                    Toast.makeText(getActivity(), sb.toString() + failedString, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.appmgr_extractapks_success, Toast.LENGTH_SHORT).show();
                }
            }
            
        }.execute();
        
    }
    
    private void modifyFolder(final DisplayableAppFolder folder) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.dialog_app_new_group, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle(R.string.appmgr_modify_group_title);
        alertDialogBuilder.setIcon(R.drawable.appmgr_edit_group_modify);
        final EditText groupName = (EditText) promptsView
                .findViewById(R.id.et_group_name);
        final EditText groupDescription = (EditText) promptsView
                .findViewById(R.id.et_group_description);
        if(groupName != null && groupDescription != null) {
            groupName.setText(folder.getName());
            groupDescription.setText(folder.getDescription());
        }
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    String newName = groupName.getText().toString();
                                    String newDescription = groupDescription.getText().toString();
                                    if(newName != folder.getName()
                                            || newDescription != folder.getDescription()) {
                                        if(newName != null && newName.length() > 0) {
                                            final ApkGroup group = folder.getApkGroup();
                                            group.Name = newName;
                                            group.Description = newDescription;
                                            new ProgressableTask<Void, Void, Void>(getActivity()) {
                                                
                                                private boolean isOk = false;
                                                @Override
                                                protected Void doInBackground(
                                                        Void... arg0) {
                                                    DatabaseHelper dbHelper = DatabaseHelper.instance();
                                                    
                                                    try {
                                                        dbHelper.getGroupDataDao().update(group);
                                                        isOk = true;
                                                    } catch(Exception ex) {
                                                        ex.printStackTrace();
                                                    }
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Void result) {
                                                    super.onPostExecute(result);
                                                    if(isOk) {
                                                        loadData();
                                                    } else {
                                                        Toast.makeText(getActivity(), 
                                                                R.string.appmgr_modify_group_failed, Toast.LENGTH_LONG).show();                                                    }
                                                    
                                                }
                                                
                                            }.execute();
                                        }
                                    }
                                    
                                    
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
    
    private void leaveGroup(final List<DisplayableFile> files) {
        new ProgressableTask<Void, Void, Boolean>(getActivity()) {
            @Override
            protected Boolean doInBackground(Void... params) {
                DatabaseHelper dbHelper = DatabaseHelper.instance();
                
                try {
                    for(DisplayableFile file: files) {
                        if(file instanceof DisplayableAppFile) {
                            String packageName = ((DisplayableAppFile)file).getPackageName();
                            DeleteBuilder<ApkItem, Integer> deleteBuilder 
                                = dbHelper.getItemDataDao().deleteBuilder();
                            deleteBuilder.where().eq(ApkItem.PACKAGE_COL, packageName);
                            deleteBuilder.delete();
                        }
                    }
                    
                    return true;
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if(result.booleanValue()) {
                    loadData();
                }
                
                Toast.makeText(getActivity(), result.booleanValue() 
                        ? R.string.appmgr_leavegroup_success: R.string.appmgr_leavegroup_failed, 
                        Toast.LENGTH_SHORT).show(); 
            }
            
        }.execute();
    }
        
    private void runApp(DisplayableAppFile app) {
        Intent intent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(app.getPackageName());
        if (intent != null) {
            getActivity().startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.appmgr_run_app, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onMenuItemClicked(ActionMode mode, MenuItem item) {
        final List<DisplayableFile> files = getSelectedFiles();
        
        switch(item.getItemId()) {
        case R.id.action_app_extract:
            extractApks(files);
            break;
        case R.id.action_app_run:
            if(files.size() == 1) {
                DisplayableFile file = files.get(0);
                if(file instanceof DisplayableAppFile) {
                    runApp((DisplayableAppFile)file);
                }
            }
            break;
        case R.id.action_app_group_delete:
            
            new AlertDialog.Builder(getActivity())
            .setInverseBackgroundForced(true)
            .setTitle(String.format(StringsHelper.getString(R.string.appmgr_del_groups), files.size()))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new ProgressableTask<Void,Void,Void>(getActivity()) {
                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            loadData();
                        }
                        @Override
                        protected Void doInBackground(Void... params){
                            for(DisplayableFile file : files) {
                                if(file instanceof DisplayableAppFolder) {
                                    deleteAppFolder((DisplayableAppFolder)file);
                                }
                            }
                            return null;
                        }
                        
                    }.execute();
                }
            })
            .setIcon(files.get(0).getIconDrawable(getActivity()))
            .setNegativeButton(android.R.string.cancel, null)
            .create().show();
            
            break;
        case R.id.action_app_group_modify:
            if(files.size() == 1) {
                DisplayableFile file = files.get(0);
                if(file instanceof DisplayableAppFolder) {
                    DisplayableAppFolder folder = (DisplayableAppFolder)file;
                    modifyFolder(folder);
                }
            }
            break;
        case R.id.action_app_group_open:
            if(files.size() == 1) {
                DisplayableFile file = files.get(0);
                if(file instanceof DisplayableAppFolder) {
                    DisplayableAppFolder folder = (DisplayableAppFolder)file;
                    currentGroup = folder.getApkGroup();
                    loadData();
                }
            }
            break;
        case R.id.action_app_view_in_market:
            if(files.size() == 1) {
                DisplayableFile file = files.get(0);
                if(file instanceof DisplayableAppFile) {
                    String packageName = ((DisplayableAppFile)file).getPackageName();
                    if (isPlayStoreInstalled()) {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + packageName)));
                    } else {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" 
                                        + packageName)));
                    }
                }
            }
            break;
        case R.id.action_app_uninstall:
            uninstallApps(files);
            break;
        case R.id.action_app_leave_group:
            leaveGroup(files);
            break;
        case R.id.action_app_join_group:
            
            ArrayList<DisplayableAppFile> apps = new ArrayList<DisplayableAppFile>();
            for(DisplayableFile file : files) {
                if(file instanceof DisplayableAppFile) {
                    apps.add((DisplayableAppFile)file);
                }
            }
            if(apps.size() > 0) {
                AppManagerHelper.addtoGroup(getActivity(), apps, new ICallback() {
                    @Override
                    public void onFinished(Object obj) {
                        loadData();
                    }
                    
                });
            }
            break;
        }
    
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_bookmarks;
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }
}

