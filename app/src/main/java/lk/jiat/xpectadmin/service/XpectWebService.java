package lk.jiat.xpectadmin.service;

import java.util.ArrayList;

import lk.jiat.xpectadmin.dto.EventDTO;
import lk.jiat.xpectadmin.dto.EventsDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface XpectWebService {
    @GET("event/getallevent")
    Call<ArrayList<EventDTO>> getAllEvent();

    @GET("event/geteventbyuniqueid1/{uniqueID}")
    Call<EventDTO> getEventByUniqueId(@Path("uniqueID") String uniqueID);



}
