package com.example.android.nepalfishkeepers;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hypertrack.lib.HyperTrack;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    boolean isOpen = false;
    private RecyclerView mNfkList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FloatingActionButton fab;
    private Animation rotateForward, rotateBackward;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createNotificationChannel();

        fab = findViewById(R.id.fab);

        mNfkList = findViewById(R.id.nfk_list);
        mNfkList.setHasFixedSize(true);
        mNfkList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("NFK");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(registerIntent);
//                    MainActivity.this.finish();
                }

            }
        };
        HyperTrack.initialize(this.getApplicationContext(), "pk_7f056ad4bb48896ed2513162043cde5e337fa62f");
    }

    private void checkForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }
        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
        }
        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here i.e User Login in this case
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, EnableLocationActivity.class);
                startActivity(intent);
//                MainActivity.this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, EnableLocationActivity.class);
                startActivity(intent);
//                MainActivity.this.finish();
            }
        }
    }

    public void fabBtnClicked(View view) {
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#008080")));
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);
        animateFab();
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(intent);
    }

    private void animateFab() {
        if (isOpen) {
            fab.startAnimation(rotateBackward);
            isOpen = false;
        } else {
            fab.startAnimation(rotateForward);
            isOpen = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkForLocationSettings();
    }

    public void onBackPressed() {

        android.os.Process.killProcess(android.os.Process.myPid());
        // This above line close correctly
    }

    private void initialize() {
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Nfk, NfkViewHolder> FBRA = new FirebaseRecyclerAdapter<Nfk, NfkViewHolder>(
                Nfk.class,
                R.layout.nfk_row,
                NfkViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(NfkViewHolder viewHolder, Nfk model, int position) {

                final String post_key = getRef(position).getKey().toString();
                final String post_uid = model.getUid();

                viewHolder.setUserImage(getApplicationContext(), model.getUserimage());
                viewHolder.setPostUsername(model.getUsername());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setUid(post_uid);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleInstaActivity = new Intent(MainActivity.this, SingleNfkActivity.class);
                        singleInstaActivity.putExtra("postId", post_key);
                        singleInstaActivity.putExtra("postOwner", post_uid);
                        startActivity(singleInstaActivity);
//                        MainActivity.this.finish();
                    }
                });
            }
        };
        mNfkList.setAdapter(FBRA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.logout) {
            mAuth.signOut();

        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * This view holder contains the recycler view to set the value to the view.
     * */
    public static class NfkViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public NfkViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.textTitle);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = mView.findViewById(R.id.textDescription);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx, String image) {
            ImageView post_image = mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(image).into(post_image);
        }

        public void setCategory(String category) {
            TextView post_category = mView.findViewById(R.id.textCategory);
            post_category.setText(category);
        }

        public void setUserImage(Context ctx, String image) {
            Log.d("image", image);
            ImageView userImage = mView.findViewById(R.id.imageView);
            Picasso.with(ctx).load(image).into(userImage);
        }

        public void setPostUsername(String username) {
            TextView postUsername = mView.findViewById(R.id.textUsername);
            postUsername.setText(username);
        }

        public void setUid(String uid) {
            TextView postUid = mView.findViewById(R.id.textUid);
            postUid.setText(uid);
        }

    }

    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100,200,300,400,300,200,400});

            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


}
