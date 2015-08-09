package com.example.android.wifidirect;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
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

    private WifiP2pInfo info;
    private EditText ChatEdit;
    private   TextView ChatText;
    public static Handler handler1,handler2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        handler1 = new  Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = (String) msg.obj;
                ChatText.append(s+"\n");
            }
        };
        handler2 = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String s = (String) msg.obj;
                ChatText.append(s+"\n");
            }
        };
        ChatEdit = (EditText) findViewById(R.id.ChatEdit);
        ChatText = (TextView) findViewById(R.id.ChatText);
        boolean a = getIntent().getBooleanExtra("init",false);
        info =(WifiP2pInfo) getIntent().getParcelableExtra("info");
            if(!info.isGroupOwner)
                if (getIntent().getBooleanExtra("init",false)) {
                    clientInit();
                }
        findViewById(R.id.ChatSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DeviceDetailFragment.writer.write(ChatEdit.getText().toString() + "\n");
                    DeviceDetailFragment.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ChatEdit.setText("");
            }
        });
    }

    public void clientInit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = null;
                Socket socket = null;

                try {
                    socket = new Socket(info.groupOwnerAddress.getHostAddress(),8988);
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

//        AsyncTask<Void,String,Void> read = new AsyncTask<Void, String, Void>() {
//            @Override
//            protected void onProgressUpdate(String... values) {
//                super.onProgressUpdate(values);
//                    ChatText.append(values[0]+"\n");
//            }
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                String line = null;
//                Socket socket = null;
//
//                try {
//                    socket = new Socket(info.groupOwnerAddress.getHostAddress(),8988);
//                    DeviceDetailFragment.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    DeviceDetailFragment.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try{
//                    while ((line = DeviceDetailFragment.reader.readLine())!=null){
//                        publishProgress(line);
//                    }
//                    DeviceDetailFragment.reader.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        };
//        read.execute();

    }
//    public void serverInit(){
//
//        AsyncTask<Void,String,Void> server = new  AsyncTask<Void,String,Void>(){
//            @Override
//            protected void onProgressUpdate(String... values) {
//                super.onProgressUpdate(values);
//                ChatText.append(values[0] + "\n");
//            }
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                String line;
//                try {
//                    ServerSocket serverSocket = new ServerSocket(8988);
//                    Socket socket = serverSocket.accept();
//                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try{
//                    while ((line = reader.readLine())!=null){
//                        publishProgress(line);
//                    }
//                    reader.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute();
//
//    }
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
}
