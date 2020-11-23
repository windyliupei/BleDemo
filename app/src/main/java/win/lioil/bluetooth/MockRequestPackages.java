package win.lioil.bluetooth;


import android.support.v4.os.IResultReceiver;

import java.util.Queue;

/*给Client 端用的
* 当客户端输入 01时，模拟：
*
* {
    "m": "conh",
    "p": {
        "t": ""
    }
}
* */
public class MockRequestPackages {


    public static Queue<byte[]> getMockReqBytes(byte[] inputFileName){

        String inputFileNameStr = new String(inputFileName);

        return getApiReq(inputFileNameStr);
    }

    private static Queue<byte[]> getConh(){
        String json = "{" +
                "\"m\": \"conh\"," +
                "\"p\": {" +
                "\"t\": \"\"" +
                "}" +
                "}";


        Queue<byte[]> splitByte = SplitPackage.splitByte(json.getBytes());

        return splitByte;
    }

    private static Queue<byte[]> getApiReq(String fileName){
        String json = "{\"test\":\""+fileName+"\"}";
        Queue<byte[]> splitByte = SplitPackage.splitByte(json.getBytes());
        return splitByte;
    }

    public static String generateBigBigData(){
       return  "{\n" +
                "    \"m\": [\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Gates\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Bush\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Carter\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Gates\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Bush\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"123456789012345678901234\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

    public static String generateBigData(){
        return  "{\n" +
                "    \"m\": [\n" +
                "        {\n" +
                "            \"firstName\": \"123456789012345678901234\",\n" +
                "            \"lastName\": \"Gates\"\n" +
                "        } "+
                "    ]\n" +
                "}";
    }



}
