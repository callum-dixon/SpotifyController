package dixon.callum.spotifycontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;


public class MainActivity extends Activity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "16d910db449d423c981c61a4eb8dac01";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "http://google.com/callback";

    private static Player mPlayer;
    private PlaybackState mCurrentPlaybackState;

    GoogleApiClient mApiClient;

    private TextView mMetadataText;
    private Metadata mMetadata;


    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mMetadataText = (TextView) findViewById(R.id.metaData);

    }

    public void onConnected(Bundle bundle) {
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    private static final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            //logStatus("OK!");
        }

        @Override
        public void onError(Error error) {
            //logStatus("ERROR:" + error);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        // Remember kids, always use the English locale when changing case for non-UI strings!
        // Otherwise you'll end up with mysterious errors when running in the Turkish locale.
        // See: http://java.sys-con.com/node/46241
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        updateView();
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        updateView();
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {

    }

    private void updateView() {

        // Same goes for the playing state
        boolean playing = mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying;


        if (mMetadata != null) {
            findViewById(R.id.imageButton2).setEnabled(mMetadata.nextTrack != null);
            findViewById(R.id.imageButton).setEnabled(mMetadata.prevTrack != null);
            findViewById(R.id.imageButton3).setEnabled(mMetadata.currentTrack != null);
        }

        if (mMetadata != null && mMetadata.currentTrack != null) {
            mMetadataText.setText(mMetadata.contextName + "\n" + mMetadata.currentTrack.name + " \n" + mMetadata.currentTrack.artistName);
    }
        else {
            mMetadataText.setText("<nothing is playing>");

        }
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void onPlayButtonClicked(View v) {
        playMusic("spotify:album:6KiS2t3EapTmHSt9xGUqe7");
    }

    public void onNextButtonClicked(View v) {
        nextTrack();
        }

    public void onPreviousButtonClicked(View v) {
        previousTrack();
    }

    public void onClickPlay(View v) {
        mPlayer.pause(mOperationCallback);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Connection to watch has failed");
    }

    public static void playMusic(String URI) {
        mPlayer.playUri(mOperationCallback, URI, 0, 0);
    }

    public static void pauseMusic() {
        mPlayer.pause(mOperationCallback);
    }

    public static void nextTrack() {
        mPlayer.skipToNext(mOperationCallback);
    }

    public static void previousTrack() {
        mPlayer.skipToPrevious(mOperationCallback);
    }
}

