package scps.nyu.edu.nycrealestate;

import android.app.Application;
import com.parse.Parse;

// this class initializes the parse.com connection when the app is started
public class NYCApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();

        // we only want to initialize the parse.com connection once
        Parse.initialize(
                this,
                getResources().getString(R.string.parse_app_id),
                getResources().getString(R.string.parse_client_key)
        ); // Your Application ID and Client Key are defined elsewhere
    }
}
