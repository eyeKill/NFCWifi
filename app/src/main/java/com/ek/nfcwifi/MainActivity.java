package com.ek.nfcwifi;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    public static MainActivity myInstance;
    private PendingIntent mPendingIntent;
    private WifiAdmin wifiAdmin;
    private NfcAdmin nfcAdmin;

    private int nfcState = 0;
    //0=write,1=read,>=2=null

    private ViewPager viewPager;
    private SubmitFragment submitFragment;
    private AlertDialog dialog;

    public WifiAdmin getWifiAdmin() {
        return wifiAdmin;
    }

    public NfcAdmin getNfcAdmin() {
        return nfcAdmin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myInstance=this;
        //ui相关
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        final ArrayList<String> titleList = new ArrayList<String>();
        titleList.add("SUBMIT");
        titleList.add("READ");
        titleList.add("WIFI");
        titleList.add("URL");
        final ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(submitFragment = new SubmitFragment());
        fragments.add(new ReadTagFragment());
        fragments.add(new WifiFragment());
        fragments.add(new UrlFragment());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(
                getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 4;
            }
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                nfcState=position;
                if (position == 0) wifiAdmin.openWifi();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setCurrentItem(1);
        //create a dialog to use
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("put your tag...");
        builder.setTitle("Message");
        dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nfcState = 0;
            }
        });

        //nfc相关
        nfcAdmin = new NfcAdmin(this);
        //nfc标签前台抓取相关设置
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        //wifi相关
        wifiAdmin = new WifiAdmin(this);
        wifiAdmin.openWifi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(nfcAdmin!=null) nfcAdmin.nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);//注册前台抓取
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdmin!=null) nfcAdmin.nfcAdapter.disableForegroundDispatch(this);//反注册前台抓取
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(!(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)||
                intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)||
                intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED))) return;
        if (nfcState==0) {
            //写tag
            if (dialog.isShowing()) dialog.dismiss();
            try {
                nfcAdmin.writeTag(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(nfcState==1){
            //读tag
            wifiAdmin.openWifi();
            nfcAdmin.readTag(intent);
            if(nfcAdmin.getRecordList_get().size()>0) {
                Intent startActivityIntent = new Intent();
                startActivityIntent.setClass(MainActivity.this, UserActivity.class);
                startActivity(startActivityIntent);
            } else Toast.makeText(this,"No NDEF Message found.",Toast.LENGTH_SHORT).show();
                    /*while (!wifiAdmin.getmWifiManager().isWifiEnabled()) ;
                    w = nfcAdmin.readTag(intent);
                    wifiAdmin.connectWifi(wifiAdmin.addWifiConf(w.SSID, w.preSharedKey));*/
                    /*final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 0)
                                Toast.makeText(getApplicationContext(), "Failed to connect " + w.SSID, Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getApplicationContext(), "Successfully connected to" + w.SSID, Toast.LENGTH_LONG).show();
                            super.handleMessage(msg);
                        }
                    };*/
            //use a progress dialog
            //TODO debug this
                    /*Toast.makeText(this, "trying to connect " + w.SSID, Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.currentThread().sleep(7000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("7000ms passed.");
                            if (!wifiAdmin.getWifiInfo().getSSID().contains(w.SSID))
                                handler.sendEmptyMessage(0);//failed
                            else handler.sendEmptyMessage(1);//succeed
                        }
                    }).start();*/
         }
    }

    public void setToWrite() {
        nfcState = 0;//ready to write
        dialog.show();
    }

    public SubmitFragment getSubmitFragment() {
        return submitFragment;
    }
}
