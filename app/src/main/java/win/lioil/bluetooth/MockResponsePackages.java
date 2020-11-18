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

    private static Queue<byte[]> getConhRsp(){
        String json = "{" +
                "\"m\": \"conh\"," +
                "\"r\": {}," +
                "\"e\":0" +
                "}";

       return SplitPackage.splitByte(json.getBytes());
    }


//------------------以下方法和逻辑无关---------------------------------------//
    //循环他测试内存溢出
    public static void receive2Package() {

        //组织测试数据1
        String json = "{\\" +
                "    \"employees\": [\\" +
                "        {\\" +
                "            \"firstName\": \"Bill\",\\" +
                "            \"lastName\": \"Gates\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"George\",\\" +
                "            \"lastName\": \"Bush\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"Thomas\",\\" +
                "            \"lastName\": \"Carter\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"Bill\",\\" +
                "            \"lastName\": \"Gates\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"George\",\\" +
                "            \"lastName\": \"Bush\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"Thomas\",\\" +
                "            \"lastName\": \"Carter\"\\" +
                "        }\\" +
                "    ]\\" +
                "}";

        json = json.trim().replace(" ","");
        json = json.trim().replace("\\","");
        Queue<byte[]> bytes = SplitPackage.splitByte(json.getBytes());

        int packageCount = bytes.size();
        if (packageCount>1){
            for (int index = 0;index<packageCount;index++){
                byte[] peekByte = bytes.poll();
                MergePackage.getInstance().appendPackage(peekByte);
            }
        }

        String finalJson = MergePackage.getInstance().exportToJson();


        //组织测试数据2，大于15个包儿了，出错了
        String json2 = "{\\" +
                "    \"employees2\": [\\" +
                "        {\\" +
                "            \"skn\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"Gates\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"Bush\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"Carter\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"Gates\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"Bush\"\\" +
                "        },\\" +
                "        {\\" +
                "            \"firstName\": \"123456789012345678901234\",\\" +
                "            \"lastName\": \"123456789012345678901234\"\\" +
                "        }\\" +
                "    ]\\" +
                "}";

        json2 = json2.trim().replace(" ","");
        Queue<byte[]> bytes2 = SplitPackage.splitByte(json2.getBytes());

        int packageCount2 = bytes2.size();
        if (packageCount2>1){
            for (int index = 0;index<packageCount2;index++){
                byte[] peekByte = bytes2.poll();
                MergePackage.getInstance().appendPackage(peekByte);
            }
        }

        String finalJson2 = MergePackage.getInstance().exportToJson();


    }


}
