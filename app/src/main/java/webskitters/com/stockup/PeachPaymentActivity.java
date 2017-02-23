package webskitters.com.stockup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.connect.PWConnect;
import com.mobile.connect.exception.PWError;
import com.mobile.connect.exception.PWException;
import com.mobile.connect.exception.PWProviderNotInitializedException;
import com.mobile.connect.listener.PWTokenObtainedListener;
import com.mobile.connect.listener.PWTransactionListener;
import com.mobile.connect.payment.PWCurrency;
import com.mobile.connect.payment.PWPaymentParams;
import com.mobile.connect.payment.credit.PWCreditCardType;
import com.mobile.connect.provider.PWTransaction;
import com.mobile.connect.service.PWProviderBinder;

import webskitters.com.stockup.Utils.Utils;

public class PeachPaymentActivity extends AppCompatActivity implements
        PWTokenObtainedListener, PWTransactionListener {

    EditText et_name, et_card_no, et_card_exp, et_card_cvv;
    Button btn_pay;
    TextView txt_success;
    String strName = "", strCardNo = "", strCVV = "", month = "", year = "";
    Double amtPay = 0.0;
    ProgressDialog pDialog;
    boolean callTokenSave = true;
    private PWProviderBinder _binder;
    // Test
    //private static final String APPLICATIONIDENTIFIER = "peach.Stockup.mcommerce.test";
    //private static final String PROFILETOKEN = "7e352877b32a11e6a349d9c796a85012";
    // Live
    private static final String APPLICATIONIDENTIFIER = "peach.Stockup.mcommerce";
    private static final String PROFILETOKEN = "e933ab24b32811e68fe38909e007f9c9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_peach_payment);
        initFields();
        startService(new Intent(this,
                com.mobile.connect.service.PWConnectService.class));
        bindService(new Intent(this,
                        com.mobile.connect.service.PWConnectService.class),
                _serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initFields() {
        et_name = (EditText) findViewById(R.id.et_name);
        et_card_no = (EditText) findViewById(R.id.et_card_no);
        et_card_exp = (EditText) findViewById(R.id.et_card_exp);
        et_card_cvv = (EditText) findViewById(R.id.et_card_cvv);

        btn_pay = (Button) findViewById(R.id.btn_pay);

        txt_success = (TextView) findViewById(R.id.txt_success);

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_success.setVisibility(View.GONE);
                payUsingPeach();
            }
        });
    }

    private void payUsingPeach() {
        strName = et_name.getText().toString().trim();
        strCardNo = et_card_no.getText().toString().trim();
        strCVV = et_card_cvv.getText().toString().trim();

        if (et_card_exp.getText().toString().trim().length() == 7){
            month = et_card_exp.getText().toString().substring(0, 2);
            year = et_card_exp.getText().toString().substring(3, 7);
        }
        else {
            Toast.makeText(PeachPaymentActivity.this,"Expiary Date not in correct Format.", Toast.LENGTH_LONG).show();
        }

        PWPaymentParams paymentParams = null;

        String customIdentifier  = "1002";
        String givenName = strName;
        String familyName = strName;
        String street = "1st Avenue";
        String zip = "711109";
        String city = "Kolkata";
        String state = "West Bengal";
        String countryCode = "+91";
        String email = "partha.chatterjee@webskitters.com";
        String IPaddress = Utils.getIPAddress(true);
        try {
            paymentParams = _binder.getPaymentParamsFactory().createCreditCardPaymentParams(amtPay, PWCurrency.SOUTH_AFRICA_RAND, "A test charge", strName, PWCreditCardType.VISA, strCardNo, year, month, strCVV);
            paymentParams.setCustomIdentifier(customIdentifier);
            paymentParams.setCustomerGivenName(givenName);
            paymentParams.setCustomerFamilyName(familyName);
            paymentParams.setCustomerAddressStreet(street);
            paymentParams.setCustomerAddressZip(zip);
            paymentParams.setCustomerAddressCity(city);
            paymentParams.setCustomerAddressState(state);
            paymentParams.setCustomerAddressCountryCode(countryCode);
            paymentParams.setCustomerEmail(email);
            paymentParams.setCustomerIP(IPaddress);

        } catch (PWProviderNotInitializedException e) {
            e.printStackTrace();
            return;
        } catch (PWException e) {
            e.printStackTrace();
            return;
        }

        try {
            pDialog = new ProgressDialog(PeachPaymentActivity.this);
            pDialog.show();
            pDialog.setMessage("Processing.Please Wait.");
            pDialog.setCanceledOnTouchOutside(false);
            _binder.createAndRegisterDebitTransaction(paymentParams);
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Could not contact Gateway!");
            //Toast.makeText(PeachPaymentActivity.this,"Error: Could not contact Gateway!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(_serviceConnection);
        stopService(new Intent(this,
                com.mobile.connect.service.PWConnectService.class));
    }

    @Override
    public void obtainedToken(String token, PWTransaction pwTransaction) {
        Log.i("TokenizationActivity", token);
        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }

        setStatusText("Your transaction is successfully done.thank you.");

        /*Intent intent = new Intent(PeachPaymentActivity.this, PeachPaymentActivity.class);
        finish();
        startActivity(intent);*/
        //displayAlert("Your transaction is successfully done.thank you.");
    }

    private void setStatusText(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                txt_success.setVisibility(View.VISIBLE);
                txt_success.setText(msg);
            }
        });
    }

    @Override
    public void transactionSucceeded(PWTransaction pwTransaction) {
        if (callTokenSave){
            saveCard();

        }
    }

    @Override
    public void transactionFailed(PWTransaction pwTransaction, PWError pwError) {
        setStatusText("Error contacting the gateway.");
        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        //Toast.makeText(PeachPaymentActivity.this,, Toast.LENGTH_LONG).show();
        Log.e("TokenizationActivity", pwError.getErrorMessage());
    }

    @Override
    public void creationAndRegistrationSucceeded(PWTransaction pwTransaction) {
        //setStatusText("Processing...");
        try {
            if (callTokenSave) {
                _binder.debitTransaction(pwTransaction);
            }
            else {
                _binder.obtainToken(pwTransaction);
            }
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Invalid Transaction.");
            //Toast.makeText(PeachPaymentActivity.this,"Invalid Transaction.", Toast.LENGTH_LONG).show();
            //setStatusText("Invalid Transaction.");
            e.printStackTrace();
        }
    }

    @Override
    public void creationAndRegistrationFailed(PWTransaction pwTransaction, PWError pwError) {
        //setStatusText("Error contacting the gateway.");
        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        setStatusText("Error contacting the gateway.");
        //Toast.makeText(PeachPaymentActivity.this,"Error contacting the gateway.", Toast.LENGTH_LONG).show();
        Log.e("TokenizationActivity", pwError.getErrorMessage());
    }

    private void saveCard() {
        PWPaymentParams paymentParams = null;
        try {
            paymentParams = _binder
                    .getPaymentParamsFactory()
                    .createCreditCardTokenizationParams(strName, PWCreditCardType.VISA, strCardNo, year, month, strCVV);

        } catch (PWProviderNotInitializedException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Provider not initialized!");
            //Toast.makeText(PeachPaymentActivity.this,"Error: Provider not initialized!", Toast.LENGTH_LONG).show();
            //setStatusText("Error: Provider not initialized!");
            e.printStackTrace();
            return;
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Invalid Parameters!");
            //Toast.makeText(PeachPaymentActivity.this,"Error: Invalid Parameters!", Toast.LENGTH_LONG).show();
            //setStatusText("Error: Invalid Parameters!");
            e.printStackTrace();
            return;
        }

        callTokenSave = false;

        try {
            _binder.createAndRegisterObtainTokenTransaction(paymentParams);
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Could not contact Gateway!");
            //Toast.makeText(PeachPaymentActivity.this,"Error: Could not contact Gateway!", Toast.LENGTH_LONG).show();
            //setStatusText("Error: Could not contact Gateway!");
            e.printStackTrace();
        }
    }

    private ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _binder = (PWProviderBinder) service;
            // we have a connection to the service
            try {
                _binder.initializeProvider(PWConnect.PWProviderMode.LIVE,
                        APPLICATIONIDENTIFIER, PROFILETOKEN);
                _binder.addTransactionListener(PeachPaymentActivity.this);
                _binder.addTokenObtainedListener(PeachPaymentActivity.this);
                _binder.initializeProvider(PWConnect.PWProviderMode.LIVE,
                        APPLICATIONIDENTIFIER, PROFILETOKEN);
            } catch (PWException ee) {
                //setStatusText("Error initializing the provider.");
                setStatusText("Error initializing the provider.");
                //Toast.makeText(PeachPaymentActivity.this,"Error initializing the provider.", Toast.LENGTH_LONG).show();
                // error initializing the provider
                ee.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _binder = null;
        }
    };


}
