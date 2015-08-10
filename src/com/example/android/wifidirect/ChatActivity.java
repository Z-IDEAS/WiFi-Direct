package com.example.android.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class ChatActivity extends Activity {
    static boolean onlinetag ;
    private WifiP2pInfo info;
    private EditText ChatEdit;
    private   TextView ChatText;
    public static Handler handler1,handler2,handler3;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        intentFilter = new IntentFilter();
        broadcastReceiver=new SwichBroadcastReceiver();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
        ChatEdit = (EditText) findViewById(R.id.ChatEdit);
        ChatText = (TextView) findViewById(R.id.ChatText);
        handler1 = new myHandler();
        handler2 = new myHandler();
        handler3 = new myHandler();
        boolean a = getIntent().getBooleanExtra("init",false);
        info =(WifiP2pInfo) getIntent().getParcelableExtra("info");
                if (getIntent().getBooleanExtra("init",false)){
                    onlineconnect();
                    if(!info.isGroupOwner){
                    clientInit();
                }}
        onlinetag=false;
        findViewById(R.id.ChatSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onlinetag){
                try {
                    ChatText.append(LoginActivity.name+":"+ChatEdit.getText().toString() + "\n");
                    DeviceDetailFragment.writer.write(LoginActivity.name+":"+ChatEdit.getText().toString() + "\n");
                    DeviceDetailFragment.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
                else {
                    try {
                        ChatText.append(LoginActivity.name+":"+ChatEdit.getText().toString() + "\n");
                        olwriter.write(LoginActivity.name+":"+ChatEdit.getText().toString() + "\n");
                        olwriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ChatEdit.setText("");
            }
        });
    }
//    public void onPause() {
//        super.onPause();
//        unregisterReceiver(broadcastReceiver);
//    }
    Socket socket = null;
    public void clientInit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = null;
                try {
                    socket = new Socket(info.groupOwnerAddress.getHostAddress(),12345);
                    DeviceDetailFragment.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    DeviceDetailFragment.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }catch (IOException e) {
                    e.printStackTrace();
                }
                try{
                    while ((line = DeviceDetailFragment.reader.readLine())!=null){
                        Message msg = new Message();
                        msg.obj = line;
                        handler2.sendMessage(msg);
                    }
                    DeviceDetailFragment.reader.close();

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Socket online = null;
    BufferedReader olreader = null;
    BufferedWriter olwriter = null;
        public void onlineconnect(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = null;
                    try {
                        online = new Socket("192.168.1.104",10000);
                        olwriter = new BufferedWriter(new OutputStreamWriter(online.getOutputStream()));
                        olreader = new BufferedReader(new InputStreamReader(online.getInputStream()));
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    try{
                        while ((line = olreader.readLine())!=null){
                            Message msg = new Message();
                            msg.obj = line;
                            handler3.sendMessage(msg);
                        }
                        olreader.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class myHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String s = (String) msg.obj;
            ChatText.append(s + "\n");
        }
    }
}
