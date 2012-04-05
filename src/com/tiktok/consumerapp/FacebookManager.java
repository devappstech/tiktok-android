//-----------------------------------------------------------------------------
// FacebookManager
//-----------------------------------------------------------------------------

package com.tiktok.consumerapp;

//-----------------------------------------------------------------------------
// imports
//-----------------------------------------------------------------------------

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;

//-----------------------------------------------------------------------------
// class implementation
//-----------------------------------------------------------------------------

public final class FacebookManager
{
    //-------------------------------------------------------------------------
    // CompletionHandler
    //-------------------------------------------------------------------------

    public static interface CompletionHandler
    {
        public abstract void onSuccess(Facebook facebook);
        public abstract void onError(Throwable error);
        public abstract void onCancel();
    }

    //-------------------------------------------------------------------------
    // statics
    //-------------------------------------------------------------------------

    private static final String kLogTag = "FacebookManager";

    //-------------------------------------------------------------------------
    // get instance
    //-------------------------------------------------------------------------

    public static FacebookManager getInstance(Context context)
    {
        if (sManager == null) {
            sManager = new FacebookManager(context.getApplicationContext());
        }
        return sManager;
    }

    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    private FacebookManager(Context context)
    {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mFacebook    = new Facebook(Constants.kFacebookApiKey);
    }

    //-------------------------------------------------------------------------

    public void authorize(Activity activity, final CompletionHandler handler)
    {
        String[] permissions = {
            "user_likes",
            "user_birthday",
            "user_checkins",
            "user_interests",
            "user_location",
            "user_relationships",
            "user_relationship_details",
            "offline_access",
            "publish_stream",
        };

        // attempt to log into facebook
        mFacebook.authorize(activity, permissions, new DialogListener() {

            public void onComplete(Bundle values) {
                saveFacebookData();
                if (handler != null) handler.onSuccess(mFacebook);
            }

            public void onFacebookError(FacebookError error) {
                Log.e(kLogTag, String.format("Login failed", error));
                if (handler != null) handler.onError(error);
            }

            public void onError(DialogError error) {
                Log.e(kLogTag, String.format("Login failed", error));
                if (handler != null) handler.onError(error);
            }

            public void onCancel() {
                Log.w(kLogTag, "Login canceled.");
                if (handler != null) handler.onCancel();
            }

        });
    }

    //-------------------------------------------------------------------------

    public boolean logout(Context context)
    {
        boolean result = false;

        try {
            mFacebook.logout(context);
            clearFacebookData();
            result = true;
        } catch (IOException e) {
            Log.e(kLogTag, "Failed logout", e);
        }

        return result;
    }

    //-------------------------------------------------------------------------

    public Facebook facebook()
    {
        return mFacebook;
    }

    //-------------------------------------------------------------------------

    public void loadFacebookData()
    {
        String token = mPreferences.getString("fb_access_token", null);
        long expires = mPreferences.getLong("fb_access_expires", 0);
        if ((token != null) && (expires != 0)) {
            mFacebook.setAccessToken(token);
            mFacebook.setAccessExpires(expires);
        }
    }

    //-------------------------------------------------------------------------

    public void saveFacebookData()
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("fb_access_token", mFacebook.getAccessToken());
        editor.putLong("fb_access_expires", mFacebook.getAccessExpires());
        editor.commit();
    }

    //-------------------------------------------------------------------------

    public void clearFacebookData()
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove("fb_access_token");
        editor.remove("fb_access_expires");
        editor.commit();
    }

    //-------------------------------------------------------------------------
    // fields
    //-------------------------------------------------------------------------

    private static FacebookManager sManager;

    private Facebook          mFacebook;
    private SharedPreferences mPreferences;
}
