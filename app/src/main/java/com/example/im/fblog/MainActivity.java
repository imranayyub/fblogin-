package com.example.im.fblog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    Button bnfb;
    TextView Status_view;
    CallbackManager callbackManager;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bnfb = (Button) findViewById(R.id.bnfb);
        img=(ImageView)findViewById(R.id.img);
        Status_view = (TextView) findViewById(R.id.Status_view);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

//                        Status_view.setText("Login Success " + loginResult.getAccessToken().getUserId().toString() + "\n" + loginResult.getAccessToken().getToken().toString());

                        setFacebookData(loginResult);
                        bnfb.setText("Log Out");
                    }

                    @Override
                    public void onCancel() {
                        Status_view.setText("Login Cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Status_view.setText("An Error Occured");
                    }
                });

        bnfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("promo", MODE_PRIVATE);
                boolean activated = pref.getBoolean("activated", false);
                if(activated == false) {  // User hasn't actived the promocode -> activate it
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("activated", true);
                    editor.commit();
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
                }
                else
                {
                    LoginManager.getInstance().logOut();
                    bnfb.setText("Continue with Fb");
                    Status_view.setText("Status");
                    img.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("activated", false);
                    editor.commit();

                }
            }


        });
    }

    private void setFacebookData(LoginResult loginResult)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        try {
                            Log.i("Response",response.toString());

                            String email = response.getJSONObject().getString("email");
                            String firstName = response.getJSONObject().getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");
                            String gender = response.getJSONObject().getString("gender");
                            Profile profile = Profile.getCurrentProfile();
                            String id = profile.getId();
                            String link = profile.getLinkUri().toString();
                           String name = profile.getName();
                            Status_view.setText("Login Success : " + name + "\n"+ email);
                            img.setVisibility(View.VISIBLE);
                            Log.i("Link",link);
                            if (Profile.getCurrentProfile()!=null)
                            {
                                Glide.with(getApplicationContext()).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200))
                                        .thumbnail(0.5f)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(img);

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }


}

