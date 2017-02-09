package br.com.wellington.find_it.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;

import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import br.com.wellington.find_it.Utils.SimpleSHA1;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

public class LoginActivity extends AppCompatActivity {

    private final static int LOGIN_ACTION = 0;
    private final static int FORGOT_ACTION = 1;
    private final static int SIGN_UP_ACTION = 2;

    // UI Elements
    private LinearLayout mLoginStatus, mLoginForm, mForgotForm, mSignUpForm;
    private TextInputLayout mEmailLoginTextInputLayout, mPasswordLoginTextInputLayout, mEmailForgotTextInputLayout, mNameSignUpTextInputLayout, mEmailSignUpTextInputLayout, mContactSignUpTextInputLayout, mPasswordSignUpTextInputLayout, mRepeatPasswordSignUpTextInputLayout;
    private AppCompatEditText mEmailLogin, mPasswordLogin, mEmailForgot, mNameSignUp, mEmailSignUp, mContactSignUp, mPasswordSignUp, mRepeatPasswordSignUp;
    private Animation animTranslate, animFadeOut, animFadeIn, animFadeOutForms, animFadeInForms;
    private ScrollView mScrollingLogin;
    private ImageView mLogo;
    private TextView mNameAppLabel;
    private AppCompatCheckBox mContinueLoggedCheckbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorPrimaryDark));
        }

        // Status
        initStatusScreen();
        // Animations
        initAnimationsForms();
        // Login Form
        initLoginForm();
        // Forgot Form
        initForgotForm();
        // Sign Up form
        initSignUpForm();
        // Splash Screen Animation
        runSplashScreen();

    }

    @SuppressLint("HardwareIds")
    private String getPhoneNumber(){
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telemamanger.getLine1Number();
    }

    private void initStatusScreen() {
        mLoginStatus = (LinearLayout) findViewById(R.id.login_status);
        mScrollingLogin = (ScrollView) findViewById(R.id.scrolling_login);
    }

    private void initAnimationsForms() {
        animTranslate = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate);
        animFadeOut = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
        animFadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
    }

    private void initLoginForm() {

        mLoginForm = (LinearLayout) findViewById(R.id.login_form);
        mEmailLogin = (AppCompatEditText) findViewById(R.id.email_login);
        mEmailLoginTextInputLayout = (TextInputLayout) findViewById(R.id.email_login_text_input_layout);
        mPasswordLogin = (AppCompatEditText) findViewById(R.id.password_login);
        mPasswordLoginTextInputLayout = (TextInputLayout) findViewById(R.id.password_login_text_input_layout);
        mContinueLoggedCheckbox = (AppCompatCheckBox) findViewById(R.id.continue_logged_checkbox);
        AppCompatButton mForgotPasswordButton = (AppCompatButton) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animFadeOutForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
                animFadeInForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                animFadeOutForms.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        clearForms();
                        mLoginForm.setVisibility(View.GONE);
                        mLoginForm.clearAnimation();
                        mForgotForm.setVisibility(View.VISIBLE);
                        mForgotForm.setAnimation(animFadeInForms);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                });
                mLoginForm.startAnimation(animFadeOutForms);
            }
        });
        AppCompatButton mLoginSubmit = (AppCompatButton) findViewById(R.id.login_submit);
        mLoginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (ConnectionManagement.isConnected(LoginActivity.this))
                    login();
                else
                    ConnectionManagement.showIsNotConected(LoginActivity.this);
            }
        });
        AppCompatButton mSignUpButton = (AppCompatButton) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animFadeOutForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
                animFadeInForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                animFadeOutForms.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        clearForms();
                        final Handler handler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mScrollingLogin.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                        }).start();
                        mContactSignUp.setText(getPhoneNumber());
                        mLoginForm.setVisibility(View.GONE);
                        mLoginForm.clearAnimation();
                        mSignUpForm.setVisibility(View.VISIBLE);
                        mSignUpForm.setAnimation(animFadeInForms);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                });
                mLoginForm.startAnimation(animFadeOutForms);
            }
        });

    }

    private void initForgotForm() {
        mForgotForm = (LinearLayout) findViewById(R.id.forgot_form);
        mEmailForgot = (AppCompatEditText) findViewById(R.id.email_forgot);
        mEmailForgotTextInputLayout = (TextInputLayout) findViewById(R.id.email_forgot_text_input_layout);
        AppCompatButton mForgotSubmit = (AppCompatButton) findViewById(R.id.forgot_submit);
        mForgotSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (ConnectionManagement.isConnected(LoginActivity.this))
                    forgotPassword();
                else
                    ConnectionManagement.showIsNotConected(LoginActivity.this);
            }
        });
    }

    private void initSignUpForm() {
        mSignUpForm = (LinearLayout) findViewById(R.id.sign_up_form);
        mNameSignUp = (AppCompatEditText) findViewById(R.id.name_sign_up);
        mNameSignUpTextInputLayout = (TextInputLayout) findViewById(R.id.name_sign_up_text_input_layout);
        mContactSignUp = (AppCompatEditText) findViewById(R.id.contact_sign_up);
        mContactSignUpTextInputLayout = (TextInputLayout) findViewById(R.id.contact_sign_up_text_input_layout);
        mEmailSignUp = (AppCompatEditText) findViewById(R.id.email_sign_up);
        mEmailSignUpTextInputLayout = (TextInputLayout) findViewById(R.id.email_sign_up_text_input_layout);
        mPasswordSignUp = (AppCompatEditText) findViewById(R.id.password_sign_up);
        mPasswordSignUpTextInputLayout = (TextInputLayout) findViewById(R.id.password_sign_up_text_input_layout);
        mRepeatPasswordSignUp = (AppCompatEditText) findViewById(R.id.repeat_password_sign_up);
        mRepeatPasswordSignUpTextInputLayout = (TextInputLayout) findViewById(R.id.repeat_password_sign_up_text_input_layout);
        AppCompatButton mSignUpSubmit = (AppCompatButton) findViewById(R.id.sign_up_submit);
        mSignUpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (ConnectionManagement.isConnected(LoginActivity.this))
                    signUp();
                else
                    ConnectionManagement.showIsNotConected(LoginActivity.this);
            }
        });
    }

    private void runSplashScreen() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        animTranslate.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {
                                mNameAppLabel = (TextView) findViewById(R.id.name_app_label);
                                mNameAppLabel.startAnimation(animFadeOut);
                                mNameAppLabel.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                if(ConnectionManagement.isConnected(getApplicationContext())){
                                    if(ConnectionManagement.isPasswordSave(getApplicationContext())){
                                        loginRequest(ConnectionManagement.getLoginSave(getApplicationContext()).toLowerCase(), ConnectionManagement.getPasswordSave(getApplicationContext()));
                                    } else {
                                        mLoginForm.setVisibility(View.VISIBLE);
                                        mLoginForm.startAnimation(animFadeIn);
                                    }
                                } else {
                                    ConnectionManagement.showIsNotConected(LoginActivity.this);
                                    if(ConnectionManagement.isPasswordSave(getApplicationContext())){
                                        mEmailLogin.setText(ConnectionManagement.getLoginSave(getApplicationContext()));
                                    }
                                    mLoginForm.setVisibility(View.VISIBLE);
                                    mLoginForm.startAnimation(animFadeIn);
                                }
                            }
                        });
                        mLogo = (ImageView) findViewById(R.id.logo);
                        mLogo.startAnimation(animTranslate);
                    }
                }, 3000);
    }

    private void login() {
        boolean valid = true;

        final String email = mEmailLogin.getText().toString();
        final String password = mPasswordLogin.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailLoginTextInputLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            mEmailLoginTextInputLayout.setError(null);
            mEmailLoginTextInputLayout.setErrorEnabled(false);
        }

        if (password.isEmpty() || password.length() < 6) {
            mPasswordLoginTextInputLayout.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            mPasswordLoginTextInputLayout.setError(null);
            mPasswordLoginTextInputLayout.setErrorEnabled(false);
        }

        if (valid) {
            try {
                loginRequest( mEmailLogin.getText().toString().trim().toLowerCase(), SimpleSHA1.SHA1(mPasswordLogin.getText().toString().trim()));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private void forgotPassword() {
        boolean valid = true;

        String email = mEmailForgot.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailForgotTextInputLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            mEmailForgotTextInputLayout.setError(null);
            mEmailForgotTextInputLayout.setErrorEnabled(false);
        }

        if (valid) {
            showProgress(true, FORGOT_ACTION);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            showProgress(false, FORGOT_ACTION);
                        }
                    }, 3000);
        }

    }

    @SuppressWarnings("ConstantConditions")
    private void hideKeyboard() {
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signUp() {
        boolean valid = true;

        String name = mNameSignUp.getText().toString().trim(),
                email = mEmailSignUp.getText().toString().trim(),
                contact = mContactSignUp.getText().toString().trim(),
                password = mPasswordSignUp.getText().toString().trim(),
                repeatPassword = mRepeatPasswordSignUp.getText().toString().trim();

        if (name.isEmpty() || name.length() <= 7) {
            mNameSignUpTextInputLayout.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else {
            mNameSignUpTextInputLayout.setError(null);
            mNameSignUpTextInputLayout.setErrorEnabled(false);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailSignUpTextInputLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            mEmailSignUpTextInputLayout.setError(null);
            mEmailSignUpTextInputLayout.setErrorEnabled(false);
        }

        if (contact.isEmpty() || !Patterns.PHONE.matcher(contact).matches()) {
            mContactSignUpTextInputLayout.setError(getString(R.string.error_invalid_contact));
            valid = false;
        } else {
            mContactSignUpTextInputLayout.setError(null);
            mContactSignUpTextInputLayout.setErrorEnabled(false);
        }

        if (password.isEmpty() || password.length() < 6) {
            mPasswordSignUpTextInputLayout.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            mPasswordSignUpTextInputLayout.setError(null);
            mPasswordSignUpTextInputLayout.setErrorEnabled(false);
        }
        if (!password.equals(repeatPassword)) {
            mRepeatPasswordSignUpTextInputLayout.setError(getString(R.string.error_repeat_password));
            valid = false;
        } else {
            mRepeatPasswordSignUpTextInputLayout.setError(null);
            mRepeatPasswordSignUpTextInputLayout.setErrorEnabled(false);
        }

        if (valid) {
            signUpRequest();
        }
    }

    private void loginRequest(final String email, final String password) {
        showProgress(true, LOGIN_ACTION);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        showProgress(false, LOGIN_ACTION);
                        //Showing toast message of the response
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.getBoolean("error")) {
                                mPasswordLoginTextInputLayout.setError(jsonObject.getString("message"));
                            } else {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                JSONObject jsonClient = jsonObject.getJSONObject("client");

                                Cliente user = new Cliente(jsonClient.getLong("codigoCliente"), jsonClient.getString("nomeCliente"), jsonClient.getString("emailCliente"), jsonClient.getString("senhaCliente"), jsonClient.getString("contatoCliente"));

                                if (mContinueLoggedCheckbox.isChecked()) {
                                    ConnectionManagement.savePreferences(user.getEmailCliente(), user.getSenhaCliente(), getApplicationContext());
                                }

                                if (jsonClient.getString("linkFotoCliente") != null && !jsonClient.getString("linkFotoCliente").equals("null") && !jsonClient.getString("linkFotoCliente").equals("")) {
                                    user.setLinkFotoPerfilCliente(jsonClient.getString("linkFotoCliente"));
                                }

                                bundle.putSerializable("user", user);
                                intent.putExtras(bundle);
                                intent.setClass(LoginActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showProgress(false, LOGIN_ACTION);
                        //Showing toast
                        Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("action", "login");
                params.put("emailCliente", email);
                params.put("senhaCliente", password);


                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void signUpRequest() {
        showProgress(true, SIGN_UP_ACTION);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        showProgress(false, SIGN_UP_ACTION);
                        //Showing toast message of the response
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            AlertDialog.Builder dlg = new AlertDialog.Builder(LoginActivity.this);
                            dlg.setMessage(jsonObject.getString("message"));
                            if (jsonObject.getBoolean("error")) {
                                dlg.setPositiveButton("Ok", null);
                            } else {
                                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        animFadeOutForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
                                        animFadeInForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                                        animFadeOutForms.setAnimationListener(new Animation.AnimationListener() {

                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                clearForms();
                                                mSignUpForm.setVisibility(View.GONE);
                                                mSignUpForm.clearAnimation();
                                                mLoginForm.setVisibility(View.VISIBLE);
                                                mLoginForm.setAnimation(animFadeInForms);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                            }

                                        });
                                        mSignUpForm.startAnimation(animFadeOutForms);
                                    }
                                });
                            }

                            dlg.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showProgress(false, SIGN_UP_ACTION);
                        //Showing toast
                        Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new Hashtable<>();
                String name = mNameSignUp.getText().toString().trim(),
                        email = mEmailSignUp.getText().toString().trim(),
                        contact = mContactSignUp.getText().toString().trim(),
                        password = mPasswordSignUp.getText().toString().trim();

                //Adding parameters
                params.put("action", "signUp");
                params.put("nomeCliente", name);
                params.put("emailCliente", email);
                params.put("contatoCliente", contact);
                try {
                    params.put("senhaCliente", SimpleSHA1.SHA1(password));
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show, int action) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            if (action == FORGOT_ACTION) {
                mForgotForm.setVisibility(show ? View.GONE : View.VISIBLE);
                mForgotForm.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mForgotForm.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            } else if (action == SIGN_UP_ACTION) {
                mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
                mSignUpForm.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            } else {
                mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginForm.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            }
            mLoginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginStatus.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
            if (action == FORGOT_ACTION) {
                mForgotForm.setVisibility(show ? View.GONE : View.VISIBLE);
            } else if (action == SIGN_UP_ACTION) {
                mSignUpForm.setVisibility(show ? View.GONE : View.VISIBLE);
            } else {
                mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void clearForms() {
        mEmailLogin.setText("");
        mPasswordLogin.setText("");
        mEmailForgot.setText("");
        mNameSignUp.setText("");
        mEmailSignUp.setText("");
        mPasswordSignUp.setText("");
        mRepeatPasswordSignUp.setText("");
        mEmailLoginTextInputLayout.setError(null);
        mEmailLoginTextInputLayout.setErrorEnabled(false);
        mPasswordLoginTextInputLayout.setError(null);
        mPasswordLoginTextInputLayout.setErrorEnabled(false);
        mEmailForgotTextInputLayout.setError(null);
        mEmailForgotTextInputLayout.setErrorEnabled(false);
        mNameSignUpTextInputLayout.setError(null);
        mNameSignUpTextInputLayout.setErrorEnabled(false);
        mEmailSignUpTextInputLayout.setError(null);
        mEmailSignUpTextInputLayout.setErrorEnabled(false);
        mContactSignUpTextInputLayout.setError(null);
        mContactSignUpTextInputLayout.setErrorEnabled(false);
        mPasswordSignUpTextInputLayout.setError(null);
        mPasswordSignUpTextInputLayout.setErrorEnabled(false);
        mRepeatPasswordSignUpTextInputLayout.setError(null);
        mRepeatPasswordSignUpTextInputLayout.setErrorEnabled(false);
    }


    @Override
    public void onBackPressed() {
        if (mForgotForm.getVisibility() == View.VISIBLE) { //Hide Forgot Form and show Login form
            animFadeOutForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
            animFadeInForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
            animFadeOutForms.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearForms();
                    mForgotForm.setVisibility(View.GONE);
                    mForgotForm.clearAnimation();
                    mLoginForm.setVisibility(View.VISIBLE);
                    mLoginForm.setAnimation(animFadeInForms);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mForgotForm.startAnimation(animFadeOutForms);
        } else if (mSignUpForm.getVisibility() == View.VISIBLE) { //Hide Sign Up Form and show Login form
            animFadeOutForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fadeout);
            animFadeInForms = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
            animFadeOutForms.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearForms();
                    mSignUpForm.setVisibility(View.GONE);
                    mSignUpForm.clearAnimation();
                    mLoginForm.setVisibility(View.VISIBLE);
                    mLoginForm.setAnimation(animFadeInForms);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });
            mSignUpForm.startAnimation(animFadeOutForms);
        } else
            super.onBackPressed();
    }


}
