package com.paybus.api;

import com.paybus.models.AddCardRequest;
import com.paybus.models.BusRoute;
import com.paybus.models.CardResponse;
import com.paybus.models.PaymentRequest;
import com.paybus.models.SendSMSRequest;
import com.paybus.models.TokenResponse;
import com.paybus.models.TransactionResponse;
import com.paybus.models.VerifySMSRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PayBusApi {

    @POST("api/auth/send-code")
    Call<SendCodeResponse> sendCode(@Body SendSMSRequest request);

    @POST("api/auth/send-telegram-code")
    Call<TelegramCodeResponse> sendTelegramCode(@Body SendSMSRequest request);

    @POST("api/auth/verify-code")
    Call<TokenResponse> verifyCode(@Body VerifySMSRequest request);

    @GET("api/auth/profile")
    Call<TokenResponse.UserResponse> getProfile(@Header("Authorization") String token);

    @PUT("api/auth/profile")
    Call<TokenResponse.UserResponse> updateProfile(@Header("Authorization") String token,
                                                    @Body UpdateProfileRequest request);

    @POST("api/payment/send-card-verify-code")
    Call<SendCodeResponse> sendCardVerifyCode(@Header("Authorization") String token, @Body AddCardRequest request);

    @POST("api/payment/verify-card-code")
    Call<CardResponse> verifyCardCode(@Header("Authorization") String token, @Body CardVerifyRequest request);

    @POST("api/payment/add-card")
    Call<CardResponse> addCard(@Header("Authorization") String token, @Body AddCardRequest request);

    @GET("api/payment/cards")
    Call<List<CardResponse>> getCards(@Header("Authorization") String token);

    @POST("api/payment/pay")
    Call<TransactionResponse> pay(@Header("Authorization") String token, @Body PaymentRequest request);

    @GET("api/payment/transactions")
    Call<List<TransactionResponse>> getTransactions(@Header("Authorization") String token);

    @GET("api/bus/routes")
    Call<List<BusRoute>> getBusRoutes();

    @GET("api/bus/nearby-stops")
    Call<NearbyStopsResponse> getNearbyStops(@Header("Authorization") String token,
                                              @Query("lat") double lat,
                                              @Query("lng") double lng);

    @GET("api/bus/nearby")
    Call<NearbyBusesResponse> getNearbyBuses(@Header("Authorization") String token,
                                              @Query("lat") double lat,
                                              @Query("lng") double lng);

    @GET("api/bus/arrivals/{stop_id}")
    Call<BusArrivalsResponse> getStopArrivals(@Header("Authorization") String token,
                                               @Path("stop_id") String stopId);

    @GET("api/bus/route-schedule/{route_id}")
    Call<RouteScheduleResponse> getRouteSchedule(@Header("Authorization") String token,
                                                  @Path("route_id") String routeId);

    @GET("api/bus/stop-location/{stop_id}")
    Call<StopLocationResponse> getStopLocation(@Header("Authorization") String token,
                                                @Path("stop_id") String stopId);

    class SendCodeResponse {
        public String message;
        public String code;
    }

    class TelegramCodeResponse {
        public String message;
        public boolean sent;
    }

    class CardVerifyRequest {
        public String code;
    }

    class UpdateProfileRequest {
        public String full_name;
    }

    class NearbyStopsResponse {
        public List<BusStop> stops;
    }

    class BusStop {
        public String id;
        public String name;
        public double lat;
        public double lng;
        public String address;
    }

    class BusArrivalsResponse {
        public String stop_id;
        public List<BusArrival> arrivals;
    }

    class BusArrival {
        public String route;
        public String destination;
        public int arrival_minutes;
    }

    class RouteScheduleResponse {
        public String route_id;
        public RouteSchedule schedule;
    }

    class RouteSchedule {
        public String name;
        public int price;
        public List<ScheduleStop> stops;
    }

    class ScheduleStop {
        public String stop_id;
        public String name;
        public String arrival_time;
        public double lat;
        public double lng;
    }

    class StopLocationResponse {
        public String id;
        public String name;
        public double lat;
        public double lng;
    }

    class NearbyBusesResponse {
        public String message;
        public List<BusInfo> buses;
    }

    class BusInfo {
        public String id;
        public String route;
        public double lat;
        public double lng;
        public double speed;
        public double direction;
        public String plate;
        public String busType;
    }
}
