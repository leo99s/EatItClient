package pht.eatit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import info.hoang8f.widget.FButton;
import pht.eatit.database.Database;
import pht.eatit.global.Config;
import pht.eatit.global.Global;
import pht.eatit.model.Notification;
import pht.eatit.model.Order;
import pht.eatit.model.Request;
import pht.eatit.model.Response;
import pht.eatit.model.Sender;
import pht.eatit.model.Token;
import pht.eatit.remote.FCMService;
import pht.eatit.remote.MapService;
import pht.eatit.viewholder.OrderAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import static java.lang.System.currentTimeMillis;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Cart extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public TextView total_price;
    FButton btnOrder;
    RecyclerView rcvCart;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;

    List<Order> orderList = new ArrayList<>();
    OrderAdapter adapter;

    FCMService mFCMService;
    MapService mMapService;

    Place orderPlace;

    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // Using SandBox for testing, else ENVIRONMENT_PRODUCTION
            .clientId(Config.PAYPAL_ID);

    String address, comment;
    private static final int PAYPAL_REQUEST_CODE = 9999;

    // Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICES_REQUEST_CODE = 9997;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContentView(R.layout.activity_cart);

        mFCMService = Global.getFCMAPI();
        mMapService = Global.getMapAPI();

        // Runtime permission
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if(checkPlayServices()){    // Check if your device has pay services ?
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        // Init PayPal
        Intent paypal = new Intent(this, PayPalService.class);
        paypal.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(paypal);

        total_price = findViewById(R.id.total_price);
        btnOrder = findViewById(R.id.btnOrder);
        rcvCart = findViewById(R.id.rcvCart);
        rcvCart.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rcvCart.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");

        loadOrder();

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(orderList.size() != 0){
                    showAlert();
                }
                else {
                    Toast.makeText(Cart.this, "Your cart is empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){    // Check if your device has pay services ?
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        updateLocation();
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLastLocation != null){
            Log.d("LOCATION", "Your location : " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
        } else {
            Log.d("LOCATION", "Couldn't find your location !");
        }
    }

    private void updateLocation() {
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable((this));

        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, "Your device isn't supported !", Toast.LENGTH_SHORT).show();
                finish();
            }

            return false;
        }

        return true;
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
        alert.setIcon(R.drawable.ic_shopping_cart);
        alert.setTitle("One more step !");
        alert.setMessage("Enter your address :");

        LayoutInflater inflater = this.getLayoutInflater();
        View place_order = inflater.inflate(R.layout.place_order, null);

        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.edtAddress);
        final MaterialEditText edtComment = place_order.findViewById(R.id.edtComment);
        final RadioButton rdiLocation = place_order.findViewById(R.id.rdiLocation);
        final RadioButton rdiAddress = place_order.findViewById(R.id.rdiAddress);

        // Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        // Set hint for Autocomplete EditText
        ((android.widget.EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Address");

        // Set text size
        ((android.widget.EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(20);

        // Get address from Place Autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                orderPlace = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("Place Error", status.getStatusMessage());
            }
        });

        rdiLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mMapService.getAddress(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                                    // If fetch API successfully
                                    try {
                                        JSONObject object = new JSONObject(response.body().toString());
                                        JSONArray result = object.getJSONArray("results");
                                        address = result.getJSONObject(0).getString("formatted_address");
                                        ((android.widget.EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);
                                    } catch (JSONException e) {

                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                    });
                }
            }
        });

        alert.setView(place_order);

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(!rdiLocation.isChecked() && !rdiAddress.isChecked()){
                    if(orderPlace != null){
                        address = orderPlace.getAddress().toString();
                    } else {
                        Toast.makeText(Cart.this, "Please enter your address !", Toast.LENGTH_SHORT).show();

                        // Remove fragment
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.edtAddress)).commit();
                        return;
                    }
                }

                if(TextUtils.isEmpty(address)){
                    Toast.makeText(Cart.this, "Please enter your address !", Toast.LENGTH_SHORT).show();

                    // Remove fragment
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.edtAddress)).commit();
                    return;
                }

                comment = edtComment.getText().toString();
                String fmtPrice = total_price.getText().toString()
                        .replace("$", "")
                        .replace(",", "");

                PayPalPayment payment = new PayPalPayment(
                        new BigDecimal(fmtPrice),
                        "USD",
                        "Eat It Order",
                        PayPalPayment.PAYMENT_INTENT_SALE);

                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
                startActivityForResult(intent, PAYPAL_REQUEST_CODE);

                // Remove fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.edtAddress)).commit();
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();

                // Remove fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.edtAddress)).commit();
            }
        });

        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirmation != null){
                    try {
                        String detail = confirmation.toJSONObject().toString(4);
                        JSONObject object = new JSONObject(detail);

                        // Create new request
                        Request newRequest = new Request(
                                Global.activeUser.getPhone(),
                                Global.activeUser.getName(),
                                address,
                                String.format("%s,%s", orderPlace.getLatLng().latitude, orderPlace.getLatLng().longitude),
                                total_price.getText().toString(),
                                "0",
                                comment,
                                object.getJSONObject("response").getString("state"),
                                orderList
                        );

                        // Use System.currentTimeMillis to key and submit to Firebase
                        String id_order = String.valueOf(currentTimeMillis());
                        request.child(id_order).setValue(newRequest);
                        sendNotification(id_order);

                        // Clear the cart
                        new Database(getBaseContext()).clearCart();

                        Toast.makeText(this, "Thanks for your order !", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (JSONException e) {

                    }
                }
            } else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid payment !", Toast.LENGTH_SHORT).show();
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment was canceled !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotification(final String id_order) {
        DatabaseReference reference = database.getInstance().getReference("Token");
        Query tokens = reference.orderByChild("serverToken").equalTo(true);

        tokens.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    Token serverToken = childDataSnapshot.getValue(Token.class);

                    // Create raw payload to send
                    Notification notification = new Notification("Hoàng Tâm", "You have a new order : " + id_order);
                    Sender content = new Sender(serverToken.getToken(), notification);
                    mFCMService.sendNotification(content)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    if(response.code() == 200){
                                        if(response.body().success == 1){
                                            Toast.makeText(Cart.this, "Thanks for your order !", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(Cart.this, "Sorry, something was wrong !", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadOrder() {
        orderList = new Database(this).loadOrder();
        adapter = new OrderAdapter(this, orderList);
        adapter.notifyDataSetChanged();
        rcvCart.setAdapter(adapter);

        // Calculate total price
        int total = 0;

        for (Order order : orderList){
            total += Integer.parseInt(order.getPrice()) * Integer.parseInt(order.getQuantity());
        }

        Locale locale = new Locale("en", "US");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        total_price.setText(numberFormat.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Global.DELETE)){
            deleteOrder(item.getOrder());
        }
        
        return true;
    }

    private void deleteOrder(int position) {
        orderList.remove(position);
        new Database(this).clearCart();
        
        for (Order order : orderList){
            new Database(this).addOrder(order);
        }

        loadOrder();
    }
}
