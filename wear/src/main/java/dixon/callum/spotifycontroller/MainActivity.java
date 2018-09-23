package dixon.callum.spotifycontroller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.RecognizerIntent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private String nodeId;
    private static final long CONNECTION_TIME_OUT_MS = 10000;
    private String txtSpeechInput;

    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        initGoogleApiClient();
    }

    private void initGoogleApiClient() //initialize the google client api for watch -> phone communication
    {
        mApiClient = getGoogleApiClient(this); //initialize it
        retrieveDeviceNode();
    }

    private GoogleApiClient getGoogleApiClient(Context context) //build a new google client
    {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode() //retrieve the node id of everything connected to the wearable
    {
        new Thread(new Runnable() //run it on a new thread
        {
            @Override
            public void run()
            {
                mApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS); //set timeout limit
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(mApiClient).await(); //wait for connection
                List<Node> nodes = result.getNodes(); //if connection is successful, retrieve nodes


                if (nodes.size() > 0) //if there is at least one node active
                {
                    nodeId = nodes.get(0).getId(); //get its id
                }

                mApiClient.disconnect(); //disconnect from the client
            }
        }).start();
    }

    private void sendToaster(final String MESSAGE) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(mApiClient, nodeId, MESSAGE, null);
                    mApiClient.disconnect();
                }
            }).start();
        }
    }

    public void playButton(View v) {
        sendToaster("play");
    }

    public void pauseButton(View v) {
        sendToaster("pause");
    }

    public void btnSpeak(View v) {
        promptSpeechInput();
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput = (result.get(0));
                    voiceOperation(txtSpeechInput);
                }
                break;
            }
        }
    }

    private void voiceOperation(String txtSpeechInput) {
        if(txtSpeechInput.contains("play")){
            sendToaster("play");
        }
        if(txtSpeechInput.contains("pause")){
            sendToaster("pause");
        }
        if(txtSpeechInput.contains("next")){
            sendToaster("nexttrack");
        }
        if(txtSpeechInput.contains("last")){
            sendToaster("previoustrack");
        }
    }
}
