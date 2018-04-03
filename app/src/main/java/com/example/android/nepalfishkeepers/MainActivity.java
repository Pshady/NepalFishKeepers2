package com.example.android.nepalfishkeepers;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hypertrack.lib.HyperTrack;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    boolean isOpen = false;
    private RecyclerView mInstaList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FloatingActionButton fab;
    private Animation rotateForward, rotateBackward;
    private MaterialSearchView searchView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search_view);


        mInstaList = findViewById(R.id.insta_list);
        mInstaList.setHasFixedSize(true);
        mInstaList.setLayoutManager(new LinearLayoutManager(this));
        mDatabase = FirebaseDatabase.getInstance().getReference().child("NFK");


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(registerIntent);
                }

            }
        };
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
        }
        else {
            fab.startAnimation(rotateForward);
            isOpen = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Insta, InstaViewHolder> FBRA = new FirebaseRecyclerAdapter<Insta, InstaViewHolder>(

                Insta.class,
                R.layout.nfk_row,
                InstaViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(InstaViewHolder viewHolder, Insta model, int position) {

                final String post_key = getRef(position).getKey().toString();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUserName(model.getUsername());
                viewHolder.setCategory(model.getCategory());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleInstaActivity = new Intent(MainActivity.this, SingleInstaActivity.class);
                        singleInstaActivity.putExtra("PostId", post_key);
                        startActivity(singleInstaActivity);
                    }
                });
            }
        };
        mInstaList.setAdapter(FBRA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

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
        }
        else if (id == R.id.logout) {
            mAuth.signOut();
            // Stop HyperTrack SDK
            HyperTrack.stopTracking();
        }else if(id == R.id.action_map){
            startActivity(new Intent(this, MapActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

    /*
    * This view holder contains the recycler view to set the value to the view.
    * */
    public static class InstaViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public InstaViewHolder(View itemView) {
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

        public void setUserName(String userName) {
            TextView postUsername = mView.findViewById(R.id.textUsername);
            postUsername.setText(userName);
        }

        public void setCategory(String category) {
            TextView post_category = mView.findViewById(R.id.textCategory);
            post_category.setText(category);
        }

    }


}
