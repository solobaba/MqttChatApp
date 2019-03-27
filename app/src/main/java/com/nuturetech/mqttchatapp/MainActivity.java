package com.nuturetech.mqttchatapp;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nuturetech.mqttchatapp.Adapter.CustomAdapter;
import com.nuturetech.mqttchatapp.model.ChatModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    ListView listView;
    List<ChatModel> list_chat = new ArrayList<>();

    // connection default values
    private static final String BROKER_URI = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC = "mqttdemo";
    private static final int QOS = 2;

    // user name for the chat
    private static final String USER_NAME = Build.DEVICE;

    // global types
    private MqttAndroidClient client;
    private EditText textMessage;
    private TextView textConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_of_message);

        // get text elements to re-use them
        textMessage = findViewById(R.id.message);

        // when the activity is created call to connect to the broker
        connect();
    }

    private void connect(){
        // create a new MqttAndroidClient using the current context
        client = new MqttAndroidClient(this, BROKER_URI, USER_NAME);
        client.setCallback(this); // set this as callback to listen for messages

        try{
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // clean session in order to don't get duplicate messages each time we connect

            client.connect(options, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(MainActivity.this, "Ready for chat", Toast.LENGTH_SHORT).show();
                    // once connected call to subscribe to receive messages
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(MainActivity.this, "Unavailable chat, cause: " +
                            throwable.getMessage(), Toast.LENGTH_LONG).show();
                }

            });
        } catch (MqttException e){
            Toast.makeText(this, "ERROR, client not connected to broker in " +
                    BROKER_URI, Toast.LENGTH_LONG).show();
        }
    }

    public void publish(View view) {
        // we are in the right view?
        if (view.getId() == R.id.btnSend) {
            // we only publish if connected
            if (null != client && client.isConnected()) {

                String message = textMessage.getText().toString();

                //user send message
                ChatModel chatModel = new ChatModel(message, true);
                list_chat.add(chatModel);

                CustomAdapter adapter = new CustomAdapter(list_chat, getApplicationContext());
                listView.setAdapter(adapter);

                // we only publish if there is message to publish
                if (!message.isEmpty()) {

                    message = "<b>" + USER_NAME + "</b>: " + message + "<br/>";
                    textMessage.setText("");

                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(QOS);
                    try {
                        client.publish(TOPIC, mqttMessage, null, new IMqttActionListener() {

                            @Override
                            public void onSuccess(IMqttToken iMqttToken) {
                                Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                Toast.makeText(MainActivity.this, "Failed on publish, cause: " +
                                        throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        });
                    } catch (MqttException e) {
                        Toast.makeText(this, " ERROR, an error occurs when publishing", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "WARNING, client not connected", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void subscribe() {
        try {
            client.subscribe(TOPIC, QOS, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(MainActivity.this, "Subscribed to:" + TOPIC, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(MainActivity.this, "Failed on subscribe, cause: " +
                            throwable, Toast.LENGTH_LONG).show();
                }

            });

        } catch (MqttException e) {
            Toast.makeText(this, "ERROR, an error occurs when subscribing", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(this, "Connection lost!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Toast.makeText(this, "message arrived", Toast.LENGTH_SHORT).show();
        //format message in html
        String text = null != textConversation.getTag() ? (String) textConversation.getTag() : "";
        text += message.toString();
        textConversation.setTag(text);
        textConversation.setText(Html.fromHtml(text));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //Toast.makeText(this, "Delivery complete!", Toast.LENGTH_SHORT).show();
    }
}
