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


    public static Queue<byte[]> getMockReqBytes(byte[] inputCmd){

        String cmdStr = new String(inputCmd);
        Queue<byte[]> result = null;
        switch (cmdStr){
            case "01":{
                result = getConh();
            }
        }
        return result;
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



}
