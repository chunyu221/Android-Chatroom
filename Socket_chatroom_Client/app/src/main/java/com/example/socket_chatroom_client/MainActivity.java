package com.example.socket_chatroom_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText input_name;
    EditText input_IP;
    EditText input_port;
    Button bt_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewElement();
        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", input_name.getText().toString());
                bundle.putString("IP", input_IP.getText().toString());
                bundle.putString("port", input_port.getText().toString());

                Intent it = new Intent();
                it.putExtras(bundle);
                it.setClass(MainActivity.this, MainActivity2.class);
                startActivity(it);
            }
        });
    }

    private void initViewElement() {
        bt_connect = (Button) findViewById(R.id.bt_connect);
        input_name = (EditText) findViewById(R.id.input_name);
        input_IP = (EditText) findViewById(R.id.input_IP);
        input_port = (EditText) findViewById(R.id.input_port);
    }
}