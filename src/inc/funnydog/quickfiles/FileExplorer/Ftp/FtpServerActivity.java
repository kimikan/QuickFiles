package inc.funnydog.quickfiles.FileExplorer.Ftp;

import java.net.InetAddress;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.inmobi.monetization.IMBanner;
import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.RecommandActivity;
import inc.funnydog.quickfiles.SettingActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FtpServerActivity extends Activity {

    TextView _text;
    Button _control;
    LinearLayout _adLayout;
    AdView adView;
    LinearLayout _inmobiLayout;
    IMBanner _inmobiBanner;
    
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        //setupViews();
        if(adView != null) {
            adView.loadAd(new AdRequest());
        }
        if(_inmobiBanner != null) {
            _inmobiBanner.loadBanner();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ftpServerReceiver);
        stopServer();
    }

    private void startServer() {
        sendBroadcast(new Intent(FtpServerService.ACTION_START_FTPSERVER));
    }

    void setupViews() {
        setContentView(R.layout.activity_ftp);
        
        if(Build.VERSION.SDK_INT >= 14) {
            try {
            //getActionBar().setHomeButtonEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        _text = (TextView)findViewById(R.id.ftp_status);
        _control = (Button)findViewById(R.id.ftp_control);
        _adLayout = (LinearLayout)findViewById(R.id.ad_layout);
        _inmobiLayout = (LinearLayout)findViewById(R.id.inmobi_ad_layout);
        
        _inmobiBanner = new IMBanner(this, "2ef7c9502fac41bcacbaf68dcdc9d01b"
                , IMBanner.INMOBI_AD_UNIT_320X50);
        _inmobiLayout.addView(_inmobiBanner);
        _inmobiBanner.setRefreshInterval(40);
        _inmobiBanner.loadBanner();
        adView = new AdView(this, AdSize.SMART_BANNER, /*"ca-app-pub-5022412144860588/4337971351"*/"ca-app-pub-5022412144860588/5814704557");
        _adLayout.addView(adView);
        adView.loadAd(new AdRequest());
        if (FtpServerService.isRunning()) {
            // Fill in the FTP server address
            InetAddress address = FtpServerService.getLocalInetAddress();
            String iptext = "ftp://" + address.getHostAddress() + ":"
                    + Settings.getPortNumber() + "/";
            
            _text.setText(iptext);
            _control.setText(R.string.ftp_stop);
        } 
        
        _control.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(FtpServerService.isRunning()) {
                    stopServer();
                } else {
                    startServer();
                }
                _control.setEnabled(false);
            }
            
        });
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(FtpServerService.ACTION_STARTED);
        filter.addAction(FtpServerService.ACTION_STOPPED);
        filter.addAction(FtpServerService.ACTION_FAILEDTOSTART);
        registerReceiver(ftpServerReceiver, filter);
    }
    
    
    private void stopServer() {
        sendBroadcast(new Intent(FtpServerService.ACTION_STOP_FTPSERVER));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ftp_setting, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if(item.getItemId() == android.R.id.home) {
            this.finish();
        } else if(item.getItemId() == R.id.action_recommand) {
            //startActivity(new Intent(this, RecommandActivity.class));
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" 
                            + "inc.funnydog.quickfiles")));
        }
        
        return super.onOptionsItemSelected(item);
    }

    BroadcastReceiver ftpServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FtpServerService.ACTION_STARTED)) {
                InetAddress address = FtpServerService.getLocalInetAddress();
                if (address == null) {
                    _text.setText(R.string.ftp_no_wifi);
                    return;
                }
                String iptext = "ftp://" + address.getHostAddress() + ":"
                        + Settings.getPortNumber() + "/";
                _text.setText(iptext);
                _control.setText(R.string.ftp_stop);
            } else if (intent.getAction().equals(FtpServerService.ACTION_STOPPED)) {
                _text.setText(R.string.ftp_was_stop);
                _control.setText(R.string.ftp_start);
            } else if (intent.getAction().equals(FtpServerService.ACTION_FAILEDTOSTART)) {
                _text.setText(R.string.ftp_no_wifi);
                _control.setText(R.string.ftp_start);
            }
            _control.setEnabled(true);
        }
    };
}
