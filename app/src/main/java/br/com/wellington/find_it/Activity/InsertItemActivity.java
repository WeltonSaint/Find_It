package br.com.wellington.find_it.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.wellington.find_it.Adapter.GalleryAdapter;
import br.com.wellington.find_it.Bean.Cliente;
import br.com.wellington.find_it.Bean.Item;
import br.com.wellington.find_it.Utils.ImageUtils;
import me.nereo.multi_image_selector.MultiImageSelector;
import br.com.wellington.find_it.R;

import static br.com.wellington.find_it.Utils.Consts.SERVICE_URL;

public class InsertItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private EditText mItemName, mItemDescription;
    private Spinner spinSituation, spinCategory;
    private ProgressDialog progressDialog;
    private RecyclerView mPhotosItemRecyclerView;

    private double lat;
    private double longit;
    private double distance;
    private int strokeColor = 0x33b5e6;
    private int shadeColor = 0x4433b5e6;
    ArrayList<String> imagesEncodedList;
    ArrayList<Bitmap> images;
    private static Cliente user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_item);

        mItemName = (EditText) findViewById(R.id.name_item);
        mItemDescription = (EditText) findViewById(R.id.description_item);

        spinSituation = (Spinner) findViewById(R.id.spin_situation_item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.situation_item, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSituation.setAdapter(adapter);

        spinCategory = (Spinner) findViewById(R.id.spin_category_item);

        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(this,
                R.array.category_item, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategory.setAdapter(adapterCategory);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = (Cliente) extras.getSerializable("user");
        }

        mPhotosItemRecyclerView = (RecyclerView) findViewById(R.id.photos_item_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPhotosItemRecyclerView.setLayoutManager(llm);

        Button mChoosePhotosItem = (Button) findViewById(R.id.choose_photos_item);
        mChoosePhotosItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mMap.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.insert_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_insert) {
            insertItem();
            return true;
        } else if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        distance = 0;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            lat = location.getLatitude();
            longit = location.getLongitude();
            CircleOptions circleOptions = new CircleOptions().center(new LatLng(lat, longit)).radius(distance).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2);
            googleMap.addCircle(circleOptions);
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(getResources().getString(R.string.reference_point))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(174)));
        }
        //add location button click listener
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                if (ActivityCompat.checkSelfPermission(InsertItemActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InsertItemActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                final Location location = locationManager.getLastKnownLocation(locationProvider);

                if (location != null) {
                    googleMap.clear();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                            .zoom(17)
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    lat = location.getLatitude();
                    longit = location.getLongitude();
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(getResources().getString(R.string.reference_point))
                            .draggable(true)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(174)));
                }
                return true;
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                googleMap.clear();
                lat = latLng.latitude;
                longit = latLng.longitude;

                CircleOptions circleOptions = new CircleOptions().center(latLng).radius(distance).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2);
                googleMap.addCircle(circleOptions);
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getResources().getString(R.string.reference_point))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(174)));
            }
        });


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();

                Location loc1 = new Location("");
                Location loc2 = new Location("");

                loc1.setLatitude(lat);
                loc1.setLongitude(longit);
                loc2.setLatitude(latLng.latitude);
                loc2.setLongitude(latLng.longitude);

                distance = loc1.distanceTo(loc2);

                CircleOptions circleOptions = new CircleOptions().center(new LatLng(lat, longit)).radius(distance).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2);
                googleMap.addCircle(circleOptions);
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, longit))
                        .title(getResources().getString(R.string.reference_point))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(174)));
            }
        });

    }

    public void insertItem() {

        // Store values at the time of the sign up attempt.
        String nameItem = mItemName.getText().toString();
        String description = mItemDescription.getText().toString();
        String category = spinCategory.getSelectedItem().toString();
        String situation = spinSituation.getSelectedItem().toString();

        boolean valid = true;

        // Check for a valid item.
        if (TextUtils.isEmpty(nameItem)) {
            mItemName.setError(getString(R.string.error_field_required));
            valid = false;
        } else {
            mItemName.setError(null);
        }
        if (TextUtils.isEmpty(description)) {
            mItemDescription.setError(getString(R.string.error_field_required));
            valid = false;
        } else {
            mItemDescription.setError(null);
        }
        if (category.equals("Categoria")) {
            Toast.makeText(this, getResources().getString(R.string.error_choose_category), Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (lat == 0 && longit == 0) {
            Toast.makeText(this, getResources().getString(R.string.error_reference_point), Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (distance == 0) {
            Toast.makeText(this, getResources().getString(R.string.error_reference_radius), Toast.LENGTH_LONG).show();
            valid = false;
        }
        if (valid)
            insertItemRequest(new Item(nameItem, description, lat, longit, distance, category, situation, (int) user.getCodigoCliente()));
    }


    @SuppressLint("PrivateResource")
    public void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            imagesEncodedList = new ArrayList<>();
            MultiImageSelector selector = MultiImageSelector.create();
            selector.count(5);
            selector.multi();
            selector.origin(imagesEncodedList);
            selector.start(InsertItemActivity.this, REQUEST_IMAGE);
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
                            ActivityCompat.requestPermissions(InsertItemActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private void insertItemRequest(final Item item) {

        progressDialog = ProgressDialog.show(InsertItemActivity.this, getString(R.string.insert_item_progress_dialog_title), getString(R.string.insert_item_progress_dialog_content), false, false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVICE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        progressDialog.dismiss();

                        //Showing message of the response
                        JSONObject jsonObject;
                        try {

                            s = new String(s.getBytes("ISO-8859-1"), "UTF-8");

                            jsonObject = new JSONObject(s);
                            AlertDialog.Builder dlg = new AlertDialog.Builder(InsertItemActivity.this);
                            dlg.setMessage(jsonObject.getString("message"));

                            if (jsonObject.getBoolean("error"))

                                dlg.setPositiveButton("Ok", null);

                            else {
                                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });

                            }

                            dlg.show();

                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Disimissing the progress dialog
                        progressDialog.dismiss();
                        //Showing toast
                        Toast.makeText(InsertItemActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("action", "insertItem");
                params.put("nomeItem", item.getNomeItem());
                params.put("descricaoItem", item.getDescricaoItem());
                params.put("latitudeItem", String.valueOf(item.getLatitudeItem()));
                params.put("longitudeItem", String.valueOf(item.getLongitudeItem()));
                params.put("raioItem", String.valueOf(item.getRaioItem()));
                params.put("nomeCategoria", item.getCategoriaItem());
                params.put("nomeStatus", item.getStatusItem());
                params.put("codigoCliente", String.valueOf(item.getCodigoUsuario()));
                if (images != null) {
                    for (int i = 0; i < images.size(); i++) {
                        params.put("fotoItem[" + i + "]", ImageUtils.getStringImage(images.get(i)));
                    }
                }
                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                imagesEncodedList = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                images = new ArrayList<>();
                List<Uri> imagesUri = new ArrayList<>();
                for (int i = 0; i < imagesEncodedList.size(); i++) {
                    File imgFile = new File(imagesEncodedList.get(i));
                    Uri uri = Uri.fromFile(imgFile);
                    Bitmap img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imagesUri.add(uri);
                    images.add(img);
                }

                GalleryAdapter adapter = new GalleryAdapter(getApplicationContext(), imagesUri);
                mPhotosItemRecyclerView.setAdapter(adapter);

                if (imagesUri.size() != 0) {
                    mPhotosItemRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mPhotosItemRecyclerView.setVisibility(View.GONE);
                }

            }
        }
    }

}
