package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.android.nepalfishkeepers.Constants.FCM_SERVER_KEY;

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
    private TextView singlePostPrice;
    private Button deleteButton;
    private Button startSharingButton;
    private Button letsTradeButton;
    private Button trackButton;
    private Button reportButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_nfk);

        post_key = getIntent().getExtras().getString("postKey");
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
        singlePostPrice = findViewById(R.id.singlePrice);

        deleteButton = findViewById(R.id.singleDeleteButton);
        deleteButton.setVisibility(View.GONE);
        letsTradeButton = findViewById(R.id.letsTradeButton);
        letsTradeButton.setVisibility(View.GONE);
        startSharingButton = findViewById(R.id.startSharingButton);
        startSharingButton.setVisibility(View.GONE);
        trackButton = findViewById(R.id.trackButton);
        trackButton.setVisibility(View.GONE);
        reportButton = findViewById(R.id.reportButton);
        reportButton.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        deleteButton.setVisibility(View.GONE);
        letsTradeButton.setVisibility(View.GONE);
        startSharingButton.setVisibility(View.GONE);
        trackButton.setVisibility(View.GONE);
        reportButton.setVisibility(View.GONE);

        //value listener implemented for post listens to the
        mPostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    mPost = dataSnapshot.getValue(Nfk.class);

                    if (mAuth.getCurrentUser().getUid().equals(mPost.getUid())) {
                        if (mPost.tradeCount == 0) {
                            // For post owner
                            deleteButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        reportButton.setVisibility(View.VISIBLE);
                        if(mPost.isReported()) {
                            reportButton.setEnabled(false);
                        }
                        if (mPost.tradeCount == 0) {
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
                    singlePostPrice.setText(String.valueOf(mPost.getPrice()));
                    Picasso.with(SingleNfkActivity.this).load(mPost.getImage()).into(singlePostImage);
                    Picasso.with(SingleNfkActivity.this).load(mPost.getUserimage()).into(singlePostUserImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //value listener implemented for trade listens to the

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
                        // If current user is trading
                        letsTradeButton.setVisibility(View.VISIBLE);
                        reportButton.setVisibility(View.VISIBLE);
                        if(mPost.isReported()) {
                            reportButton.setEnabled(false);
                        }
                        if (trade.getTraderId().equals(mCurrentUser.getUid())) {
                            //person requesting to buy item
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
                                    letsTradeButton.setText("Already traded");
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
                            //third person watching the post
                            letsTradeButton.setVisibility(View.VISIBLE);
                            letsTradeButton.setEnabled(false);
                            switch (tradeStatus) {
                                case TRADED:
                                    letsTradeButton.setText("Already traded");
                                    break;
                                case NOT_SHARED:
                                case WAITING_FOR_APPROVAL:
                                case APPROVED:
                                default:
                                       letsTradeButton.setText("Trade going on");
                                       break;
                            }
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
// to ensure low battery and memory is consumed
        mTradeDatabase.child(post_key).removeEventListener(mTradeListener);
        mPostDatabase.removeEventListener(mPostListener);
    }

    public void deleteButtonClicked(View view) {
        mPostDatabase.removeValue();
        mTradeDatabase.child(post_key).removeValue();
        finish();
    }

    public void reportButtonClicked(View view) {
        mPostDatabase.child("reported").setValue(true);

        Toast.makeText(getApplicationContext(), "Post has been reported.", Toast.LENGTH_SHORT).show();;
    }

    private void sendNotification(final String title, final String body, final String firebaseToken) {
        String url ="https://fcm.googleapis.com/fcm/send";
        VolleyLog.DEBUG = true;

        JSONObject params = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            params.put("to", firebaseToken);
            notification.put("message", body);
            notification.put("title", title);
            notification.put("type", NotificationType.TRADE_REQUEST);
            notification.put("postKey", post_key);
            params.put("data", notification);
        } catch (JSONException e) {
            Log.d("JsonError", e.getMessage());
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Toast.makeText(getApplicationContext(), "Notification sent", Toast.LENGTH_SHORT).show();;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + FCM_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueueSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonRequest);
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

        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.child(mPost.getUid()).getValue(User.class);
                User currentUser = dataSnapshot.child(mCurrentUser.getUid()).getValue(User.class);

                sendNotification("Trade requested",
                        currentUser.getUsername() + " has requested for a trade",
                        user.getFirebasetoken());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
