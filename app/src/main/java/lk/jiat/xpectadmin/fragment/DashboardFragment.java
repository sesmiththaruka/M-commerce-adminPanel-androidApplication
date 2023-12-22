package lk.jiat.xpectadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import lk.jiat.xpectadmin.MainActivity;
import lk.jiat.xpectadmin.R;


public class DashboardFragment extends Fragment {

    private Button btnViewPendingEvents;
    private Button btnViewApprovedEvents;
    private Button btnViewRejectedEvents;
private FirebaseFirestore firestore;
    public static final String TAG = MainActivity.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"ooookkk");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnViewPendingEvents = view.findViewById(R.id.btnPendingEvent);
        btnViewApprovedEvents = view.findViewById(R.id.btnApprovedEvents);
        btnViewRejectedEvents = view.findViewById(R.id.btnRejectedEvents);

        firestore = FirebaseFirestore.getInstance();



        btnViewPendingEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new PendingEventFragment());
                transaction.commit();

            }
        });

        btnViewApprovedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new ApprovedEventsFragment());
                transaction.commit();

            }
        });

        btnViewRejectedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new RejectedEventsFragment());
                transaction.commit();

            }
        });

    }
}