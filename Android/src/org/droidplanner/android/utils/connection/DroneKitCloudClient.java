package org.droidplanner.android.utils.connection;

import android.util.Log;

import com.google.gson.JsonParseException;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import io.swagger.client.ApiException;
import io.swagger.client.JsonUtil;
import io.swagger.client.Pair;
import io.swagger.client.model.CreateMission;
import io.swagger.client.model.CreateUser;
import io.swagger.client.model.CreateVehicle;
import io.swagger.client.model.LogMessage;
import io.swagger.client.model.LoginPassword;
import io.swagger.client.model.Media;
import io.swagger.client.model.Mission;
import io.swagger.client.model.Recap;
import io.swagger.client.model.RecapAuth;
import io.swagger.client.model.RecapProperties;
import io.swagger.client.model.RecapPropertiesAll;
import io.swagger.client.model.RecapResult;
import io.swagger.client.model.SceneResult;
import io.swagger.client.model.Token;
import io.swagger.client.model.UpdateUser;
import io.swagger.client.model.User;
import io.swagger.client.model.Vehicle;
import timber.log.Timber;

/**
 * Created by chavi on 10/21/15.
 */
public class DroneKitCloudClient {
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String basePath = "http://dronekitcloudv2staging-1257415351.us-east-1.elb.amazonaws.com/api/v2";
    private static final String TOKEN_KEY = "Token";
    private static final String API_KEY = "apiKey";

    private OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            HostnameVerifier hv =
                HttpsURLConnection.getDefaultHostnameVerifier();
            return hv.verify("3drobotics.com", session);
        }
    };

    public DroneKitCloudClient() {
        client.setConnectTimeout(100, TimeUnit.SECONDS);
        client.setReadTimeout(100, TimeUnit.SECONDS);
    }


    public class NullX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
     * Get the log records from the given mediaId, which represent a dataflash log
     *
     * @param mediaId   The media identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @param start     Beginning relative timestamp to include in the results
     * @param end       End relative timestamp to include in the results
     * @param types     Message types to include in the results
     * @param fields    Fields in each message to incldue in the results
     * @param frequency The minimum time between messages of each type in seconds. For instance, if the frequency is 60 then only one event per minute per log type will be returned.
     * @return LogMessage
     */
    public LogMessage actionsLogreaderMediaIdGet(String mediaId, String apiKey, String token, Integer start, Integer end, List<String> types, List<String> fields, Integer frequency) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'mediaId' is set
        if (mediaId == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaId' when calling actionsLogreaderMediaIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsLogreaderMediaIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsLogreaderMediaIdGet");
        }


        // create path and map variables
        String path = "/actions/logreader/{mediaId}".replaceAll("\\{" + "mediaId" + "\\}", escapeString(mediaId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));
        queryParams.addAll(parameterToPairs("", "start", start));
        queryParams.addAll(parameterToPairs("", "end", end));
        queryParams.addAll(parameterToPairs("multi", "types", types));
        queryParams.addAll(parameterToPairs("multi", "fields", fields));
        queryParams.addAll(parameterToPairs("", "frequency", frequency));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (LogMessage) deserialize(response, "", LogMessage.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Create new recap job
     *
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body
     * @return RecapResult
     */
    public RecapResult actionsRecapPost(String apiKey, String token, Recap body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapPost");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling actionsRecapPost");
        }

        // create path and map variables
        String path = "/actions/recap";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (RecapResult) deserialize(response, "", RecapResult.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return RecapAuth
     */
    public RecapAuth actionsRecapAuthPost(String apiKey, String token) throws ApiException {
        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapAuthPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapAuthPost");
        }


        // create path and map variables
        String path = "/actions/recap/auth";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        // header params
        Map<String, String> headerParams = new HashMap<>();

        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        headerParams.put(TOKEN_KEY, parameterToString(token));

        HttpUrl httpUrl = createUrl(basePath, path, queryParams);

        headerParams.put("Accept", "application/json");
        headerParams.put("Content-Type", "application/json");
        Headers.Builder headersBuilder = new Headers.Builder();
        for (String key : headerParams.keySet()) {
            headersBuilder.add(key, headerParams.get(key));
        }

        try {
            JSONObject body = new JSONObject();
            body.put("param", basePath + "/actions/recap/auth/callback?token=" + token);
            String response = authRecap(httpUrl, headersBuilder.build(), token, body);
            if (response != null) {
                return (RecapAuth) deserialize(response, "", RecapAuth.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (IOException e) {
            throw new ApiException(500, e.getMessage());
        } catch (JSONException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body   Send the verifier token to Recap.
     * @return void
     */
    public void actionsRecapAuthCallbackPost(String apiKey, String token, Object body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapAuthCallbackPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapAuthCallbackPost");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling actionsRecapAuthCallbackPost");
        }


        // create path and map variables
        String path = "/actions/recap/auth/callback";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return;
            } else {
                return;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return User
     */
    public User actionsRecapAuthLogoutPost(String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapAuthLogoutPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapAuthLogoutPost");
        }


        // create path and map variables
        String path = "/actions/recap/auth/logout";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        HttpUrl httpUrl = createUrl(basePath, path, queryParams);

        headerParams.put("Accept", "application/json");
        headerParams.put("Content-Type", "application/json");
        Headers.Builder headersBuilder = new Headers.Builder();
        for (String key : headerParams.keySet()) {
            headersBuilder.add(key, headerParams.get(key));
        }

        try {
            String response = logoutRecap(httpUrl, headersBuilder.build());
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get recap job
     *
     * @param recapId The recap identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return RecapResult
     */
    public RecapResult actionsRecapRecapIdGet(String recapId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'recapId' is set
        if (recapId == null) {
            throw new ApiException(400, "Missing the required parameter 'recapId' when calling actionsRecapRecapIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapRecapIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapRecapIdGet");
        }


        // create path and map variables
        String path = "/actions/recap/{recapId}".replaceAll("\\{" + "recapId" + "\\}", escapeString(recapId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (RecapResult) deserialize(response, "", RecapResult.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get the progress &amp; processing time of a recap job
     *
     * @param recapId The recap identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return RecapProperties
     */
    public RecapProperties actionsRecapRecapIdPropertiesGet(String recapId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'recapId' is set
        if (recapId == null) {
            throw new ApiException(400, "Missing the required parameter 'recapId' when calling actionsRecapRecapIdPropertiesGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapRecapIdPropertiesGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapRecapIdPropertiesGet");
        }


        // create path and map variables
        String path = "/actions/recap/{recapId}/properties".replaceAll("\\{" + "recapId" + "\\}", escapeString(recapId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (RecapProperties) deserialize(response, "", RecapProperties.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get all the properties associated with a recap job
     *
     * @param recapId The recap identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return RecapPropertiesAll
     */
    public RecapPropertiesAll actionsRecapRecapIdPropertiesAllGet(String recapId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'recapId' is set
        if (recapId == null) {
            throw new ApiException(400, "Missing the required parameter 'recapId' when calling actionsRecapRecapIdPropertiesAllGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapRecapIdPropertiesAllGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapRecapIdPropertiesAllGet");
        }


        // create path and map variables
        String path = "/actions/recap/{recapId}/properties/all".replaceAll("\\{" + "recapId" + "\\}", escapeString(recapId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));


        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (RecapPropertiesAll) deserialize(response, "", RecapPropertiesAll.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Check the result of the photoscene
     *
     * @param recapId The recap identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return SceneResult
     */
    public SceneResult actionsRecapRecapIdResultsGet(String recapId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'recapId' is set
        if (recapId == null) {
            throw new ApiException(400, "Missing the required parameter 'recapId' when calling actionsRecapRecapIdResultsGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling actionsRecapRecapIdResultsGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling actionsRecapRecapIdResultsGet");
        }


        // create path and map variables
        String path = "/actions/recap/{recapId}/results".replaceAll("\\{" + "recapId" + "\\}", escapeString(recapId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (SceneResult) deserialize(response, "", SceneResult.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get an authentication token to use. Add this to your header.
     *
     * @param apiKey The API key assigned to you
     * @param body   Log in a user
     * @return Token
     */
    public Token login(String apiKey, LoginPassword body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling login");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling login");
        }


        // create path and map variables
        String path = "/auth/signIn";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (Token) deserialize(response, "", Token.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get the media object with the given ID
     *
     * @param mediaId The media identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return Media
     */
    public Media mediaMediaIdGet(String mediaId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'mediaId' is set
        if (mediaId == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaId' when calling mediaMediaIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling mediaMediaIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling mediaMediaIdGet");
        }


        // create path and map variables
        String path = "/media/{mediaId}".replaceAll("\\{" + "mediaId" + "\\}", escapeString(mediaId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (Media) deserialize(response, "", Media.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Delete an existing media object
     *
     * @param mediaId The media identifier
     * @param apiKey  The API key assigned to you
     * @param token   auth token given by /auth/signIn
     * @return void
     */
    public void mediaMediaIdDelete(String mediaId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'mediaId' is set
        if (mediaId == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaId' when calling mediaMediaIdDelete");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling mediaMediaIdDelete");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling mediaMediaIdDelete");
        }


        // create path and map variables
        String path = "/media/{mediaId}".replaceAll("\\{" + "mediaId" + "\\}", escapeString(mediaId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "DELETE", queryParams, postBody, headerParams);
            if (response != null) {
                return;
            } else {
                return;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Create a new mission
     *
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body
     * @return Mission
     */
    public Mission missionsPost(String apiKey, String token, CreateMission body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling missionsPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling missionsPost");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling missionsPost");
        }

        // create path and map variables
        String path = "/missions";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        Log.d("CHAVI", "body: " + postBody.toString());

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (Mission) deserialize(response, "", Mission.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get the data for a mission. Returns a Mission object which contains the available resources for this mission.
     *
     * @param missionId The mission identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return Mission
     */
    public Mission missionsMissionIdGet(String missionId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'missionId' is set
        if (missionId == null) {
            throw new ApiException(400, "Missing the required parameter 'missionId' when calling missionsMissionIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling missionsMissionIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling missionsMissionIdGet");
        }

        // create path and map variables
        String path = "/missions/{missionId}".replaceAll("\\{" + "missionId" + "\\}", escapeString(missionId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (Mission) deserialize(response, "", Mission.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Delete an existing mission
     *
     * @param missionId The mission identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return void
     */
    public void missionsMissionIdDelete(String missionId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'missionId' is set
        if (missionId == null) {
            throw new ApiException(400, "Missing the required parameter 'missionId' when calling missionsMissionIdDelete");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling missionsMissionIdDelete");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling missionsMissionIdDelete");
        }


        // create path and map variables
        String path = "/missions/{missionId}".replaceAll("\\{" + "missionId" + "\\}", escapeString(missionId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "DELETE", queryParams, postBody, headerParams);
            if (response != null) {
                return;
            } else {
                return;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get a list of media from the mission
     *
     * @param missionId The mission identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return List<Media>
     */
    public List<Media> missionsMissionIdMediaGet(String missionId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'missionId' is set
        if (missionId == null) {
            throw new ApiException(400, "Missing the required parameter 'missionId' when calling missionsMissionIdMediaGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling missionsMissionIdMediaGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling missionsMissionIdMediaGet");
        }


        // create path and map variables
        String path = "/missions/{missionId}/media".replaceAll("\\{" + "missionId" + "\\}", escapeString(missionId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (List<Media>) deserialize(response, "array", Media.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Add a new media object to the mission
     *
     * @param missionId The mission identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @param file
     * @param mediaType Type of the media. Ex: image/jpeg, video/*
     * @return Media
     */
    public Media missionsMissionIdMediaPost(String missionId, String apiKey, String token, File file, String mediaType) throws ApiException {
        RequestBody postBody = null;

        // verify the required parameter 'missionId' is set
        if (missionId == null) {
            throw new ApiException(400, "Missing the required parameter 'missionId' when calling missionsMissionIdMediaPost");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling missionsMissionIdMediaPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling missionsMissionIdMediaPost");
        }

        // verify the required parameter 'file' is set
        if (file == null) {
            throw new ApiException(400, "Missing the required parameter 'file' when calling missionsMissionIdMediaPost");
        }

        // verify the required parameter 'mediaType' is set
        if (mediaType == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaType' when calling missionsMissionIdMediaPost");
        }

        postBody = new MultipartBuilder()
            .type(MultipartBuilder.FORM)
            .addFormDataPart("file", file.getName(),
                RequestBody.create(MediaType.parse(mediaType), file))
            .build();

        // create path and map variables
        String path = "/missions/{missionId}/media".replaceAll("\\{" + "missionId" + "\\}", escapeString(missionId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));
        headerParams.put("mediaType", parameterToString(mediaType));

        try {
            String response = postMedia(basePath, path, queryParams, postBody, headerParams);
            if (response != null) {
                return (Media) deserialize(response, "", Media.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Create a new user
     *
     * @param apiKey The API key assigned to you
     * @param body   Create User object
     * @return User
     */
    public User createUser(String apiKey, CreateUser body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling createUser");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling createUser");
        }


        // create path and map variables
        String path = "/users";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get currently logged in user
     *
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return User
     */
    public User usersMeGet(String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersMeGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersMeGet");
        }


        // create path and map variables
        String path = "/users/me";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));


        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Update current user. All fields are optional.
     *
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body   Fields to update user, all params are optional
     * @return User
     */
    public User usersMePost(String apiKey, String token, UpdateUser body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersMePost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersMePost");
        }


        // create path and map variables
        String path = "/users/me";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Gets a `User` object.
     *
     * @param userId The user identifier
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return User
     */
    public User usersUserIdGet(String userId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling usersUserIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersUserIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersUserIdGet");
        }


        // create path and map variables
        String path = "/users/{userId}".replaceAll("\\{" + "userId" + "\\}", escapeString(userId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Update a user. All fields are optional.
     *
     * @param userId The user identifier
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body   Fields to update user, all params are optional
     * @return User
     */
    public User usersUserIdPost(String userId, String apiKey, String token, UpdateUser body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling usersUserIdPost");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersUserIdPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersUserIdPost");
        }

        // create path and map variables
        String path = "/users/{userId}".replaceAll("\\{" + "userId" + "\\}", escapeString(userId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (User) deserialize(response, "", User.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Delete a user
     *
     * @param userId The user identifier
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return void
     */
    public void usersUserIdDelete(String userId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling usersUserIdDelete");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersUserIdDelete");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersUserIdDelete");
        }


        // create path and map variables
        String path = "/users/{userId}".replaceAll("\\{" + "userId" + "\\}", escapeString(userId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));


        try {
            String response = invokeAPI(basePath, path, "DELETE", queryParams, postBody, headerParams);
            if (response != null) {
                return;
            } else {
                return;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get a list of missions for a user
     *
     * @param userId The user identifier
     * @param token  auth token given by /auth/signIn
     * @return List<Mission>
     */
    public List<Mission> usersUserIdMissionsGet(String userId, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling usersUserIdMissionsGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersUserIdMissionsGet");
        }


        // create path and map variables
        String path = "/users/{userId}/missions".replaceAll("\\{" + "userId" + "\\}", escapeString(userId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (List<Mission>) deserialize(response, "array", Mission.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get a list of vehicles owned by the user
     *
     * @param userId The user identifier
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @return List<Vehicle>
     */
    public List<Vehicle> usersUserIdVehiclesGet(String userId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling usersUserIdVehiclesGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling usersUserIdVehiclesGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling usersUserIdVehiclesGet");
        }

        // create path and map variables
        String path = "/users/{userId}/vehicles".replaceAll("\\{" + "userId" + "\\}", escapeString(userId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (List<Vehicle>) deserialize(response, "array", Vehicle.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Create a new vehicle for a user or org
     *
     * @param apiKey The API key assigned to you
     * @param token  auth token given by /auth/signIn
     * @param body
     * @return Vehicle
     */
    public Vehicle vehiclesPost(String apiKey, String token, CreateVehicle body) throws ApiException {
        Object postBody = body;

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling vehiclesPost");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling vehiclesPost");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling vehiclesPost");
        }


        // create path and map variables
        String path = "/vehicles";

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "POST", queryParams, postBody, headerParams);
            if (response != null) {
                return (Vehicle) deserialize(response, "", Vehicle.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get a specific vehicle
     *
     * @param vehicleId The vehicle identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return Vehicle
     */
    public Vehicle vehiclesVehicleIdGet(String vehicleId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'vehicleId' is set
        if (vehicleId == null) {
            throw new ApiException(400, "Missing the required parameter 'vehicleId' when calling vehiclesVehicleIdGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling vehiclesVehicleIdGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling vehiclesVehicleIdGet");
        }


        // create path and map variables
        String path = "/vehicles/{vehicleId}".replaceAll("\\{" + "vehicleId" + "\\}", escapeString(vehicleId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (Vehicle) deserialize(response, "", Vehicle.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Delete an existing vehicle
     *
     * @param vehicleId The vehicle identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return void
     */
    public void vehiclesVehicleIdDelete(String vehicleId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'vehicleId' is set
        if (vehicleId == null) {
            throw new ApiException(400, "Missing the required parameter 'vehicleId' when calling vehiclesVehicleIdDelete");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling vehiclesVehicleIdDelete");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling vehiclesVehicleIdDelete");
        }


        // create path and map variables
        String path = "/vehicles/{vehicleId}".replaceAll("\\{" + "vehicleId" + "\\}", escapeString(vehicleId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "DELETE", queryParams, postBody, headerParams);
            if (response != null) {
                return;
            } else {
                return;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    /**
     * Get a list of missions flown by a vehicle
     *
     * @param vehicleId The vehicle identifier
     * @param apiKey    The API key assigned to you
     * @param token     auth token given by /auth/signIn
     * @return List<Mission>
     */
    public List<Mission> vehiclesVehicleIdMissionsGet(String vehicleId, String apiKey, String token) throws ApiException {
        Object postBody = null;

        // verify the required parameter 'vehicleId' is set
        if (vehicleId == null) {
            throw new ApiException(400, "Missing the required parameter 'vehicleId' when calling vehiclesVehicleIdMissionsGet");
        }

        // verify the required parameter 'apiKey' is set
        if (apiKey == null) {
            throw new ApiException(400, "Missing the required parameter 'apiKey' when calling vehiclesVehicleIdMissionsGet");
        }

        // verify the required parameter 'token' is set
        if (token == null) {
            throw new ApiException(400, "Missing the required parameter 'token' when calling vehiclesVehicleIdMissionsGet");
        }


        // create path and map variables
        String path = "/vehicles/{vehicleId}/missions".replaceAll("\\{" + "vehicleId" + "\\}", escapeString(vehicleId.toString()));

        // query params
        List<Pair> queryParams = new ArrayList<>();
        queryParams.addAll(parameterToPairs("", API_KEY, apiKey));

        // header params
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(TOKEN_KEY, parameterToString(token));

        try {
            String response = invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams);
            if (response != null) {
                return (List<Mission>) deserialize(response, "array", Mission.class);
            } else {
                return null;
            }
        } catch (ApiException ex) {
            throw ex;
        }
    }

    public static String formatDateTime(Date datetime) {
        return DATE_TIME_FORMAT.format(datetime);
    }

    public static String parameterToString(Object param) {
        if (param == null) {
            return "";
        } else if (param instanceof Date) {
            return formatDateTime((Date) param);
        } else if (param instanceof Collection) {
            StringBuilder b = new StringBuilder();
            for (Object o : (Collection) param) {
                if (b.length() > 0) {
                    b.append(",");
                }
                b.append(String.valueOf(o));
            }
            return b.toString();
        } else {
            return String.valueOf(param);
        }
    }

    /*
      Format to {@code Pair} objects.
    */
    public static List<Pair> parameterToPairs(String collectionFormat, String name, Object value) {
        List<Pair> params = new ArrayList<>();

        // preconditions
        if (name == null || name.isEmpty() || value == null) {
            return params;
        }

        Collection valueCollection = null;
        if (value instanceof Collection) {
            valueCollection = (Collection) value;
        } else {
            params.add(new Pair(name, parameterToString(value)));
            return params;
        }

        if (valueCollection.isEmpty()) {
            return params;
        }

        // get the collection format
        collectionFormat = (collectionFormat == null || collectionFormat.isEmpty() ? "csv" : collectionFormat); // default: csv

        // create the params based on the collection format
        if (collectionFormat.equals("multi")) {
            for (Object item : valueCollection) {
                params.add(new Pair(name, parameterToString(item)));
            }

            return params;
        }

        String delimiter = ",";

        if (collectionFormat.equals("csv")) {
            delimiter = ",";
        } else if (collectionFormat.equals("ssv")) {
            delimiter = " ";
        } else if (collectionFormat.equals("tsv")) {
            delimiter = "\t";
        } else if (collectionFormat.equals("pipes")) {
            delimiter = "|";
        }

        StringBuilder sb = new StringBuilder();
        for (Object item : valueCollection) {
            sb.append(delimiter);
            sb.append(parameterToString(item));
        }

        params.add(new Pair(name, sb.substring(1)));

        return params;
    }

    public String escapeString(String str) {
        return str;
    }

    public static Object deserialize(String json, String containerType, Class cls) throws ApiException {
        try {
            if ("list".equalsIgnoreCase(containerType) || "array".equalsIgnoreCase(containerType)) {
                return JsonUtil.deserializeToList(json, cls);
            } else if (String.class.equals(cls)) {
                if (json != null && json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
                    return json.substring(1, json.length() - 1);
                } else {
                    return json;
                }
            } else {
                return JsonUtil.deserializeToObject(json, cls);
            }
        } catch (JsonParseException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public static String serialize(Object obj) throws ApiException {
        try {
            if (obj != null) {
                return JsonUtil.serialize(obj);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public String invokeAPI(String host, String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams) throws ApiException {
        HttpUrl httpUrl = createUrl(host, path, queryParams);
        Log.d("CHAVI", "url: " + httpUrl);

        //headerParams.put("Accept", "application/json");
        headerParams.put("Content-Type", "application/json");
        Headers.Builder headersBuilder = new Headers.Builder();
        for (String key : headerParams.keySet()) {
            headersBuilder.add(key, headerParams.get(key));
        }

        Response response = null;

        try {
            if ("GET".equals(method)) {
                response = getRequest(httpUrl, headersBuilder.build());
            } else if ("POST".equals(method)) {
                response = postRequest(httpUrl, headersBuilder.build(), body);
            } else if ("PUT".equals(method)) {
                response = putRequest(httpUrl, headersBuilder.build(), body);
            } else if ("DELETE".equals(method)) {
                response = deleteRequest(httpUrl, headersBuilder.build());
            } else if ("PATCH".equals(method)) {
                response = patchRequest(httpUrl, headersBuilder.build(), body);
            }

            String responseBody = response.body().string();

            Log.d("CHAVI", "response code: " + response.code() + " response " + responseBody);
            int code = response.code();
            String responseString = null;
            if (code == 204) {
                responseString = "";
                return responseString;
            } else if (code >= 200 && code < 300) {
                return responseBody;
            } else {
                if (response.message() != null) {
                    responseString = response.message();
                } else {
                    responseString = "no data";
                }
            }
            throw new ApiException(code, responseString);
        } catch (IOException e) {
            Timber.e("Failed to invoke api: " + path + " " + e);
            throw new ApiException(500, e.getMessage());
        }
    }

    public String postMedia(String host, String path, List<Pair> queryParams, RequestBody requestBody, Map<String, String> headerParams) throws ApiException {
        HttpUrl httpUrl = createUrl(host, path, queryParams);

        //headerParams.put("Content-Type", "application/json");
        Headers.Builder headersBuilder = new Headers.Builder();
        for (String key : headerParams.keySet()) {
            headersBuilder.add(key, headerParams.get(key));
        }

        try {

            Request request = new Request.Builder()
                .url(httpUrl)
                .headers(headersBuilder.build())
                .post(requestBody)
                .build();

            Log.d("CHAVI", "request: " + request.urlString() + " body: " + request.body().toString());
            Response response = client.newCall(request).execute();

            String responseBody = response.body().string();

            int code = response.code();
            String responseString;
            if (code == 204) {
                responseString = "";
                return responseString;
            } else if (code >= 200 && code < 300) {
                return responseBody;
            } else {
                if (response.message() != null) {
                    responseString = response.message();
                } else {
                    responseString = "no data";
                }
            }
            throw new ApiException(code, responseString);
        } catch (IOException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public Response getRequest(HttpUrl url, Headers headers) throws IOException {

        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public Response postRequest(HttpUrl url, Headers headers, Object body) throws IOException, ApiException {

        String serializedBody = serialize(body);
        RequestBody requestBody = RequestBody.create(JSON, serializedBody);
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build();

        Log.d("CHAVI", "request: " + request.urlString() + " body: " + request.body().toString() + " serializedBody: " + serializedBody);
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response putRequest(HttpUrl url, Headers headers, Object body) throws IOException, ApiException {
        RequestBody requestBody = RequestBody.create(JSON, serialize(body));
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .put(requestBody)
            .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response deleteRequest(HttpUrl url, Headers headers) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .delete()
            .headers(headers)
            .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public Response patchRequest(HttpUrl url, Headers headers, Object body) throws IOException, ApiException {
        RequestBody requestBody = RequestBody.create(JSON, serialize(body));
        Request request = new Request.Builder()
            .url(url)
            .patch(requestBody)
            .headers(headers)
            .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    public String authRecap(HttpUrl url, Headers headers, String token, JSONObject requestBodyJson) throws IOException, ApiException {
        RequestBody requestBody = RequestBody.create(JSON, requestBodyJson.toString());
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build();

        Response response = client.newCall(request).execute();

        String responseBody = response.body().string();

        Log.d("CHAVI", "response code: " + response.code() + " response " + responseBody);
        int code = response.code();
        String responseString = null;
        if (code == 204) {
            responseString = "";
            return responseString;
        } else if (code >= 200 && code < 300) {
            return responseBody;
        } else {
            if (response.message() != null) {
                responseString = response.message();
            } else {
                responseString = "no data";
            }
        }
        throw new ApiException(code, responseString);
    }

    public String logoutRecap(HttpUrl url, Headers headers) throws ApiException {
        JSONObject jsonObject = new JSONObject();
        RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build();

        try {
            Response response = client.newCall(request).execute();


            String responseBody = response.body().string();

            Log.d("CHAVI", "response code: " + response.code() + " response " + responseBody);
            int code = response.code();
            String responseString = null;
            if (code == 204) {
                responseString = "";
                return responseString;
            } else if (code >= 200 && code < 300) {
                return responseBody;
            } else {
                if (response.message() != null) {
                    responseString = response.message();
                } else {
                    responseString = "no data";
                }
            }
            throw new ApiException(code, responseString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ApiException();

    }

    public HttpUrl createUrl(String host, String path, List<Pair> queryParams) {

        String url = host + path;
        HttpUrl.Builder httpUrl = HttpUrl.parse(url).newBuilder();

        for (Pair queryParam : queryParams) {
            httpUrl.addQueryParameter(queryParam.getName(), queryParam.getValue());
        }

        return httpUrl.build();
    }
}