package inc.funnydog.quickfiles;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import inc.funnydog.quickfiles.FileExplorer.CheckableLayout;
import inc.funnydog.quickfiles.FileExplorer.DisplayableFile;

public abstract class FragmentViewBase extends Fragment {
    protected List<DisplayableFile> _files = new ArrayList<DisplayableFile>();
    
    protected FileAdapter _adapter;
    
    protected boolean _isListView = SettingActivity.isListView();
    
    protected ListView _listView;
    
    protected GridView _gridView;
    
    protected boolean _isViewMode = true;
    
    protected LinearLayout _navigatonPane;
    
    protected LinearLayout _editPane;
    
    protected Menu _mainMenu;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        
        if(inflater != null) {
            View view = inflater.inflate(getLayoutRes(), null);
            _listView = (ListView)view.findViewById(R.id.file_list);
            _gridView = (GridView)view.findViewById(R.id.file_grid);
            _navigatonPane = (LinearLayout)view.findViewById(R.id.navigation_pane);
            _editPane = (LinearLayout)view.findViewById(R.id.edit_pane);
            _adapter = new FileAdapter(getActivity());
            
            setupViews();
            return view;
        }
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setListView(boolean isListView) {
        _isListView = isListView;
        setupViews();
    }
    
    protected void setViewMode(boolean isViewMode) {
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
    
    protected int getSelectedCount() {
        if(_isListView) {
            return _listView.getCheckedItemCount();
        } else {
            return _gridView.getCheckedItemCount();
        }
    }
    
    private void updateActionModeTitle() {
        if(_isViewMode)
            return;

        int selectCount = getSelectedCount();
        
        ActionMode mode = (ActionMode)MainActivity._actionMode;
        if(mode == null) return;
        if(selectCount <= 0) {
            mode.finish();
        } else {
            mode.setTitle(selectCount + "/" + _files.size());
            mode.invalidate();
            updateActionModeMenu(mode, mode.getMenu(), selectCount);
        }
    }
    
    protected abstract void onUpdateModeMenu(MenuInflater menuInflator, Menu menu, boolean isViewMode);
    
    protected abstract void onItemClick(AdapterView<?> arg0, View arg1,
            int arg2, long arg3, boolean isViewMode);
    
    protected abstract void onMenuItemClicked(ActionMode mode, MenuItem item);
    
    protected abstract int getLayoutRes();
    
    protected void updateBrowseModeMenu(Menu menu) {
        if(menu == null)
            return;
        
        Activity activity = getActivity();
        if(activity != null) {
            MenuInflater menuInflator = activity.getMenuInflater();
            if(menuInflator != null) {
                onUpdateModeMenu(menuInflator, menu, true);
            }
        }
    }
    
    private void updateActionModeMenu(ActionMode mode, Menu menu, int selectCount) {
        if(mode == null || menu == null) 
            return;
        
        MenuInflater inflater = mode.getMenuInflater();
        onUpdateModeMenu(inflater, menu, false);
    }
    
    protected List<DisplayableFile> getSelectedFiles() {
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
                
                if(!_isViewMode) {
                    //Select Item
                    updateActionModeTitle();
                }
                FragmentViewBase.this.onItemClick(arg0, arg1, arg2, arg3, _isViewMode);
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
                        return true;
                    }
                    
                    // may be called multiple times if the mode is invalidated.
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                      
                      return false; // Return false if nothing is done
                    }

                    // called when the user selects a contextual menu item
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                      onMenuItemClicked(mode, item);
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

        if(_isListView && _listView != null) {
            _gridView.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _listView.setAdapter(_adapter);
            _listView.setItemsCanFocus(false);
            _listView.setOnItemClickListener(itemClickListener);
            _listView.setOnItemLongClickListener(itemLongClickListener);
            _gridView.setAdapter(null);
        } else {
            _listView.setVisibility(View.GONE);
            _gridView.setVisibility(View.VISIBLE);
            _gridView.setAdapter(_adapter);
            _gridView.setOnItemClickListener(itemClickListener);
            _gridView.setOnItemLongClickListener(itemLongClickListener);
            _listView.setAdapter(null);
        }
    }
    
    public void listFiles(List<DisplayableFile> files) {
        try {
            if(files != null) {
                _files.clear();
                _files.addAll(files);
                _adapter.notifyDataSetChanged();
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
        return super.onOptionsItemSelected(item);
    }

    public class FileAdapter extends BaseAdapter {
        
        private LayoutInflater _inflator;
        
        private Context _context;
        
        public FileAdapter(Context context) {
            _context = context;
            _inflator = LayoutInflater.from(getActivity());
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
