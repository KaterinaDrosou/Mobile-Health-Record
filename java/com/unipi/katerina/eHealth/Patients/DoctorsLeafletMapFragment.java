package com.unipi.katerina.eHealth.Patients;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.katerina.eHealth.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DoctorsLeafletMapFragment extends Fragment {

    WebView webView;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctors_leaflet_map, container, false);
        webView = view.findViewById(R.id.webViewMap);
        db = FirebaseFirestore.getInstance();

        setupWebView();

        return view;
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadDoctorsAndAddMarkers();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(url));
                    startActivity(Intent.createChooser(emailIntent, "Αποστολή email με..."));
                    return true;
                } else if (url.startsWith("tel:")) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(url));
                    startActivity(callIntent);
                    return true;
                }
                // Για όλα τα άλλα (http, https) άφησέ τα να φορτωθούν κανονικά
                return false;
            }
        });

        webView.loadUrl("file:///android_asset/leaflet_map.html");
    }

    private void loadDoctorsAndAddMarkers() {
        db.collection("Doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String name = doc.getString("FullName");
                        String specialty = doc.getString("specialty");
                        String address = doc.getString("clinicAddress");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");

                        if (address == null || address.isEmpty()) {
                            continue;
                        }

                        try {
                            List<Address> results = geocoder.getFromLocationName(address, 1);
                            if (!results.isEmpty()) {
                                Address loc = results.get(0);
                                double lat = loc.getLatitude();
                                double lon = loc.getLongitude();

                                String js = String.format(Locale.US,
                                        "addDoctorMarker(%f, %f, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                                        lat, lon,
                                        escapeJS(name),
                                        escapeJS(specialty),
                                        escapeJS(address),
                                        escapeJS(email),
                                        escapeJS(phone));

                                webView.post(() -> webView.evaluateJavascript(js, null));
                            } else {
                                Log.w("LeafletDebug", "Δεν βρέθηκε τοποθεσία για: " + address);
                            }
                        } catch (IOException e) {
                            Log.e("LeafletDebug", "Σφάλμα geocoder: " + e.getMessage());
                        }
                    }

                    webView.postDelayed(() ->
                            webView.evaluateJavascript("fitToMarkers();", null), 1000);
                })
                .addOnFailureListener(e -> Log.e("LeafletDebug", "Firestore error: " + e.getMessage()));
    }

    private String escapeJS(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\\\"")
                .replace("\n", "")
                .replace("'", "\\'");
    }
}