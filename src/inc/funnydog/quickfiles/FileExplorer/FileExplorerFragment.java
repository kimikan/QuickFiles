package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import inc.funnydog.quickfiles.MainActivity;
import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.SettingActivity;
import inc.funnydog.quickfiles.FileExplorer.FileHelper.IOperationCallback;
import inc.funnydog.quickfiles.FileExplorer.Ftp.FtpServerActivity;
import inc.funnydog.quickfiles.Helpers.ICallback;
import inc.funnydog.quickfiles.Helpers.NavigationHistory;
import inc.funnydog.quickfiles.Helpers.Preferences;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;

public class FileExplorerFragment extends Fragment implements 
    OnKeyListener {

    private List<DisplayableFile> _files = new ArrayList<DisplayableFile>();
    
    private FileAdapter _adapter;
    
    private boolean _isListView = SettingActivity.isListView();
    
    private boolean _isShowHideFiles = SettingActivity.showHideFiles();
    
    private ListView _listView;
    
    private GridView _gridView;
    
    private boolean _isViewMode = true;
    
    private LinearLayout _navigatonPane;
    
    private LinearLayout _editPane;
    
    private Menu _mainMenu;
    
    private File _currentDirectory;
    
    private ImageButton _controlLeft;
    
    private ImageButton _controlRight;
    
    private Button _controlCenter;
    
    public void setInitDirectory(File file) {
        _currentDirectory = file;
    }
    
    @Override
    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
        if(arg1 == KeyEvent.KEYCODE_BACK) {
            File file = null;
            while(true) {
                file = NavigationHistory.instance().pop();
                if(file == null)
                    return false;
                if(file.equals(_currentDirectory)) {
                    continue;
                }
                
                navigateToDir(file, false);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        
        if(inflater != null) {
            View view = inflater.inflate(R.layout.fragment_file_explorer, null);
            _listView = (ListView)view.findViewById(R.id.file_list);
            _gridView = (GridView)view.findViewById(R.id.file_grid);
            _navigatonPane = (LinearLayout)view.findViewById(R.id.navigation_pane);
            _editPane = (LinearLayout)view.findViewById(R.id.edit_pane);
            _controlLeft = (ImageButton)view.findViewById(R.id.control_left);
            _controlRight = (ImageButton)view.findViewById(R.id.control_right);
            _controlCenter = (Button)view.findViewById(R.id.control_center);
            setupEvents();
            //_listView.setSelector(new ColorDrawable(Color.BLUE));
            //_gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
            _adapter = new FileAdapter(getActivity());
            
            setupViews();
            NavigationHistory.instance().clear();
            navigateToDir(_currentDirectory != null 
                            ? _currentDirectory 
                            : Environment.getExternalStorageDirectory());
            getActivity().setTitle(R.string.filemgr_title);
            return view;
        }
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupEvents() {
        
        _controlRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              final List<DisplayableFile> tempSelectedFiles 
                          = getSelectedFiles();
              final File currentDir = _currentDirectory;
              final IOperationCallback callback = new IOperationCallback() {
                  @Override
                  public void onFinished(Context context,
                          Object obj) {
                      if(currentDir == _currentDirectory) {
                          navigateToDir(currentDir);
                      }
                  }
              };
                
              FileHelper.deleteFiles(getActivity(), tempSelectedFiles, 
                      callback);
              if(MainActivity._actionMode != null) {
                  ((ActionMode)MainActivity._actionMode).finish();
              }
            }
        });
        
        _controlLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<DisplayableFile> tempSelectedFiles 
                            = getSelectedFiles();
                if(tempSelectedFiles == null) {
                    return;
                }
                
                if(tempSelectedFiles.size() > 0) {
                    FileHelper.sendFiles(getActivity(), tempSelectedFiles);
                }
                
                if(MainActivity._actionMode != null) {
                    try {
                        ((ActionMode)MainActivity._actionMode).finish();
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        _controlCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FtpServerActivity.class);
                if(intent != null) {
                    if(!_isViewMode) {
                        List<DisplayableFile> files = getSelectedFiles();
                        File[] files2 = new File[files.size()];
                        for(int i =0; i < files.size(); ++i) {
                            files2[i] = files.get(i).getFile();
                        }
                        Preferences.setFtpSharedFiles(files2);
                        getActivity().startActivity(intent);
                    } 
                }
                
                if(MainActivity._actionMode != null) {
                    try {
                        ((ActionMode)MainActivity._actionMode).finish();
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                } // endif
            }
        });
    }
    
    public void setListView(boolean isListView) {
        _isListView = isListView;
        setupViews();
    }
    
    private void setViewMode(boolean isViewMode) {
        this._isViewMode = isViewMode;
        if(_isViewMode) {
            _navigatonPane.setVisibility(View.VISIBLE);
            _editPane.setVisibility(View.GONE);
            if(_isListView) {
                _listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            } else {
                _gridView.setChoiceMode(GridView.CHOICE_MODE_NONE);
            }
        } else {
            _editPane.setVisibility(View.VISIBLE);
            _navigatonPane.setVisibility(View.GONE);
            if(_isListView) {
                _listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            } else {
                _gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
            }
        }
    }
    
    private void updateActionModeTitle() {
        if(_isViewMode)
            return;

        int selectCount = 0;
        if(_isListView) {
            selectCount = _listView.getCheckedItemCount();
        } else {
            selectCount = _gridView.getCheckedItemCount();
        }
        
        ActionMode mode = (ActionMode)MainActivity._actionMode;
        if(mode == null) 
            return;
        
        if(selectCount <= 0) {
            mode.finish();
        } else {
            mode.setTitle(selectCount + "/" + _files.size());
            mode.invalidate();
            updateActionModeMenu(mode, mode.getMenu(), selectCount);
        }
    }
    
    private void updateBrowseModeMenu(Menu menu) {
        if(menu == null)
            return;
        
        Activity activity = getActivity();
        if(activity != null) {
            MenuInflater menuInflator = activity.getMenuInflater();
            if(menuInflator != null) {
                menu.clear();
                if(ClipBoard.instance().getFiles() != null 
                        && !ClipBoard.instance().getFiles().isEmpty()) {
                    menuInflator.inflate(R.menu.filemgr_view_paste, menu);
                }
                menuInflator.inflate(R.menu.filemgr_view_common, menu);
            }
        }
    }
    
    private void updateActionModeMenu(ActionMode mode, Menu menu, int selectCount) {
        if(mode == null || menu == null) {
            return;
        }
        
        MenuInflater inflater = mode.getMenuInflater();
        boolean isZip = false;
        menu.clear();
        inflater.inflate(R.menu.filemgr_edit_common, menu);
        if(selectCount == 1) {
            List<DisplayableFile> selectedFiles = getSelectedFiles();
            inflater.inflate(R.menu.filemgr_edit_one_file, menu);
            if(FileHelper.isZipFile(selectedFiles.get(0).getFile())) {
                inflater.inflate(R.menu.filemgr_edit_extract, menu);
                isZip = true;
            }
        }
        if(!isZip) {
            inflater.inflate(R.menu.filemgr_edit_compress, menu);
        }
    }
    
    private List<DisplayableFile> getSelectedFiles() {
        List<DisplayableFile> files = new ArrayList<DisplayableFile>();
        SparseBooleanArray array;
        if(_isListView) {
            array = _listView.getCheckedItemPositions();
        } else {
            array = _gridView.getCheckedItemPositions();
        }
        
        if(array != null) {
            for(int i = 0; i < _files.size(); ++i) {
                if(array.get(i)) {
                    files.add(_files.get(i));
                }
            }
        }
        
        return files;
    }
    
    private void setupViews() {
        
        AdapterView.OnItemClickListener itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if(_isViewMode) {
                    try {
                        DisplayableFile displayableFile = (DisplayableFile)_files.get(arg2);
                        
                        if(displayableFile != null) {
                            if( displayableFile instanceof DisplayableFolder) {
                                navigateToDir(displayableFile.getFile());
                            } else {
                                FileHelper.openFile(displayableFile.getFile(), getActivity());
                            }
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //Select Item
                    updateActionModeTitle();
                }
            }
        };
        
        AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (MainActivity._actionMode != null) {
                    return false;
                }

                setViewMode(false);
                if(_isListView) {
                    _listView.setItemChecked(arg2, true);
                } else {
                    _gridView.setItemChecked(arg2, true);
                }
                
                // start the CAB using the ActionMode.Callback defined above
                MainActivity._actionMode = getActivity().startActionMode(new ActionMode.Callback() {
                                
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater inflater = mode.getMenuInflater();
                        if(getSelectedFiles().size() > 2) {
                            inflater.inflate(R.menu.filemgr_edit_common, menu);
                        } else {
                            inflater.inflate(R.menu.filemgr_edit_extract, menu);
                        }
                        //mode.
                        return true;
                    }
                    
                    // may be called multiple times if the mode is invalidated.
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                      
                      return false; // Return false if nothing is done
                    }

                    // called when the user selects a contextual menu item
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        final File currentDir = _currentDirectory;
                        final IOperationCallback callback = new IOperationCallback() {
                            @Override
                            public void onFinished(Context context,
                                    Object obj) {
                                if(currentDir == _currentDirectory) {
                                    navigateToDir(currentDir);
                                }
                            }
                        };
                        final List<DisplayableFile> tempSelectedFiles = getSelectedFiles();
                        switch (item.getItemId()) {
                        case R.id.action_share:
                          FileHelper.sendFiles(getActivity(), tempSelectedFiles);
                          break;
                        case R.id.action_delete:
                          FileHelper.deleteFiles(getActivity(), tempSelectedFiles, 
                                  callback);
                          break;
                        case R.id.action_rename:
                          if(tempSelectedFiles.size() != 1)
                              break;
                          FileHelper.renameFile(getActivity(), tempSelectedFiles.get(0), 
                                  callback);
                          break;
                        case R.id.action_compress:
                          if(tempSelectedFiles.size() <= 0)
                              break;
                          FileHelper.zipFiles(tempSelectedFiles, getActivity(), callback);
                          break;
                        case R.id.action_extract:
                          if(tempSelectedFiles.size() <= 0)
                              break;
                          FileHelper.extractZipFile(tempSelectedFiles, getActivity(), callback);
                          break;
                        case R.id.action_copy:
                          ClipBoard.instance().setFiles(tempSelectedFiles, false);
                          break;
                        case R.id.action_cut:
                          ClipBoard.instance().setFiles(tempSelectedFiles, true);
                          break;
                        case R.id.action_create_shortcut:
                          FileHelper.createShortcut(tempSelectedFiles, getActivity());
                          break;
                        case R.id.action_property:
                          FileHelper.showDetails(getActivity(), tempSelectedFiles.get(0));
                          break;
                        case R.id.action_add_bookmark:
                          FileHelper.addBookMarks(getActivity(), tempSelectedFiles);
                        default:
                          break;
                        }
                      
                        mode.finish();
                        if(_mainMenu != null) {
                          updateBrowseModeMenu(_mainMenu);
                        }
                        return false;
                    }

                    // called when the user exits the action mode
                    public void onDestroyActionMode(ActionMode mode) {
                        MainActivity._actionMode = null;
                        setViewMode(true);
                        _adapter.notifyDataSetChanged();
                                
                        if(_isListView) {
                            _listView.clearChoices();
                        } else {
                            _gridView.clearChoices();
                        }
                    }
                });
                updateActionModeTitle();
                return true;
            }
        };

        AbsListView.OnScrollListener onScrollListener = 
                new AbsListView.OnScrollListener() {  
            
            @Override  
            public void onScrollStateChanged(AbsListView view, int scrollState) {  
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        int firstp = view.getFirstVisiblePosition();
                        int lastp = view.getLastVisiblePosition();
                        _adapter.setScrolling(false, firstp, lastp);
                        break;  
                        
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:  
                        _adapter.setScrolling(true, 0, 0);  
                        break;  
          
                    default:  
                        break;  
                } 
            }
            
            @Override
            public void onScroll(AbsListView arg0, int arg1
                    , int arg2, int arg3) {
            }  
            
        };
        
        if(_isListView && _listView != null) {
            _gridView.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _listView.setAdapter(_adapter);
            _listView.setItemsCanFocus(false);
            _listView.setOnItemClickListener(itemClickListener);
            _listView.setOnItemLongClickListener(itemLongClickListener);
            _listView.setOnScrollListener(onScrollListener);
            _gridView.setAdapter(null);
        } else {
            _listView.setVisibility(View.GONE);
            _gridView.setVisibility(View.VISIBLE);
            _gridView.setAdapter(_adapter);
            _gridView.setOnItemClickListener(itemClickListener);
            _gridView.setOnItemLongClickListener(itemLongClickListener);
            _gridView.setOnScrollListener(onScrollListener);
            _listView.setAdapter(null);
        }
        
        onScrollIdle();
    }
    

    private void onScrollIdle() {
        final AbsListView currentView = _isListView ? _listView : _gridView;
        
        currentView.postDelayed(new Runnable() {
            public void run() {
                
                if(currentView != null) {
                    _adapter.setScrolling(false, currentView.getFirstVisiblePosition()
                            , currentView.getLastVisiblePosition());
                }
            }
        }, 500);
    }
    
    public void navigateToDir(final File file) {
        navigateToDir(file, true);
    }
    
    public void navigateToDir(final File file, boolean enqueue) {
        if(MainActivity._actionMode != null) {
            try {
                ((ActionMode)MainActivity._actionMode).finish();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            MainActivity._actionMode = null;
        }
        
        if(enqueue) {
            if(_currentDirectory != null) {
                if(!_currentDirectory.equals(NavigationHistory.instance().top())) {
                   NavigationHistory.instance().push(_currentDirectory); 
                }
            }
        }
        
        new ProgressableTask<Void, Void, Void>(getActivity()) {
            List<DisplayableFile> tempFiles = new ArrayList<DisplayableFile>();
            
            @Override
            protected Void doInBackground(Void... arg0) {
                if(file.isDirectory()) {
                    /*if(!FileHelper.isRootFolder(file)) {
                        tempFiles.add(DisplayableFile.createFile(file.getParentFile()
                                , DisplayableFile.VIRTUAL_FOLDER));
                    }*/
                    _currentDirectory = file;
                    
                    File[] files = file.listFiles(new java.io.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if(_isShowHideFiles)
                                return true;
                            return !file.isHidden();
                        }
                    });
                    
                    if(files != null) {
                        FileHelper.sortFiles(files);
                        for(File item : files) {
                            tempFiles.add(DisplayableFile.createFile(item));
                        }
                    }
                }
                
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                listFiles(tempFiles);
                
                ViewHelper.buildNavigationPane(getActivity(), _navigatonPane, 
                        file, new ViewHelper.IViewCallback() {
                    @Override
                    public void onClick(Context context, File file2) {
                        navigateToDir(file2);
                        
                    }
                });
            }
            
        }.execute();
    }
    
    public void listFiles(final List<DisplayableFile> files) {
        try {
            if(files != null) {
                for(DisplayableFile file : _files) {
                    file.dispose();
                }
                _files.clear();
                _files.addAll(files);
                _adapter.notifyDataSetChanged();
                this.onScrollIdle();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        _mainMenu = menu;
        updateBrowseModeMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_view_switch:
            setListView(!_isListView);
            break;
            
        case R.id.action_paste:
            IOperationCallback callback = new IOperationCallback() {
                @Override
                public void onFinished(Context context, Object obj) {
                    navigateToDir(_currentDirectory);
                    updateBrowseModeMenu(_mainMenu);
                }
            };
            
            if(_currentDirectory != null) {
                if(ClipBoard.instance().isCut()) {
                    FileHelper.performCutFiles(_currentDirectory, getActivity(), callback);
                } else {
                    FileHelper.performCopyFiles(_currentDirectory, getActivity(), callback);
                }
            }
            break;
            
        case R.id.action_new_folder:
            FileHelper.newFolder(this._currentDirectory, getActivity(), new IOperationCallback() {
                @Override
                public void onFinished(Context context, Object obj) {
                    navigateToDir(_currentDirectory);
                }
            });
            break;
        case R.id.action_refresh:
            navigateToDir(_currentDirectory);
            break;
        case R.id.action_recommand:
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public class FileAdapter extends BaseAdapter {
        
        private boolean _isScrolling = false;
        public void setScrolling(boolean isScrolling, int first, int last) {
            
            this._isScrolling = isScrolling;
            if(!_isScrolling) {
                try {
                    if(first >= 0) {
                        for(int i = first; i <= last; ++i) {
                            try {
                                MediaExtractor.instance().enqueue(_files.get(i));
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                MediaExtractor.instance().internalPause();
            }
        }

        private LayoutInflater _inflator;
        
        private Context _context;
        
        public FileAdapter(final Context context) {
            _context = context;
            _inflator = LayoutInflater.from(getActivity());
            
            MediaExtractor.instance().setCallback(new ICallback() {

                @Override
                public void onFinished(final Object obj) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run()  {
                            FileAdapter.this.notifyDataSetChanged();
                        }
                    });
                    //
                }
            });
        }
        
        @Override
        public int getCount() {
            return _files.size();
        }

        @Override
        public Object getItem(int arg0) {
            return _files.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }
        
        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            if(_inflator == null) {
                _inflator = LayoutInflater.from(getActivity());
            }
            
            ViewHolder holder;
            if(arg1 == null) {
                holder = new ViewHolder();
                arg1 = _inflator.inflate(_isListView 
                        ? R.layout.filemgr_list_item
                        : R.layout.filemgr_grid_item, null);
                CheckableLayout decorator = new CheckableLayout(_context);
                decorator.addView(arg1);
                arg1 = decorator;
                
                holder.image = (ImageView)arg1.findViewById(R.id.icon);
                holder.text = (TextView)arg1.findViewById(R.id.text);
                
                if(_isListView) {
                    holder.info = (TextView)arg1.findViewById(R.id.info);
                    holder.info2 = (TextView)arg1.findViewById(R.id.info2);
                }
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder)arg1.getTag();
            }
            
            DisplayableFile file = (DisplayableFile)getItem(arg0);
            if(holder != null) {
                /*if(!_isScrolling ) {
                    file.extractDrawable(getActivity());
                }*/
                holder.image.setImageDrawable(file.getIconDrawable(_context));
                holder.text.setText(file.getName());
                
                if(_isListView) {
                    holder.info.setText(file.getLastModifiedTime());
                    holder.info2.setText(file.getSize());
                }
            }
            
            arg1.setBackgroundColor(file.isChecked() 
                    ? Color.BLUE : Color.TRANSPARENT);
            return arg1;
        }
     
        class ViewHolder {
            public TextView info;
            public TextView info2;
            public TextView text;
            public ImageView image;
        }
    }
}
