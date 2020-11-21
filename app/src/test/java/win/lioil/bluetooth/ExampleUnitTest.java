package win.lioil.bluetooth;

import org.junit.Test;

import java.util.Queue;

import win.lioil.bluetooth.util.Util;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void receive2LittlePackage() {

        //组织测试数据1
        String json = "{" +
                "    \"m\": \"METHOD_NAME\"," +
                "    \"p\": {" +
                "        \"t\": \"\"" +
                "    }" +
                "}";

        json = json.trim().replace(" ","");
        Queue<byte[]> bytes = SplitPackage.splitByte(json.getBytes());

        int packageCount = bytes.size();
        if (packageCount>1){
            for (int index = 0;index<packageCount;index++){
                byte[] peekByte = bytes.poll();
                MergePackage.getInstance().appendPackage(peekByte);
            }
        }

        String finalJson = MergePackage.getInstance().exportToJson();
        assertEquals(finalJson, json);

        //组织测试数据2
        String json2 = "{" +
                "    \"m\": \"METHOD_NAME\"," +
                "    \"p\": {" +
                "        \"t\": \"\"" +
                "    }" +
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
        assertEquals(json2, finalJson2);


    }

    //每个JSON分包儿后大于10
    @Test
    public void receive2Package() {



        //组织测试数据2，大于15个包儿了，出错了
        String json2 = "{" +
                "    \"m\": [" +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"Gates\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"Bush\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"Carter\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"Gates\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"Bush\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }," +
                "        {" +
                "            \"firstName\": \"123456789012345678901234\"," +
                "            \"lastName\": \"123456789012345678901234\"" +
                "        }" +
                "    ]" +
                "}";

        json2 = json2.trim().replace(" ","");
        json2 = json2.trim().replace("\\","");
        json2 = json2.trim().replace("","");
        Queue<byte[]> bytes2 = SplitPackage.splitByte(json2.getBytes());

        int packageCount2 = bytes2.size();
        if (packageCount2>1){
            for (int index = 0;index<packageCount2;index++){
                byte[] peekByte = bytes2.poll();
                MergePackage.getInstance().appendPackage(peekByte);
            }
        }

        String finalJson2 = MergePackage.getInstance().exportToJson();
        assertEquals(json2, finalJson2);

    }


    @Test
    public void memberTest() {
        while (true){
            receive2Package();
        }
    }

    @Test
    public void cal() {
        SplitPackage.calHead(false,257,198);
    }

    @Test
    public void LoopCal() {

        boolean packageToggle = false;
        int pkgToggleCount = 1;

        for (int pkgWholeIndex = 1; pkgWholeIndex <= 257; pkgWholeIndex++) {

            if(pkgToggleCount>=127){
                packageToggle =!packageToggle;
                //每127包儿重新计数
                pkgToggleCount = 0;
            }

            SplitPackage.calHead(packageToggle,257,pkgWholeIndex);

            pkgToggleCount++;
        }
    }
    @Test
    public void packageRevers() {
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
        json2 = json2.trim().replace("\\","");
        Queue<byte[]> bytes2 = SplitPackage.splitByte(json2.getBytes());
    }

    @Test
    public void toPackageHead() {
        PackageHead packageHead = new PackageHead();
        packageHead.setAckR(true);
        packageHead.setPackageToggle(true);
        byte head = Util.getHead(packageHead);
        byte[] headBytes = {head};

        String hex = Util.bytesToHex(headBytes);


    }

    @Test
    public void toHex() {

        //1111111
        byte head = (byte) 0xff;
        PackageHead hex = Util.getPkgInfo(head);

        //01111111
        head = (byte) 0x7f;
        hex = Util.getPkgInfo(head);

        //10111111
        head = (byte) 0xbf;
        hex = Util.getPkgInfo(head);

        //11011111
        head = (byte) 0xdf;
        hex = Util.getPkgInfo(head);

        //11101111
        head = (byte) 0xef;
        hex = Util.getPkgInfo(head);

        //11110111
        head = (byte) 0xf7;
        hex = Util.getPkgInfo(head);

        //11111011
        head = (byte) 0xfb;
        hex = Util.getPkgInfo(head);

        //11111101
        head = (byte) 0xfd;
        hex = Util.getPkgInfo(head);

        //11111110
        head = (byte) 0xfe;
        hex = Util.getPkgInfo(head);
    }

    @Test
    public void getShu(){
        int[] indexAndCount = Util.getCountAndIndex(257, 127, 1);
        indexAndCount = Util.getCountAndIndex(257, 127, 12);
        indexAndCount = Util.getCountAndIndex(257, 127, 127);
        indexAndCount = Util.getCountAndIndex(257, 127, 130);
        indexAndCount = Util.getCountAndIndex(257, 127, 128);
        indexAndCount = Util.getCountAndIndex(257, 127, 254);
        indexAndCount = Util.getCountAndIndex(257, 127, 255);
        indexAndCount = Util.getCountAndIndex(257, 127, 256);
        indexAndCount = Util.getCountAndIndex(257, 127, 257);
    }

    @Test
    public void testHUbList(){

        String test = "{" +
                "    \"m\": \"hlst\", " +
                "    \"r\": {" +
                "        \"i\":\"49/50\", " +
                "        \"n\":\"123456789012345678901234\", " +
                "        \"r\":\"123456789012345678901234\", " +
                "        \"l\":\"123456789012345678901234\", " +
                "        \"h\":\"123456789012345678901234\", " +
                "        \"lo\":\"123456789012345678901234\", " +
                "        \"ro\":\"123456789012345678901234\" " +
                "    }, " +
                "    \"e\":0 " +
                "}";


        test = test.trim().replace(" ","");
        test = test.trim().replace("\\","");
        Queue<byte[]> bytes2 = SplitPackage.splitByte(test.getBytes());
    }

}