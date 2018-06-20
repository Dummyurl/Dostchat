package com.dostchat.dost.helpers.Files.backup;

import android.app.Activity;
import android.support.annotation.NonNull;

/**
 * Created by Abderrahim El imame on 10/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public interface Backup {

    void init(@NonNull final Activity activity);

    void start();

    void stop();

    void onError();

}