package com.example.socket_chatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    // element
    TextView textView;
    TextView view_name;
    EditText message;
    Button bt_send;
    Button bt_leave;

    // parameter
    private int serverPort = 8888;
    private ServerSocket serverSocket;
    private ArrayList clients = new ArrayList();
    private int clientsNum = 0;
    private Thread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // initialize element
        initViewElement();
        Intent it = this.getIntent();
        if (it != null) {
            Bundle bundle = it.getExtras();
            if (bundle != null) {
                String inputStr  = bundle.getString("input");
                if (inputStr != null && !inputStr.equals("")){
                    view_name.setText(inputStr);
                }
            }
        }

        // start a server socket
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startSocket();
            }
        });
        thread.start();

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = message.getText().toString();
                if (msg != null && !msg.equals("")){
                    String str = "Server: " + msg + "\n";
                    textView.append(str);
                    boardCast("Server", msg);
                    message.setText("");
                }
            }
        });

        bt_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = "Server has been closed, please leave the chatroom";
                boardCast("Leave", str);
                closeServer();
                backToMain();
            }
        });
    }
// 192.168.162.37
    private void initViewElement() {
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_leave = (Button) findViewById(R.id.bt_leave);
        message = (EditText) findViewById(R.id.message);
        textView = (TextView) findViewById(R.id.textView);
        view_name = (TextView) findViewById(R.id.view_name);
    }

    private void startSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Connect\n");
                }
            });
            
            Thread tClient = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!serverSocket.isClosed()) {
                        waitNewClient();
                    }
                }
            });
            tClient.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitNewClient() {
        try {
            Socket socket = serverSocket.accept();
            clientsNum++;
            addNewClient(socket);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public void addNewClient(Socket socket) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clients.add(socket);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    textView.append("New Connection\n");
                    while (socket.isConnected()) {
                        String readmsg = br.readLine();

                        if(readmsg != null) {
                            new Thread(() -> {
                                JSONObject readObj = null;
                                try {
                                    readObj = new JSONObject(readmsg);

                                    String name = readObj.getString("Name");
                                    String msg = readObj.getString("MSG");

                                    if (name.equals("welcome")) {
                                        String str = "Welcome " + msg + "!";
                                        boardCast("Server", str);
                                    }

                                    else if (name.equals("leave")) {
                                        boardCast("Server", msg);
                                        textView.append(msg + "\n");
                                    }

                                    else {
                                        boardCast(name, msg);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                textView.append((name + ": " + msg + "\n"));
                                            }
                                        });
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }

                    }
                } catch (Exception e) {
                    e.getStackTrace();
                }
                finally {
                    clients.remove(socket);
                    clientsNum--;
                }
            }
        });
        t.start();
    }

    public void boardCast(String name, String msg) {
        Socket[] clientArr = new Socket[clients.size()];
        clients.toArray(clientArr);
        for (Socket socket : clientArr) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedWriter bw;
                        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        JSONObject writeObj = new JSONObject();
                        writeObj.put("Name", name);
                        writeObj.put("MSG", msg);

                        bw.write(writeObj + "\n");
                        bw.flush();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    private void closeServer() {
        try {
//            out.close();
//            reader.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backToMain() {
        Intent it = new Intent();
        it.setClass(MainActivity2.this, MainActivity.class);
        startActivity(it);
    }
}

