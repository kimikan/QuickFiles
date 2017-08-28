package inc.funnydog.quickfiles.AppManager;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.ApkGroup;
import inc.funnydog.quickfiles.DB.ApkItem;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.Helpers.ICallback;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;

public class AppManagerHelper {
    
    public static void addNewGroup(final Context context, final ICallback callback) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_app_new_group, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle(R.string.appmgr_new_group_title);
        alertDialogBuilder.setIcon(R.drawable.browse_new_folder);
        final EditText groupName = (EditText) promptsView
                .findViewById(R.id.et_group_name);
        final EditText groupDescription = (EditText) promptsView
                .findViewById(R.id.et_group_description);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    String newName = groupName.getText().toString();
                                    String newDescription = groupDescription.getText().toString();
                                    final ApkGroup group = new ApkGroup();
                                    if(newName != null && newName.length() > 0) {
                                        group.Name = newName;
                                        group.Description = newDescription;
                                        new ProgressableTask<Void, Void, Void>(context) {
                                            
                                            private boolean isOk = false;
                                            @Override
                                            protected Void doInBackground(
                                                    Void... arg0) {
                                                DatabaseHelper dbHelper = DatabaseHelper.instance();
                                                
                                                try {
                                                    dbHelper.getGroupDataDao().createIfNotExists(group);
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
                                                    if(callback != null) {
                                                        callback.onFinished(null);
                                                    }
                                                } else {
                                                    Toast.makeText(context, 
                                                            R.string.appmgr_new_group_failed, 
                                                            Toast.LENGTH_LONG).show();                                                    }
                                            }
                                            
                                        }.execute();
                                    }
                                    
                                    
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show(); 
    }

    public static void addtoGroup(final Context context, 
            final List<DisplayableAppFile> files, 
            final ICallback callback) {
        ListView view = new ListView(context);
        final List<ApkGroup> groups = new ArrayList<ApkGroup>();
        final ArrayAdapter<ApkGroup> adapter = new ArrayAdapter<ApkGroup>(
                context,
                android.R.layout.simple_expandable_list_item_1,
                groups);
        view.setAdapter(adapter);
        new ProgressableTask<Void, Void, Void>(context) {
            List<ApkGroup> tempList;
            @Override
            protected Void doInBackground(Void... params) {
                DatabaseHelper dbHelper = DatabaseHelper.instance();
                
                try {
                    tempList = dbHelper.getGroupDataDao().queryForAll();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(tempList != null && tempList.size() > 0) {
                    groups.addAll(tempList);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.appmgr_no_group_found, 
                            Toast.LENGTH_LONG).show();
                }
            }
            
        }.execute();
       
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(R.string.appmgr_choose_folder);
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                DatabaseHelper dbHelper = DatabaseHelper.instance();
                
                try {
                    ApkGroup apkGroup = groups.get(arg2);
                    
                    for(DisplayableAppFile file : files) {
                        ApkItem itemApk = new ApkItem();
                        itemApk.GroupId = apkGroup.Id;
                        itemApk.PackageName = file.getLastModifiedTime();
                        dbHelper.getItemDataDao().createIfNotExists(itemApk);
                    }
                    
                    if(callback != null) {
                        callback.onFinished(null);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(context, R.string.appmgr_join_group_failed, 
                            Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
            
        });
    }
}
