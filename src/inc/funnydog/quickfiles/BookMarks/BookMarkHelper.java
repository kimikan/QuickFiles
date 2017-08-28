package inc.funnydog.quickfiles.BookMarks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.BookMark;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.Helpers.ICallback;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;

public class BookMarkHelper {

    public static void modifyBookmark(final Context context
            , final BookMarkDisplayableFile bookMark, final ICallback callback) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_app_new_group, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle(R.string.bookmarks_modify_title);
        alertDialogBuilder.setIcon(R.drawable.bookmark_modify);
        final EditText groupName = (EditText) promptsView
                .findViewById(R.id.et_group_name);
        final EditText groupDescription = (EditText) promptsView
                .findViewById(R.id.et_group_description);
        groupDescription.setEnabled(false);
        if(groupName != null && groupDescription != null) {
            groupName.setText(bookMark.getName());
            groupDescription.setText(bookMark.getLastModifiedTime());
        }
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    String newName = groupName.getText().toString();
                                    
                                    if(newName != bookMark.getName()) {
                                        if(newName != null && newName.length() > 0) {
                                            final BookMark group = bookMark._bookMark;
                                            group.BookmarkName = newName;
                                            new ProgressableTask<Void, Void, Void>(context) {
                                                
                                                private boolean isOk = false;
                                                @Override
                                                protected Void doInBackground(
                                                        Void... arg0) {
                                                    DatabaseHelper dbHelper = DatabaseHelper.instance();
                                                    
                                                    try {
                                                        dbHelper.getBookMarkDataDao().update(group);
                                                        isOk = true;
                                                    } catch(Exception ex) {
                                                        ex.printStackTrace();
                                                    }
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Void result) {
                                                    if(isOk) {
                                                        if(callback != null) {
                                                            callback.onFinished(null);
                                                        }
                                                    } else {
                                                        Toast.makeText(context, 
                                                                R.string.bookmarks_modify_title_failed, 
                                                                Toast.LENGTH_LONG).show();                                                    }
                                                    super.onPostExecute(result);
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
}
