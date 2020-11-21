package win.lioil.bluetooth;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MockResponsePackages {

    static String webAddress = "http://42.159.80.241:8888/app/v1/api/mock/mock/";

    public static Queue<byte[]> getMockRspBytes(String wholeJson) throws JSONException {


        JSONObject reqJson = new JSONObject(wholeJson);

        if(reqJson.get("test")!=null){
            String testFile = reqJson.getString("test");
            return getRspFromRemote(testFile);
        }

        return null;

    }

    public static String getMockRsp(String wholeJson) throws JSONException {


        JSONObject reqJson = new JSONObject(wholeJson);

        if(reqJson.names().get(0).toString() != "test"){
            String testFile = reqJson.getString("test");
            return getRspStrFromRemote(testFile);
        }

        return null;

    }

    private static Queue<byte[]> getRspFromRemote(String jsonFileName) {


        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(webAddress+jsonFileName).build();
        Call call=okHttpClient.newCall(request);

        try {
            String rspHttp = call.execute().body().string();
            rspHttp = rspHttp.replace(" ","");
            return SplitPackage.splitByte(rspHttp.getBytes());
        } catch (IOException e) {
            Log.e("Http",e.getMessage());
        }

        return null;


    }

    private static String getRspStrFromRemote(String jsonFileName) {


        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(webAddress+jsonFileName).build();
        Call call=okHttpClient.newCall(request);

        try {
            String rspHttp = call.execute().body().string();
            rspHttp = rspHttp.replace(" ","");
            return rspHttp;
        } catch (IOException e) {
            Log.e("Http",e.getMessage());
        }

        return null;


    }





}
