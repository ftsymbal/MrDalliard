package com.mera.tsymbal.tvdalliard;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;


public class Dalliard extends Activity {
private RecyclerView recyclerView;
private RecyclerView.Adapter mAdapter;
private RecyclerView.LayoutManager layoutManager;
private static final String TAG = "DalliardActivity";
private static Boolean activated = false;
public static final String NOTIFY_ACTIVITY_ACTION = "notify_activity";
private BroadcastReceiver broadcastReceiver;


@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dalliard);

        String code = getIntent().getStringExtra(getString(R.string.code));
        if ((!TextUtils.isEmpty(code)) && code.equals(getString(R.string.greeting)))
        {
                Activate();
        }

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Resources res = getResources();
        String[] stepsDataset = res.getStringArray(R.array.steps);

        mAdapter = new DalliardAdapter(stepsDataset);
        recyclerView.setAdapter(mAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create channel to show notifications.
                String channelId = getString(R.string.default_notification_channel_id);
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationManager notificationManager =
                        getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
        }

        FirebaseApp.initializeApp(this);

        FirebaseMessaging.getInstance().subscribeToTopic("Greetings")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                                        return;
                                                }

                                                // Get new Instance ID token
                                                String token = task.getResult().getToken();

                                                // Log and toast
                                                String msg = getString(R.string.msg_token_fmt, token);
                                                Log.d(TAG, msg);
                                                Toast.makeText(Dalliard.this, msg, Toast.LENGTH_SHORT).show();

                                        }
                                });
                }
        });

        }
        @Override
        protected void onResume() {
                super.onResume();
                if (activated)
                {
                        Toast.makeText(Dalliard.this, getString(R.string.greeting), Toast.LENGTH_SHORT).show();
                        TextView msgView = findViewById(R.id.textView);
                        msgView.setText(getString(R.string.activated));
                        recyclerView.setVisibility(View.VISIBLE);
                }
        }
        @Override
        protected void onStart() {
                super.onStart();
                broadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals(NOTIFY_ACTIVITY_ACTION ))
                                {
                                        recreate();
                                }
                        }
                };

                IntentFilter filter = new IntentFilter( NOTIFY_ACTIVITY_ACTION );
                registerReceiver(broadcastReceiver, filter);
        }

        @Override
        protected void onStop()
        {
                super.onStop();
                unregisterReceiver(broadcastReceiver);
        }

        public static void Activate()
        {
                activated = true;
        }
}