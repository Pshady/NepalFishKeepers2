package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SingleNfkActivity extends AppCompatActivity {

    private String post_key = null;
    private Nfk mPost;
    private DatabaseReference mDatabase;
    private DatabaseReference mPostDatabase;
    private DatabaseReference mTradeDatabase;
    private ValueEventListener mPostListener;
    private ValueEventListener mTradeListener;
    private ImageView singlePostImage;
    private ImageView singlePostUserImage;
    private TextView singlePostTitle;
    private TextView singlePostDesc;
    private TextView singlePostCategory;
    private TextView singlePostUserName;
    private Button deleteButton;
    private Button startSharingButton;
    private Button letsTradeButton;
    private Button trackButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_nfk);

        post_key = getIntent().getExtras().getString("postId");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("NFK").child(post_key);
        mTradeDatabase = FirebaseDatabase.getInstance().getReference().child("Trades");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        singlePostUserImage = findViewById(R.id.singleUserImage);
        singlePostCategory = findViewById(R.id.singleCategory);
        singlePostDesc = findViewById(R.id.singleDesc);
        singlePostTitle = findViewById(R.id.singleTitle);
        singlePostImage = findViewById(R.id.singleImageView);
        singlePostUserName = findViewById(R.id.singleUserName);

        deleteButton = findViewById(R.id.singleDeleteButton);
        deleteButton.setVisibility(View.GONE);
        letsTradeButton = findViewById(R.id.letsTradeButton);
        letsTradeButton.setVisibility(View.GONE);
        startSharingButton = findViewById(R.id.startSharingButton);
        startSharingButton.setVisibility(View.GONE);
        trackButton = findViewById(R.id.trackButton);
        trackButton.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        deleteButton.setVisibility(View.GONE);
        letsTradeButton.setVisibility(View.GONE);
        startSharingButton.setVisibility(View.GONE);
        trackButton.setVisibility(View.GONE);

        mPostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    mPost = dataSnapshot.getValue(Nfk.class);

                    if (mPost.tradeCount == 0) {
                        // For post owner
                        if (mAuth.getCurrentUser().getUid().equals(mPost.getUid())) {
                            deleteButton.setVisibility(View.VISIBLE);
                        } else {
                            // If current user is trading
                            letsTradeButton.setVisibility(View.VISIBLE);
                            letsTradeButton.setText("Lets trade");
                            letsTradeButton.setEnabled(true);
                        }
                    }

                    singlePostCategory.setText(mPost.getCategory());
                    singlePostTitle.setText(mPost.getTitle());
                    singlePostDesc.setText(mPost.getDesc());
                    singlePostUserName.setText(mPost.getUsername());
                    Picasso.with(SingleNfkActivity.this).load(mPost.getImage()).into(singlePostImage);
                    Picasso.with(SingleNfkActivity.this).load(mPost.getUserimage()).into(singlePostUserImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mPostDatabase.addValueEventListener(mPostListener);

        mTradeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Trade trade = dataSnapshot.getValue(Trade.class);
                    TradeStatus tradeStatus = trade.getTradeStatus();

                    if (mAuth.getCurrentUser().getUid().equals(trade.getTradeOwner())) {
                        // For post owner
                        deleteButton.setVisibility(View.VISIBLE);
                        switch (tradeStatus) {
                            case WAITING_FOR_APPROVAL:
                                // Buyer has asked for trade
                                startSharingButton.setVisibility(View.VISIBLE);
                                break;
                            case APPROVED:
                                // Approved for trade
                                trackButton.setVisibility(View.VISIBLE);
                                break;
                            case TRADED:
                                // Already traded
                                startSharingButton.setVisibility(View.VISIBLE);
                                startSharingButton.setText("Already Traded");
                                startSharingButton.setEnabled(false);
                                break;
                            case NOT_SHARED:
                            default:
                                break;
                        }
                    } else {
                        // For buyers
                        deleteButton.setVisibility(View.GONE);
                        if (trade.getTraderId().equals(mCurrentUser.getUid())) {
                            // If current user is trading
                            letsTradeButton.setVisibility(View.VISIBLE);

                            switch (tradeStatus) {
                                case WAITING_FOR_APPROVAL:
                                    // Already asked for trade
                                    letsTradeButton.setText("Waiting for approval");
                                    letsTradeButton.setEnabled(false);
                                    break;
                                case APPROVED:
                                    // Approved for trade
                                    letsTradeButton.setVisibility(View.GONE);
                                    trackButton.setVisibility(View.VISIBLE);
                                    break;
                                case TRADED:
                                    // Already traded
                                    letsTradeButton.setText("Already Traded");
                                    letsTradeButton.setEnabled(false);
                                    break;
                                case NOT_SHARED:
                                default:
                                    // Not asked for trade
                                    letsTradeButton.setText("Lets trade");
                                    letsTradeButton.setEnabled(true);
                                    break;
                            }
                        } else {
                            letsTradeButton.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mTradeDatabase.child(post_key).addValueEventListener(mTradeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mTradeDatabase.child(post_key).removeEventListener(mTradeListener);
        mPostDatabase.removeEventListener(mPostListener);
    }

    public void deleteButtonClicked(View view) {
        mPostDatabase.removeValue();
        mTradeDatabase.child(post_key).removeValue();
//        Intent mainIntent = new Intent(SingleNfkActivity.this, MainActivity.class);
//        startActivity(mainIntent);
        finish();
    }

    public void letsTradeButtonClicked(View view) {
        final DatabaseReference newTrade = mTradeDatabase.child(post_key);

        // Uploaded new trade to database
        Trade trade = new Trade(mCurrentUser.getUid(),
                TradeStatus.WAITING_FOR_APPROVAL.toString(),
                mPost.getUid(), null);

        newTrade.setValue(trade);

        // Update tradeCount of post
        mPost.tradeCount += 1;
        mPostDatabase.child("tradeCount").setValue(mPost.tradeCount);

//        mTradeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                newTrade.child("traderId").setValue(mCurrentUser.getUid());
//                newTrade.child("tradeStatus").setValue(TradeStatus.WAITING_FOR_APPROVAL);
//                newTrade.child("tradeOwner").setValue(mPost.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//
//                            // Send notification
////                             sendNotifications(titleValue, descValue, categoryValue, newPost.getKey());
//                        }
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        recreate();
    }

    public void startSharingButtonClicked(View view) {
        Intent intent = new Intent(this, TradeActivity.class);
        intent.putExtra("postKey", post_key);
        startActivity(intent);
//        SingleNfkActivity.this.finish();
    }

    public void trackButtonClicked(View view) {
        mTradeDatabase.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trade trade = dataSnapshot.getValue(Trade.class);
                Intent intent = new Intent(SingleNfkActivity.this, TradeActivity.class);
                intent.putExtra("postKey", post_key);
                intent.putExtra("collectionId", trade.getCollectionId());
                startActivity(intent);
//                SingleNfkActivity.this.finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
