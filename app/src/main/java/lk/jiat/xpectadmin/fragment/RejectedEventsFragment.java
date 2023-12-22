package lk.jiat.xpectadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.jiat.xpectadmin.MainActivity;
import lk.jiat.xpectadmin.R;
import lk.jiat.xpectadmin.dto.EventDTO;
import lk.jiat.xpectadmin.dto.EventStatusDTO;
import lk.jiat.xpectadmin.service.XpectWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RejectedEventsFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firestore;
    private ArrayList<EventDTO> eventDTOS;
    private EventDTO eventDTO;
    private String eventUniqueId;
    private RecyclerView.Adapter adapter;
    private Button approvebtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rejected_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);

        firestore.collection("eventEntity").whereEqualTo("status", "2")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            Log.i(TAG, "onEvent: vlaue isnt empty");
                            List<DocumentSnapshot> documents = value.getDocuments();
                            eventDTOS = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : documents) {
                                if (documentSnapshot.exists()) {
                                    Log.e(TAG, "onEvent: wadaa");
                                    String eventUniqueId = documentSnapshot.getString("eventUniqueId");
                                    String name = documentSnapshot.getString("name");
                                    String description = documentSnapshot.getString("description");
                                    String date = documentSnapshot.getString("date");
                                    String time = documentSnapshot.getString("time");
                                    String city = documentSnapshot.getString("city");
                                    String imagePath = documentSnapshot.getString("imagePath");


                                    EventDTO eventDTO = new EventDTO();
                                    eventDTO.setEventName(name);
                                    eventDTO.setEventUniqueId(eventUniqueId);
                                    eventDTO.setEventDescription(description);
                                    eventDTO.setEventDate(date);
                                    eventDTO.setEventTime(time);
                                    eventDTO.setEventLocation(city);
                                    eventDTO.setImageUrl(imagePath);

                                    eventDTO.setCategoryName("laa");
                                    //retrive ticket price from firebase

                                    firestore.collection("ticketTypes")
                                            .whereEqualTo("eventUniqueId", eventUniqueId)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                List<Double> prices = new ArrayList<>();
                                                Log.e(TAG, "onEvent:got ticket types");
                                                if (!queryDocumentSnapshots.isEmpty()) {

                                                    for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {

                                                        double price = documentSnapshot1.getDouble("price");
                                                        prices.add(price);
                                                    }
                                                } else {
                                                    Log.i(TAG, "onEvent error");
                                                }


                                                if (!prices.isEmpty()) {

                                                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                                    decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

                                                    double leastPrice = Collections.min(prices);
                                                    double highestPrice = Collections.max(prices);

                                                    String formattedLeastPrice = decimalFormat.format(leastPrice);
                                                    String formattedHighestPrice = decimalFormat.format(highestPrice);

                                                    eventDTO.setTicketPrice("Rs." + formattedLeastPrice + " - " + "Rs." + formattedHighestPrice);
                                                    eventDTOS.add(eventDTO);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(e1 -> Log.e(TAG, "Failed to fetch ticket types: " + e1.getMessage()));
                                } else {
                                    Log.i(TAG, "onEvent: not exist");
                                }


                            }

                            adapter = new RecyclerView.Adapter() {
                                @NonNull
                                @Override
                                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                    View eventView = inflater.inflate(R.layout.rejected_event_single_view, parent, false);
                                    return new RVH(eventView);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                                    RVH vh = (RVH) holder;

                                    vh.eventName.setText(eventDTOS.get(position).getEventName());
                                    Picasso.get().load(eventDTOS.get(position).getImageUrl()).into(vh.eventImage);
                                    vh.date.setText(eventDTOS.get(position).getEventDate());
                                    vh.time.setText(eventDTOS.get(position).getEventTime());
                                    vh.location.setText(eventDTOS.get(position).getEventLocation());
                                    vh.ticketPrice.setText(eventDTOS.get(position).getTicketPrice());
                                    approvebtn = vh.approve;
                                    approvebtn.setOnClickListener(view -> {
                                        EventStatusDTO eventStatusDTO = new EventStatusDTO();
                                        eventStatusDTO.setEventUniqueId(eventDTOS.get(position).getEventUniqueId());
                                        eventStatusDTO.setStatus("1");
                                        updateEventStatus(eventStatusDTO, position);
                                    });


                                }

                                @Override
                                public int getItemCount() {
                                    return eventDTOS.size();
                                }
                            };

                            RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRejectedEventsView);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);

                        }
                    }
                });



    }

    private void updateEventStatus(EventStatusDTO eventStatusDTO, int position) {
        Log.i(TAG, "Call updateEventStatus method");
        Log.i(TAG, eventStatusDTO.getEventUniqueId());
        firestore.collection("eventStatus").whereEqualTo("eventUniqueId", eventStatusDTO.getEventUniqueId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        QuerySnapshot result = task.getResult();

                        if (result != null && !result.isEmpty()) {

                            DocumentSnapshot documentSnapshot = result.getDocuments().get(0);
                            String status = documentSnapshot.getString("status");
                            if (status.equals("2")) {

                                Log.d(TAG, "Selected event status is 2");
                                String documentSnapshotId = documentSnapshot.getId();
                                firestore.collection("eventStatus")
                                        .document(documentSnapshotId)
                                        .update("status", "1")
                                        .addOnSuccessListener(aVoid -> {
                                            approvebtn.setText("Approved");
                                            approvebtn.setBackgroundColor(1);
                                            adapter.notifyItemChanged(position);
                                            Log.i(TAG, "Event Status Updated to 1");
                                        }).addOnFailureListener(e -> {
                                            Log.e(TAG, "Event Status Update fail");
                                        });
                            }
                        } else {
                            Log.e(TAG, "Result is Empty");
                            Log.i(TAG, "Add event Status");
                            firestore.collection("eventStatus").add(eventStatusDTO)
                                    .addOnCompleteListener(task1 -> {

                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, e.getMessage());
                                    });
                        }
                    } else {
                        Log.e(TAG, "Task Unsuccessfull");
                    }
                }).addOnFailureListener(e -> {
                    Log.i(TAG, e.getMessage());
                });
    }
}


class RVH extends RecyclerView.ViewHolder {

    ImageView eventImage;
    TextView eventName, date, time, location, ticketPrice;
    Button approve;

    public RVH(@NonNull View itemView) {
        super(itemView);
        eventImage = itemView.findViewById(R.id.eventImage);
        eventName = itemView.findViewById(R.id.eventName);
        date = itemView.findViewById(R.id.eventDate);
        time = itemView.findViewById(R.id.eventTime);
        location = itemView.findViewById(R.id.eventLocation);
        ticketPrice = itemView.findViewById(R.id.eventTicketPrice);
        approve = itemView.findViewById(R.id.btnApprove);


    }

}