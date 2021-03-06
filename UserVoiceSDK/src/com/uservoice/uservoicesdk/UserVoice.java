package com.uservoice.uservoicesdk;

import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.squareup.okhttp.OkHttpClient;
import com.uservoice.uservoicesdk.activity.ContactActivity;
import com.uservoice.uservoicesdk.activity.ForumActivity;
import com.uservoice.uservoicesdk.activity.PortalActivity;
import com.uservoice.uservoicesdk.activity.PostIdeaActivity;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.model.ClientConfig;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.rest.RestTask;
import com.uservoice.uservoicesdk.ui.DefaultCallback;

public class UserVoice {

    public static void launchUserVoice(Context context) {
        context.startActivity(new Intent(context, PortalActivity.class));
    }

    public static void launchForum(Context context) {
        context.startActivity(new Intent(context, ForumActivity.class));
    }

    public static void launchContactUs(Context context) {
        context.startActivity(new Intent(context, ContactActivity.class));
    }

    public static void launchPostIdea(Context context) {
        context.startActivity(new Intent(context, PostIdeaActivity.class));
    }

    public static void init(Config config, Context context) {
        init(config, context, new OkHttpClient());
    }

    public static void init(Config config, Context context, OkHttpClient httpClient) {
        Session.reset();
        Session.getInstance().setContext(context);
        Session.getInstance().setConfig(config);
        Babayaga.init(context);
        RestTask.setHttpClient(httpClient);
    }

    public static void setExternalId(String scope, String id) {
        Session.getInstance().setExternalId(scope, id);
    }

    public static void track(String event, Map<String, Object> properties) {
        Babayaga.track(event, properties);
    }

    public static void track(String event) {
        track(event, null);
    }

    public static String getVersion() {
        return "1.2.2";
    }
}
