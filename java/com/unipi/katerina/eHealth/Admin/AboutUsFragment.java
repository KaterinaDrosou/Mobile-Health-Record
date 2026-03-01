package com.unipi.katerina.eHealth.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.unipi.katerina.eHealth.R;

public class AboutUsFragment extends Fragment {

    Button btnSubmitTicket ;

    public AboutUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        btnSubmitTicket  = view.findViewById(R.id.btnSubmitTicket);

        btnSubmitTicket.setOnClickListener(v -> {
            String url = "https://www.atlassian.com/software/jira?campaign=18442480203&adgroup=140479881486&targetid=kwd-855725830&matchtype=e&network=g&device=c&device_model=&creative=687972959756&keyword=jira&placement=&target=&ds_eid=700000001558501&ds_e1=GOOGLE&gad_source=1&gad_campaignid=18442480203&gclid=EAIaIQobChMIqeC4oMT2kAMV54lQBh3D8irYEAAYASAAEgIW1_D_BwE";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }
}