package br.com.wellington.find_it.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.R;
import br.com.wellington.find_it.Utils.ConnectionManagement;
import br.com.wellington.find_it.Utils.ImageUtils;
import br.com.wellington.find_it.Utils.SimpleSHA1;
import de.hdodenhof.circleimageview.CircleImageView;
import me.nereo.multi_image_selector.MultiImageSelector;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

public class ProfileActivity extends AppCompatActivity {

    private TextInputLayout mNameProfileTextInputLayout, mEmailProfileTextInputLayout, mContactProfileTextInputLayout, mPasswordProfileTextInputLayout;
    private AppCompatEditText mNameProfile, mEmailProfile, mContactProfile, mPasswordProfile;
    private CircleImageView mProfileImage;

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private ArrayList<String> mSelectPath;
    private Bitmap profileBitmap;
    private static Cliente user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton mAlterImageButton = (ImageButton) findViewById(R.id.alter_image_button);
        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        mNameProfile = (AppCompatEditText) findViewById(R.id.name_profile);
        mEmailProfile = (AppCompatEditText) findViewById(R.id.email_profile);
        mContactProfile = (AppCompatEditText) findViewById(R.id.contact_profile);
        mPasswordProfile = (AppCompatEditText) findViewById(R.id.password_profile);
        mNameProfileTextInputLayout = (TextInputLayout) findViewById(R.id.name_profile_text_input_layout);
        mEmailProfileTextInputLayout = (TextInputLayout) findViewById(R.id.email_profile_text_input_layout);
        mContactProfileTextInputLayout = (TextInputLayout) findViewById(R.id.contact_profile_text_input_layout);
        mPasswordProfileTextInputLayout = (TextInputLayout) findViewById(R.id.password_profile_text_input_layout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = (Cliente) extras.getSerializable("user");
        }

        loadUserElements();

        mAlterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiImageSelector.create()
                        .start(ProfileActivity.this, REQUEST_IMAGE);

            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openZoomImageLayout(user.getLinkFotoPerfilCliente());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveProfile();
            return true;
        } else if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openZoomImageLayout(String linksFotoPerfil){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("fotoPerfil", linksFotoPerfil);
        bundle.putString("option", "zoomPhotoProfile");
        intent.putExtras(bundle);
        intent.setClass(getApplicationContext(), ZoomImageActivity.class);
        startActivity(intent);
    }

    private void loadUserElements() {

        if (user.getLinkFotoPerfilCliente() != null && !user.getLinkFotoPerfilCliente().equals(""))
            Picasso.with(getApplicationContext()).load(user.getLinkFotoPerfilCliente()).noFade().placeholder(R.drawable.profile).into(mProfileImage);

        mNameProfile.setText(user.getNomeCliente());
        mEmailProfile.setText(user.getEmailCliente());
        mContactProfile.setText(user.getContatoCliente());

    }

    private boolean isUpdatedProfile() {
        boolean updated = false;
        if (!mNameProfile.getText().toString().trim().equals(user.getNomeCliente()))
            updated = true;
        if (!mEmailProfile.getText().toString().trim().equals(user.getEmailCliente()))
            updated = true;
        if (!mContactProfile.getText().toString().trim().equals(user.getContatoCliente()))
            updated = true;
        if (!mPasswordProfile.getText().toString().trim().isEmpty())
            updated = true;

        return updated;
    }

    private void saveProfile() {
        boolean valid = true;

        String name = mNameProfile.getText().toString().trim(),
                email = mEmailProfile.getText().toString().trim(),
                contact = mContactProfile.getText().toString().trim(),
                password = mPasswordProfile.getText().toString().trim();

        if (name.isEmpty() || name.length() <= 7) {
            mNameProfileTextInputLayout.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else {
            mNameProfileTextInputLayout.setError(null);
            mNameProfileTextInputLayout.setErrorEnabled(false);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailProfileTextInputLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            mEmailProfileTextInputLayout.setError(null);
            mEmailProfileTextInputLayout.setErrorEnabled(false);
        }

        if (contact.isEmpty() || !Patterns.PHONE.matcher(contact).matches()) {
            mContactProfileTextInputLayout.setError(getString(R.string.error_invalid_contact));
            valid = false;
        } else {
            mEmailProfileTextInputLayout.setError(null);
            mEmailProfileTextInputLayout.setErrorEnabled(false);
        }

        if (!password.isEmpty() && password.length() < 6) {
            mPasswordProfileTextInputLayout.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            mPasswordProfileTextInputLayout.setError(null);
            mPasswordProfileTextInputLayout.setErrorEnabled(false);
        }

        if (valid && (isUpdatedProfile() || profileBitmap != null))
            updateProfile();

    }

    @SuppressLint("PrivateResource")
    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            MultiImageSelector selector = MultiImageSelector.create();
            selector.showCamera(true);
            selector.count(1);
            selector.multi();
            selector.origin(mSelectPath);
            selector.start(ProfileActivity.this, REQUEST_IMAGE);
        }
    }

    @SuppressLint("PrivateResource")
    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void updateProfile() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, getString(R.string.save_profile_progress_dialog_title), getString(R.string.save_profile_progress_dialog_content), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();

                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(s);
                            AlertDialog.Builder dlg = new AlertDialog.Builder(ProfileActivity.this);
                            dlg.setMessage(jsonObject.getString("message"));

                            if (jsonObject.getBoolean("error"))

                                dlg.setPositiveButton("Ok", null);

                            else {

                                final JSONObject finalJsonObject = jsonObject;
                                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            Intent intent = new Intent();
                                            JSONObject jsonClient = finalJsonObject.getJSONObject("client");

                                            Cliente user = new Cliente(jsonClient.getLong("codigoCliente"), jsonClient.getString("nomeCliente"), jsonClient.getString("emailCliente"), jsonClient.getString("senhaCliente"), jsonClient.getString("contatoCliente"));

                                            if (jsonClient.getString("linkFotoCliente") != null && !jsonClient.getString("linkFotoCliente").equals("null") && !jsonClient.getString("linkFotoCliente").equals("")) {
                                                user.setLinkFotoPerfilCliente(jsonClient.getString("linkFotoCliente"));
                                            }

                                            intent.putExtra("user", (Serializable) user);
                                            setResult(1, intent);
                                            if(ConnectionManagement.isPasswordSave(getApplicationContext())){
                                                ConnectionManagement.savePreferences(user.getEmailCliente(), user.getSenhaCliente(), getApplicationContext());
                                            }
                                            finish();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

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
                        //Dismissing the progress dialog and show Toast
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new Hashtable<>();
                try {
                    String name = mNameProfile.getText().toString().trim(),
                            email = mEmailProfile.getText().toString().trim(),
                            contact = mContactProfile.getText().toString().trim(),
                            password = (mPasswordProfile.getText().toString().trim().isEmpty()) ? user.getSenhaCliente() : SimpleSHA1.SHA1(mPasswordProfile.getText().toString().trim());

                    //Adding parameters
                    params.put("action", "updateUser");
                    params.put("codigoCliente", String.valueOf(user.getCodigoCliente()));
                    params.put("nomeCliente", name);
                    params.put("emailCliente", email);
                    params.put("contatoCliente", contact);
                    params.put("senhaCliente", password);

                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (profileBitmap != null) {

                    String image = ImageUtils.getStringImage(profileBitmap);
                    params.put("fotoPerfil", image);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                File imgFile = new File(mSelectPath.get(0));
                profileBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                mProfileImage.setImageBitmap(profileBitmap);
            }
        }
    }


}
