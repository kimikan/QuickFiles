package inc.funnydog.quickfiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity 
        implements OnPreferenceChangeListener, OnPreferenceClickListener {
    
    private static SharedPreferences getSharedPreferences() {
        final Context context = MyMobileApplication.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static final String KEY_VIEW_MODE = "ListViewShow";
    public static final String KEY_SHOW_HIDE_FILE = "ShowHide";
    public static final String KEY_USERNAME = "UserName";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_PORT = "Port";
    
    CheckBoxPreference prefViewMode;
    CheckBoxPreference prefShowHide;
    EditTextPreference prefUsername;
    EditTextPreference prefPassword;
    EditTextPreference prefPort;
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
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
        this.addPreferencesFromResource(R.xml.activity_setting);
        prefViewMode = (CheckBoxPreference) findPreference("key_view_mode");
        prefShowHide = (CheckBoxPreference) findPreference("key_show_hide");
        prefUsername= (EditTextPreference) findPreference("key_username");
        prefPassword = (EditTextPreference) findPreference("key_password");
        prefPort = (EditTextPreference) findPreference("key_port");
        prefViewMode.setOnPreferenceChangeListener(this);
        prefViewMode.setOnPreferenceClickListener(this);
        prefShowHide.setOnPreferenceChangeListener(this);
        prefShowHide.setOnPreferenceClickListener(this);
        
        prefUsername.setOnPreferenceChangeListener(this);
        prefPassword.setOnPreferenceChangeListener(this);
        prefPort.setOnPreferenceChangeListener(this);
        
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        boolean bMode = sp.getBoolean(KEY_VIEW_MODE, false);
        prefViewMode.setChecked(bMode);
        
        boolean bShow = sp.getBoolean(KEY_SHOW_HIDE_FILE, false);
        prefShowHide.setChecked(bShow);
        String userName = sp.getString(KEY_USERNAME, "ftp");
        prefUsername.setSummary(userName);
        String password = sp.getString(KEY_PASSWORD, "ftp");
        prefPassword.setSummary(password);
        int port = sp.getInt(KEY_PORT, 8888);
        prefPort.setSummary(port + "");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public static boolean isListView() {
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        return sp.getBoolean(KEY_VIEW_MODE, false);
    }

    public static boolean showHideFiles() {
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        return sp.getBoolean(KEY_SHOW_HIDE_FILE, false);
    }
    
    public static String getUsername() {
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        return sp.getString(KEY_USERNAME, "ftp");
    }
    
    public static String getPassword() {
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        return sp.getString(KEY_PASSWORD, "ftp");
    }
    
    public static int getPort() {
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        return sp.getInt(KEY_PORT, 8888);
    }
    
    @Override
    public boolean onPreferenceClick(Preference arg0) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, 
            Object newValue) {
        android.util.Log.i("xxx", newValue.toString());
        
        SharedPreferences sp = SettingActivity.getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        if(preference.equals(prefViewMode)) {
            editor.putBoolean(KEY_VIEW_MODE, (Boolean)newValue);
            prefViewMode.setChecked((Boolean)newValue);
        } else if (preference.equals(prefShowHide)) {
            editor.putBoolean(KEY_SHOW_HIDE_FILE, (Boolean)newValue);
            prefShowHide.setChecked((Boolean)newValue);
        } else if(preference.equals(prefUsername)) {
            String userName = newValue.toString();
            if(userName == null || userName.length() == 0) {
                Toast.makeText(this, R.string.setting_anonymouse_tip, 
                        Toast.LENGTH_SHORT).show();
                userName = "ftp";
            }
            editor.putString(KEY_USERNAME, userName);
            prefUsername.setSummary(userName);
        } else if(preference.equals(prefPassword)) {
            editor.putString(KEY_PASSWORD, newValue.toString());
            prefPassword.setSummary(newValue.toString());
        } else if(preference.equals(prefPort)) {
            int port = 0;
            try {
                
                port = Integer.parseInt(newValue.toString());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            if(port < 1000 || port > 9999) {
                Toast.makeText(this, R.string.setting_port_wrong, 
                        Toast.LENGTH_SHORT).show();
                port = 8888;
            }
            editor.putInt(KEY_PORT, port);
            prefPort.setSummary(port + "");
        }
        editor.commit();
        
        return true;
    }

}
