package win.lioil.bluetooth.ble;

public class TogglePackage {

    private static Integer toggleFlag = 1;

    public static Integer getToggle(){
        return toggleFlag;
    }

    public static Integer reverse(){
        if (toggleFlag == 0){
            toggleFlag = 1;
        }else{
            toggleFlag = 0;
        }
        return toggleFlag;
    }

    public static void reset(){
        toggleFlag = 1;
    }

}
