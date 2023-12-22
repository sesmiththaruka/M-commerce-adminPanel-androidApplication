package lk.jiat.xpectadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lk.jiat.xpectadmin.MainActivity;
import lk.jiat.xpectadmin.R;
import lk.jiat.xpectadmin.dto.EventDTO;
import lk.jiat.xpectadmin.dto.EventStatusDTO;
import lk.jiat.xpectadmin.service.XpectWebService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PendingEventFragment extends Fragment {
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<EventDTO> eventDTOS;

    private RecyclerView.Adapter adapter;
    private Button approvebtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_pending_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: call");
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.1.112:8080/api/v1/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        XpectWebService xpectWebService = retrofit.create(XpectWebService.class);

        firestore.collection("eventEntity").whereEqualTo("status", "0")
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
//                            Log.i(TAG, String.valueOf(eventDTOS.size()));
//                            Log.i(TAG, "onEvent: "+ eventDTOS.get(0).getEventName());
                            adapter = new RecyclerView.Adapter() {
                                @NonNull
                                @Override
                                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                                    View eventView = inflater.inflate(R.layout.home_single_event_view_layout, parent, false);
                                    return new VH(eventView);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                                    VH vh = (VH) holder;

                                    vh.eventName.setText(eventDTOS.get(position).getEventName());
                                    Picasso.get().load(eventDTOS.get(position).getImageUrl()).into(vh.eventImage);
                                    vh.date.setText(eventDTOS.get(position).getEventDate());
                                    vh.time.setText(eventDTOS.get(position).getEventTime());
                                    vh.location.setText(eventDTOS.get(position).getEventLocation());
                                    vh.ticketPrice.setText(eventDTOS.get(position).getTicketPrice());
                                    approvebtn = vh.approve;
                                    approvebtn.setOnClickListener(view -> {
                                        Log.e(TAG, "YUHKIugb");
                                        EventStatusDTO eventStatusDTO = new EventStatusDTO();
                                        eventStatusDTO.setEventUniqueId(eventDTOS.get(position).getEventUniqueId());
                                        eventStatusDTO.setStatus("1");
                                        updateEventStatus(eventStatusDTO, position);
                                    });
                                    vh.reject.setOnClickListener(v -> {
                                        Log.e(TAG, "clicked");
                                        EventStatusDTO eventStatusDTO = new EventStatusDTO();
                                        eventStatusDTO.setEventUniqueId(eventDTOS.get(position).getEventUniqueId());
                                        eventStatusDTO.setStatus("2");
                                        updateEventStatus(eventStatusDTO, position);
                                    });

                                }

                                @Override
                                public int getItemCount() {
                                    return eventDTOS.size();
                                }
                            };

                            RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPendingEventsView);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.e(TAG, "onEvent: empty");
                        }
                    }
                });

//        firestore.collection("eventStatus").whereEqualTo("status", "0")
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (value != null) {
//
//                            List<DocumentSnapshot> documents = value.getDocuments();
//                            eventDTOS = new ArrayList<>();
//                            for (DocumentSnapshot documentSnapshot : documents) {
//                                if (documentSnapshot.exists()) {
//                                    String eventUniqueId = documentSnapshot.getString("eventUniqueId");
//                                    Log.i(TAG, "Got Event Unique ID:--" + eventUniqueId);
////                                    Call<EventDTO> callEventByUniqueId = xpectWebService.getEventByUniqueId(eventUniqueId);
////                                    callEventByUniqueId.enqueue(new Callback<EventDTO>() {
////                                        @Override
////                                        public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
////                                            if (response.isSuccessful()) {
////                                                Log.i(TAG, "Response is success");
////                                                eventDTO = response.body();
////
////                                                if (eventDTO != null) {
////                                                    Log.i(TAG, eventDTO.getEventName());
////                                                    //retrive img from firebase
////                                                    firestore.collection("eventImages")
////                                                            .whereEqualTo("eventId", eventUniqueId)
////                                                            .get()
////                                                            .addOnSuccessListener(queryDocumentSnapshots -> {
////
////                                                                if (!queryDocumentSnapshots.isEmpty()) {
////                                                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
////                                                                    String imageUrl = documentSnapshot.getString("imagePath");
////                                                                    eventDTO.setImageUrl(imageUrl);
////                                                                    Log.e(TAG, imageUrl);
////                                                                    adapter.notifyDataSetChanged();
////                                                                }
////                                                            }).addOnFailureListener(o -> Log.e(TAG, "Firestore data retrieval failed: " + o.getMessage()));
//////                                                   //retrive img from firebase
////                                                    //retrive ticket price from firebase
//////
////                                                    firestore.collection("ticketTypes")
////                                                            .whereEqualTo("eventUniqueId", eventUniqueId)
////                                                            .get()
////                                                            .addOnSuccessListener(queryDocumentSnapshots -> {
////                                                                List<Double> prices = new ArrayList<>();
////
////                                                                if (!queryDocumentSnapshots.isEmpty()) {
////
////                                                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
////
////                                                                        double price = documentSnapshot.getDouble("price");
////                                                                        prices.add(price);
////                                                                    }
////                                                                }
////
////
////                                                                if (!prices.isEmpty()) {
////
////                                                                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
////                                                                    decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
////
////                                                                    double leastPrice = Collections.min(prices);
////                                                                    double highestPrice = Collections.max(prices);
////
////                                                                    String formattedLeastPrice = decimalFormat.format(leastPrice);
////                                                                    String formattedHighestPrice = decimalFormat.format(highestPrice);
////
////                                                                    eventDTO.setTicketPrice("Rs." + formattedLeastPrice + " - " + "Rs." + formattedHighestPrice);
////
////                                                                }
////                                                            })
////                                                            .addOnFailureListener(e1 -> Log.e(TAG, "Failed to fetch ticket types: " + e1.getMessage()));
////
//////                                                  //retrive ticket price from firebase
//////                                                  retrive eventlocation from firebase
////                                                    firestore.collection("eventLocations")
////                                                            .whereEqualTo("eventId", eventUniqueId)
////                                                            .get()
////                                                            .addOnSuccessListener(queryDocumentSnapshots -> {
////
////                                                                if (!queryDocumentSnapshots.isEmpty()) {
////                                                                    Log.i(TAG, "Set loca");
////                                                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
////                                                                    String imageUrl = documentSnapshot.getString("city");
////                                                                    eventDTO.setEventLocation(imageUrl);
////
////                                                                }
////                                                            }).addOnFailureListener(o -> Log.e(TAG, "Firestore data retrieval failed: " + o.getMessage()));
//////                                                  //retrive eventlocation from firebase
////                                                    eventDTOS.add(eventDTO);
////                                                }
////
////                                            } else {
////                                                Log.e(TAG, "Response is not success");
////                                            }
////                                        }
////
////                                        @Override
////                                        public void onFailure(Call<EventDTO> call, Throwable t) {
////                                            Log.e(TAG, "Call Fail");
////                                        }
////                                    });
//                                }
//
//                            }
//
////                            adapter = new RecyclerView.Adapter() {
////                                @NonNull
////                                @Override
////                                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////
////                                    LayoutInflater inflater = LayoutInflater.from(getActivity());
////                                    View eventView = inflater.inflate(R.layout.home_single_event_view_layout, parent, false);
////                                    return new VH(eventView);
////                                }
////
////                                @Override
////                                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
////
////                                    VH vh = (VH) holder;
////
////                                    vh.eventName.setText(eventDTOS.get(position).getEventName());
////                                    Picasso.get().load(eventDTOS.get(position).getImageUrl()).into(vh.eventImage);
////                                    vh.date.setText(eventDTOS.get(position).getEventDate());
////                                    vh.time.setText(eventDTOS.get(position).getEventTime());
////                                    vh.location.setText(eventDTOS.get(position).getEventLocation());
////                                    vh.ticketPrice.setText(eventDTOS.get(position).getTicketPrice());
////                                    approvebtn = vh.approve;
////                                    approvebtn.setOnClickListener(view -> {
////                                        Log.e(TAG, "YUHKIugb");
////                                        EventStatusDTO eventStatusDTO = new EventStatusDTO();
////                                        eventStatusDTO.setEventUniqueId(eventDTOS.get(position).getEventUniqueId());
////                                        eventStatusDTO.setStatus("1");
////                                        updateEventStatus(eventStatusDTO, position);
////                                    });
////                                    vh.reject.setOnClickListener(v -> {
////
////                                    });
////
////                                }
////
////                                @Override
////                                public int getItemCount() {
////                                    return eventDTOS.size();
////                                }
////                            };
////
////                            RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPendingEventsView);
////                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
////                            recyclerView.setAdapter(adapter);
//
//                        }
//                    }
//                });


    }

    private void updateEventStatus(EventStatusDTO eventStatusDTO, int position) {
        Log.i(TAG, "Call updateEventStatus method");
        Log.i(TAG, eventStatusDTO.getEventUniqueId());
        CollectionReference eventCollection = FirebaseFirestore.getInstance().collection("eventEntity");
        eventCollection.whereEqualTo("eventUniqueId",eventStatusDTO.getEventUniqueId())
                        .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        for (QueryDocumentSnapshot document : task.getResult()){
                                            eventCollection.document(document.getId())

                                                    .update("status",eventStatusDTO.getStatus())
                                                    .addOnSuccessListener(unused -> {
                                                        if (eventStatusDTO.getStatus().equals("1")){
                                                            approvebtn.setText("Approved");
                                                            approvebtn.setBackgroundColor(1);
                                                            adapter.notifyItemChanged(position);
                                                            sendNotification(eventStatusDTO.getEventUniqueId(), "Your event is approved");
                                                        }else {
                                                            Log.i(TAG, "updateEventStatus: click rehected");
                                                            approvebtn.setText("Rejected");
                                                            approvebtn.setBackgroundColor(1);
                                                            adapter.notifyItemChanged(position);
                                                            sendNotification(eventStatusDTO.getEventUniqueId(), "Your event is rejected");
                                                        }


                                                    }).addOnFailureListener(e -> {
                                                        Log.e(TAG, "updateEventStatus: Event updated error" );
                                                    });
                                        }
                                    }
                                });

    }

    private void sendNotification(String eventUniqueId, String message) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.112:8080/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "Xpect admin");
            notificationObj.put("body", message);

            JSONObject dataObj = new JSONObject();
            dataObj.put("userId", firebaseAuth.getCurrentUser().getUid());

            jsonObject.put("notification", notificationObj);
            jsonObject.put("data", dataObj);
            jsonObject.put("to", "cxAEsRniR--0_mWzCZBakc:APA91bHEBsx2rx0QV-TmsiWrnYiZpUNRwsFbMkUnYsk5risxELIFfrJWKwAnSu0Fo84RNmWVrN3vbUxbin_zO9dlhi-7SqGaMUNzybBPG7yp0Rq31YExEHl5qWNfaW0qL8lSdNJHp-BR");
            callApi(jsonObject);

        } catch (Exception e) {

        }


    }

    private void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAs2uSUYA:APA91bFRW9YPI8vVHTxEDZ6N8oRR0unWsK6aF8LYVyyY-0QGXRXjAyiC_n3Xmfoh5aqc4Se5ZTT8745vGcWJF1P406g8CbPW5nES34MVXla5T3M8M9B8Jwz1GVM1cgQHP8QyztfBpPNa")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Notification Gone Error");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.i(TAG, "Notification Gone Success");
            }
        });
    }
}


class VH extends RecyclerView.ViewHolder {

    ImageView eventImage;
    TextView eventName, date, time, location, ticketPrice;
    Button approve, reject;

    public VH(@NonNull View itemView) {
        super(itemView);
        eventImage = itemView.findViewById(R.id.eventImage);
        eventName = itemView.findViewById(R.id.eventName);
        date = itemView.findViewById(R.id.eventDate);
        time = itemView.findViewById(R.id.eventTime);
        location = itemView.findViewById(R.id.eventLocation);
        ticketPrice = itemView.findViewById(R.id.eventTicketPrice);
        approve = itemView.findViewById(R.id.btnApprove);
        reject = itemView.findViewById(R.id.btnReject);

    }
}