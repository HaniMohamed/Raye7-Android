package com.proga.hani.raye7;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.GPSTracker;
import Modules.Route;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    GPSTracker gps;
    Boolean getOrigin = false;
    Boolean getDestination = false;

    Geocoder geocoder;
    List<Address> addresses = new ArrayList<>();

    ImageView replace, currentLoc;

    LinearLayout formLL;

    int i=1;


    private GoogleMap mMap;
    private Button btnFindPath;
    private TextView etOrigin;
    private TextView etDestination;
    private TextView tvDuration, tvDistance;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private List<Route> publicRoutes = new ArrayList<>();
    private ProgressDialog progressDialog;

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_actionbar);

        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        geocoder = new Geocoder(this, Locale.getDefault());

        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        formLL = (LinearLayout) findViewById(R.id.form_LL);


        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (TextView) findViewById(R.id.etOrigin);
        etOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    getOrigin = true;
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }

            }
        });


        etDestination = (TextView) findViewById(R.id.etDestination);
        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    getDestination = true;
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }

            }
        });

        replace = (ImageView) findViewById(R.id.replace);
        replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = etDestination.getText().toString();
                etDestination.setText(etOrigin.getText().toString());
                etOrigin.setText(to);
            }
        });

        currentLoc = (ImageView) findViewById(R.id.currentLoc);
        currentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                } else {

                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    gps = new GPSTracker(MainActivity.this);

                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        getLocation();
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String address = addresses.get(0).getAddressLine(0) + "," + addresses.get(0).getAddressLine(1) + "," + addresses.get(0).getAddressLine(2) + "," + addresses.get(0).getAddressLine(3);
                        etOrigin.setText(address);


                        //draw marker and move camera to the place
                        final LatLng latLng = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .position(latLng));

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                                new LatLng(latitude, longitude)).zoom(12).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                        showFormLL();
                    }

                }
            }
        });


        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps = new GPSTracker(this);
        mMap = googleMap;
        getLocation();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }else{

            mMap.setMyLocationEnabled(true);
        }



        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                double lat = latLng.latitude;
                double lng = latLng.longitude;

                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String address = addresses.get(0).getAddressLine(0) + "," + addresses.get(0).getAddressLine(1) + "," + addresses.get(0).getAddressLine(2) + "," + addresses.get(0).getAddressLine(3);
                etDestination.setText(address);



                showFormLL();

            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                polyline.setColor(Color.BLUE);
                polyline.setWidth(25);
                int routId = Integer.parseInt(polyline.getId().toString().substring(2));

                tvDuration.setText(publicRoutes.get(routId).duration.text);
                tvDistance.setText(publicRoutes.get(routId).distance.text);

                for (Polyline line : polylinePaths) {
                    if (!line.getId().toString().equals(polyline.getId().toString())) {
                        line.setColor(Color.GRAY);
                        line.setWidth(15);
                    }
                }

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (formLL.getVisibility() == View.VISIBLE) {
                    hideFormLL();
                } else {
                    showFormLL();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });


        mMap.setTrafficEnabled(true);
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }else {
            gps = new GPSTracker(this);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)).zoom(12).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        mMap.clear();
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int x = 0;

        for (Route route : routes) {
            x++;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 12));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));


            publicRoutes.add(route);

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    clickable(true);

            //Showing ETA on routes
            int s = route.points.size();
            LatLng latLng = route.points.get((s / 2));
            Marker m = createMarker(latLng, route.duration.text);


            builder.include(route.startLocation);
            builder.include(route.endLocation);



            if (x == 1) {
                polylineOptions.
                        color(Color.BLUE).
                        width(25);
                m.showInfoWindow();
                tvDuration.setText(publicRoutes.get(0).duration.text);
                tvDistance.setText(publicRoutes.get(0).distance.text);

            } else {
                polylineOptions.
                        color(Color.GRAY).
                        width(15);

            }

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

        //Moving camera to Fit the route
        LatLngBounds bounds = builder.build();
        int padding = 250; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

        hideFormLL();


    }

    //Get AutoComplete Google place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (getOrigin) {
                    etOrigin.setText(place.getAddress());

                    //draw marker and move camera to the place
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(place.getLatLng()));

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).zoom(12).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



                } else if (getDestination) {
                    etDestination.setText(place.getAddress());

                    //draw marker and move camera to the place
                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(place.getLatLng()));

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).zoom(12).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        getOrigin = false;
        getDestination = false;

    }

    // Show (From/To) Layout
    public void showFormLL() {
        formLL.animate()
                .alpha(1.0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        formLL.setVisibility(View.VISIBLE);
                    }
                });
        formLL.setVisibility(View.VISIBLE);
    }

    // Hide (From/To) Layout
    public void hideFormLL() {

        formLL.animate()
                .alpha(0.0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        formLL.setVisibility(View.GONE);
                    }
                });
    }

    protected Marker createMarker(LatLng latLng, String snippet) {

        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.tvETA);
        numTxt.setText(snippet);


        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))));
    }

    public boolean checkPermission(String permission) {

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{permission},
                    i);

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission},
                        i);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        Boolean permissionStatus = MainActivity.this.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;


        i++;
        return permissionStatus;
    }



}
