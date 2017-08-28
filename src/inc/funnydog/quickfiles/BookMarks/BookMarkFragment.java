package inc.funnydog.quickfiles.BookMarks;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import inc.funnydog.quickfiles.FragmentViewBase;
import inc.funnydog.quickfiles.MainActivity;
import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.BookMark;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;
import inc.funnydog.quickfiles.FileExplorer.FileExplorerFragment;
import inc.funnydog.quickfiles.FileExplorer.FileHelper;
import inc.funnydog.quickfiles.Helpers.ICallback;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;
import inc.funnydog.quickfiles.Helpers.StringsHelper;

public class BookMarkFragment extends FragmentViewBase {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View view = super.onCreateView(inflater, container, savedInstanceState);
        loadData();
        getActivity().setTitle(R.string.bookmarks_title);
        return view;
    }
    
    private void loadData() {
        if(MainActivity._actionMode != null) {
            ((ActionMode)MainActivity._actionMode).finish();
            MainActivity._actionMode = null;
        }
        
        new ProgressableTask<Void, Void, List<DisplayableFile>> (getActivity()) {
            @Override
            protected List<DisplayableFile> doInBackground(Void... params) {
                ArrayList<DisplayableFile> files = null;
                try {
                    RuntimeExceptionDao<BookMark, Integer> dao = 
                            DatabaseHelper.instance().getBookMarkDataDao();
                    files = new ArrayList<DisplayableFile>();
                    
                    List<BookMark> bookmarks = dao.queryForAll();
                    for(BookMark bookmark : bookmarks) {
                        files.add(new BookMarkDisplayableFile(bookmark));
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
                return files;
            }

            @Override
            protected void onPostExecute(List<DisplayableFile> result) {
                super.onPostExecute(result);
                BookMarkFragment.this.listFiles(result);
            }
            
        }.execute();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_view_switch:
            setListView(!_isListView);
            break;
            
        case R.id.action_recommand:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUpdateModeMenu(MenuInflater menuInflator, Menu menu,
            boolean isViewMode) {
        if(menuInflator == null || menu == null) {
            return;
        }
        
        menu.clear();
        if(isViewMode) {
            menuInflator.inflate(R.menu.main, menu);
        } else {
            if(super.getSelectedCount() == 1) {
                menuInflator.inflate(R.menu.bookmark_rename, menu);
            }
            
            menuInflator.inflate(R.menu.bookmark_edit, menu);
        }
    }

    @Override
    protected void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3, boolean isViewMode) {
        if(isViewMode) {
            BookMarkDisplayableFile file = (BookMarkDisplayableFile)_adapter.getItem(arg2);
            if(file != null) {
                File file2 = file.getFile();
                if(file2 == null)
                    return;
                
                if(file2.exists()) {
                    if(file2.isDirectory()) {
                        FileExplorerFragment fragment = new FileExplorerFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragment.setInitDirectory(file2);
                        fragmentManager.beginTransaction()
                            .replace(R.id.content_fragment, fragment)
                            .commit();
                    } else {
                        FileHelper.openFile(file2, getActivity());
                    }
                } else {
                    Toast.makeText(getActivity(), 
                            R.string.bookmarks_targetfiles_deleted, 
                            Toast.LENGTH_LONG).show();
                }
                
            }
        }
    }

    private void deleteBookmarks(final List<DisplayableFile> files) {
        
        new AlertDialog.Builder(getActivity())
        .setInverseBackgroundForced(true)
        .setTitle(String.format(StringsHelper.getString(R.string.bookmarks_del), files.size()))
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    new ProgressableTask<File, Void, Void> (getActivity()){
                        
                        boolean bResult = false;
                        
                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            super.onPostExecute(result);
                            if(!bResult) {
                                Toast.makeText(getActivity(), 
                                        R.string.bookmarks_del_failed, Toast.LENGTH_LONG).show();
                            } else {
                                loadData();
                                Toast.makeText(getActivity(), 
                                        R.string.bookmarks_del_success, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        protected Void doInBackground(File... arg0) {
                            bResult = true;
                            for(DisplayableFile file : files) {
                                if(file instanceof BookMarkDisplayableFile) {
                                    BookMarkDisplayableFile bookmark = ((BookMarkDisplayableFile)file);
                                    DatabaseHelper dbHelper = DatabaseHelper.instance();
                   
                                    DeleteBuilder<BookMark, Integer> deleteBuilder 
                                        = dbHelper.getBookMarkDataDao().deleteBuilder();
                                    try
                                    {
                                        deleteBuilder.where().eq(BookMark.ID_COL, bookmark._bookMark.Id);
                                        deleteBuilder.delete();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        bResult = false;
                                    }
                                }
                            } // end for
                            return null;
                        }
                    }.execute();
            }
        })
        .setIcon(files.get(0).getIconDrawable(getActivity()))
        .setNegativeButton(android.R.string.cancel, null)
        .create().show();
    }
    
    
    @Override
    protected void onMenuItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_bookmark_delete:
            deleteBookmarks(getSelectedFiles());
            break;
        case R.id.action_bookmark_rename:
            List<DisplayableFile> files = super.getSelectedFiles();
            if(files != null && files.size() == 1) {
                DisplayableFile file = files.get(0);
                if(file instanceof BookMarkDisplayableFile) {
                    BookMarkHelper.modifyBookmark(getActivity(), 
                            (BookMarkDisplayableFile)file, new ICallback() {
                                @Override
                                public void onFinished(Object obj) {
                                    loadData();
                                }
                    });
                }
            }
            break;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_bookmarks;
    }

}

