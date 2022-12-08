package com.example.socket_chatroom_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Enumeration;

public class MainActivity2 extends AppCompatActivity {

    // element
    TextView textView;
    TextView view_name;
    EditText message;
    Button bt_send;
    Button bt_leave;

    // parameter
    private String clientName;
    private String serverIP;
    private int serverPort;
    private Thread thread;
//    private Thread send_Thread;
    private Thread recv_Thread;
    private Socket client;
    private BufferedWriter bw;
    private BufferedReader br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initViewElement();
        Intent it = this.getIntent();
        if (it != null) {
            Bundle bundle = it.getExtras();
            if (bundle != null) {
                clientName = bundle.getString("name");
                serverIP = bundle.getString("IP");
                serverPort = Integer.valueOf(bundle.getString("port"));
                view_name.setText(clientName);
            }
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startSocket();
            }
        });
        thread.start();

        bt_send.setOnClickListener(view -> {
            String msg = message.getText().toString();
            if (msg != null && !msg.equals("")) {
                sendMsg(clientName, msg);
                message.setText("");
            }});

        bt_leave.setOnClickListener(view -> {
            String str = clientName + " has leaved...";
            sendMsg("leave", str);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeSocket();
        });
    }

    private void initViewElement() {
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_leave = (Button) findViewById(R.id.bt_leave);
        message = (EditText) findViewById(R.id.message);
        textView = (TextView) findViewById(R.id.textView);
        view_name = (TextView) findViewById(R.id.view_name);
    }

    private void startSocket() {
        try {
            InetAddress _serverIP = InetAddress.getByName(serverIP);
            client = new Socket(_serverIP, serverPort);
            bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));

            sendMsg("welcome", clientName);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(("Hi! " + clientName + "\n"));
                }
            });

            recv_Thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    recvMsg();
                }
            });
            recv_Thread.start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("text", "Socket=" + e.toString());
            finish();
        }
    }

    public void sendMsg(String name, String msg) {

            new Thread(()->{try {
                JSONObject writeObj = new JSONObject();
                writeObj.put("Name", name);
                writeObj.put("MSG", msg);

                Log.d("start","bw.write");
                bw.write(writeObj + "\n");
                bw.flush();
                Log.d("end","bw.flush");
            }
                 catch (Exception e) {
                    e.printStackTrace();
                    Log.e("error", e.toString());
                }
            }).start();
    }

    public void recvMsg() {
        while (client.isConnected()) {
            try {
                String readmsg = br.readLine();
                if (readmsg != null && !readmsg.equals("")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject readObj = new JSONObject(readmsg);
                                String name = readObj.getString("Name");
                                String msg = readObj.getString("MSG");
                                if (name.equals("Leave")) {
                                    textView.append(msg + "\n");
                                }
                                else {
                                    textView.append(name + ": " + msg + "\n");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeSocket() {
        try {
            bw.close();
            br.close();
            client.close();
            backToMain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void backToMain() {
        Intent it = new Intent();
        it.setClass(MainActivity2.this, MainActivity.class);
        startActivity(it);
    }
}