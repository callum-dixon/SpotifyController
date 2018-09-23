package dixon.callum.spotifycontroller;


import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ListenerService extends WearableListenerService {

    String nodeId;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Objects.equals(messageEvent.getPath(), "play")){
            MainActivity.playMusic("spotify:track:7EpoeKjtxThtWkkrpVj4fH");
        }
        if (Objects.equals(messageEvent.getPath(), "pause")){
            MainActivity.pauseMusic();
        }
        if (Objects.equals(messageEvent.getPath(), "nexttrack")){
            MainActivity.nextTrack();
        }
        if (Objects.equals(messageEvent.getPath(), "previoustrack")){
            MainActivity.previousTrack();
        }
    }
}
