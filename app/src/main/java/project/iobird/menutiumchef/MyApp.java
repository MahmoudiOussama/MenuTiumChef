package project.iobird.menutiumchef;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by ioBirdOussama on 24/05/2017.
 */

public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/roboto_light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
