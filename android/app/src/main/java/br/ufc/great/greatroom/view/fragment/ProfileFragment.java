package br.ufc.great.greatroom.view.fragment;

/**
 * Created by belmondorodrigues on 22/09/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.AppController;
import br.ufc.great.greatroom.model.PersonModel;
import br.ufc.great.greatroom.util.LogFileHelper;


public class ProfileFragment extends Fragment {
    private static final String TAG = ProfileFragment.class.getSimpleName();
    private CallbackManager facebookCallbackManager;
    private ProfileTracker facebookProfileTracker;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                doFacebookLogin();
            }
        };


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        loginButton.setFragment(this);
        loginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        facebookProfileTracker.stopTracking();
    }

    @Override
    public void onStart() {
        super.onStart();
        doFacebookLogin();
        updateUi();
    }

    private void doFacebookLogin() {
        final long startTime = System.currentTimeMillis();
        final Profile profile = Profile.getCurrentProfile();
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (null == profile || null == accessToken) {
            AppController.getInstance().setUser(null);
            updateUi();
            return;
        }

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (null != object) {
                            try {
                                String imageUrl = profile.getProfilePictureUri(150, 150).toString();
                                PersonModel personModel = new PersonModel();
                                personModel.setName(profile.getName());
                                personModel.setImageUrl(imageUrl);
                                personModel.setEmail(object.getString("email"));

                                LogFileHelper.success(TAG, "login", startTime);
                                AppController.getInstance().setUser(personModel);
                            } catch (Exception e) {
                                LogFileHelper.error(TAG, "login", startTime, e.getMessage());
                            }
                        } else {
                            String errorMessage = "";
                            if (response != null && response.getError() != null) {
                                errorMessage = response.getError().getErrorMessage();
                            }
                            LogFileHelper.error(TAG, "login", startTime, errorMessage);
                            AppController.getInstance().setUser(null);
                        }
                        updateUi();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void updateUi() {
        if (getView() == null)
            return;

        PersonModel user = AppController.getInstance().getUser();
        View loggedView = getView().findViewById(R.id.logged_layout);

        if (user != null) {
            loggedView.setVisibility(View.VISIBLE);
            NetworkImageView imageView = (NetworkImageView) getView().findViewById(R.id.user_image);
            TextView userName = (TextView) getView().findViewById(R.id.user_name);
            userName.setText(user.getName());
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                imageView.setImageUrl(user.getImageUrl(), AppController.getInstance().getImageLoader());
            } else {
                imageView.setImageUrl("", AppController.getInstance().getImageLoader());
            }
        } else {
            loggedView.setVisibility(View.GONE);
        }
    }


}