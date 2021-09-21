package com.sanioluke00.cinquexassignment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity {

    private final String email_expn =
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
    private final String API_LINK = "https://api.cinquex.com/api/internshala/login";
    TextInputLayout mainlay_emailID_Edittxt, mainlay_passwrd_Edittxt;
    Button mainlay_login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        setContentView(R.layout.activity_main);

        mainlay_emailID_Edittxt = findViewById(R.id.mainlay_emailID_Edittxt);
        mainlay_passwrd_Edittxt = findViewById(R.id.mainlay_passwrd_Edittxt);
        mainlay_login_btn = findViewById(R.id.mainlay_login_btn);

        mainlay_login_btn.setOnClickListener(v -> {

            String email_id = mainlay_emailID_Edittxt.getEditText().getText().toString();
            String password = mainlay_passwrd_Edittxt.getEditText().getText().toString();

            if (email_id.isEmpty()) {
                mainlay_emailID_Edittxt.requestFocus();
                mainlay_emailID_Edittxt.setError("Can't be empty !!");
            } else if (!email_id.matches(email_expn)) {
                mainlay_emailID_Edittxt.requestFocus();
                mainlay_emailID_Edittxt.setError("Please enter a valid Email ID !!");
            } else if (password.isEmpty()) {
                mainlay_passwrd_Edittxt.requestFocus();
                mainlay_passwrd_Edittxt.setError("Can't be empty !!");
            } else if (!check_net_connection(getApplicationContext())) {
                Dialog dialog = createDialogBox(MainActivity.this, R.layout.no_net_dialog, false);
                ImageView noNet_image = dialog.findViewById(R.id.noNet_image);
                TextView noNet_content = dialog.findViewById(R.id.noNet_content);
                TextView msg_okbtn = dialog.findViewById(R.id.msg_okbtn);
                dialog.show();
                msg_okbtn.setOnClickListener(v1 -> dialog.dismiss());
            } else {

                mainlay_emailID_Edittxt.getEditText().setText("");
                mainlay_emailID_Edittxt.clearFocus();
                mainlay_emailID_Edittxt.setErrorEnabled(false);
                mainlay_passwrd_Edittxt.getEditText().setText("");
                mainlay_passwrd_Edittxt.clearFocus();
                mainlay_passwrd_Edittxt.setErrorEnabled(false);

                requestLoginUsingAPI(email_id, password);
            }
        });

    }

    private void requestLoginUsingAPI(String emailID, String pass) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        Map<String, String> mapparams = new HashMap();

        mapparams.put("email", emailID);
        mapparams.put("password", pass);
        JSONObject parameters = new JSONObject(mapparams);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                API_LINK,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Dialog dialog = createDialogBox(MainActivity.this, R.layout.no_net_dialog, false);
                        ImageView noNet_image = dialog.findViewById(R.id.noNet_image);
                        TextView noNet_content = dialog.findViewById(R.id.noNet_content);
                        TextView msg_okbtn = dialog.findViewById(R.id.msg_okbtn);
                        dialog.show();

                        try {
                            String result_txt = response.getString("message");
                            Log.e("success_msg", "The message received is " + result_txt);

                            if ((result_txt.toLowerCase()).contains("congrats"))
                                noNet_image.setImageDrawable(getDrawable(R.drawable.ic_success));
                            else
                                noNet_image.setVisibility(View.GONE);

                            noNet_content.setText(result_txt);

                        } catch (JSONException e) {
                            Log.e("json_error", "The json error is " + e);
                        }
                        msg_okbtn.setOnClickListener(v1 -> dialog.dismiss());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Dialog dialog = createDialogBox(MainActivity.this, R.layout.no_net_dialog, false);
                        ImageView noNet_image = dialog.findViewById(R.id.noNet_image);
                        TextView noNet_content = dialog.findViewById(R.id.noNet_content);
                        TextView msg_okbtn = dialog.findViewById(R.id.msg_okbtn);
                        dialog.show();

                        byte[] htmlBodyBytes = error.networkResponse.data;
                        String error_msg = new String(htmlBodyBytes).toLowerCase();
                        Log.e("volley_error", "Volley Error is : " + error_msg);

                        if (error_msg.contains("wrong credentials")) {
                            noNet_image.setImageDrawable(getDrawable(R.drawable.ic_failed));
                            noNet_content.setText("wrong credentials! Please enter proper Email ID and password.");
                        } else if (error_msg.contains("validation error")) {
                            noNet_image.setImageDrawable(getDrawable(R.drawable.ic_error));
                            noNet_content.setText("Validation Error ! Please try again");
                        }

                        msg_okbtn.setOnClickListener(v1 -> dialog.dismiss());
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void requestLoginUsingAPI01(String emailID, String pass) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        Map<String, String> params = new HashMap();
        params.put("email", emailID);
        params.put("password", pass);

        JSONObject parameters = new JSONObject(params);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                API_LINK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Dialog dialog = createDialogBox(MainActivity.this, R.layout.no_net_dialog, false);
                        ImageView noNet_image = dialog.findViewById(R.id.noNet_image);
                        TextView noNet_content = dialog.findViewById(R.id.noNet_content);
                        TextView msg_okbtn = dialog.findViewById(R.id.msg_okbtn);
                        dialog.show();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result_txt = jsonObject.getString("message");
                            Log.e("success_msg", "The message received is " + result_txt);

                            if ((result_txt.toLowerCase()).contains("congrats"))
                                noNet_image.setImageDrawable(getDrawable(R.drawable.ic_success));

                            else if ((result_txt.toLowerCase()).contains("wrong"))
                                noNet_image.setImageDrawable(getDrawable(R.drawable.ic_failed));

                            else if ((result_txt.toLowerCase()).contains("error"))
                                noNet_image.setImageDrawable(getDrawable(R.drawable.ic_error));

                            else
                                noNet_image.setVisibility(View.GONE);

                            noNet_content.setText(result_txt);

                        } catch (
                                JSONException e) {
                            Log.e("json_error", "The json error is " + e);
                        }
                        msg_okbtn.setOnClickListener(v1 -> dialog.dismiss());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("volley_error", "Volley Error is : " + error.getMessage());
            }
        }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", emailID);
                params.put("password", pass);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private boolean check_net_connection(@NotNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected();
    }

    private @NotNull Dialog createDialogBox(Activity activity, int view_id, boolean isclose) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(view_id);
        dialog.setCanceledOnTouchOutside(isclose);
        dialog.setCancelable(isclose);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        return dialog;
    }

}