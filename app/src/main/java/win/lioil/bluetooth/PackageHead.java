package win.lioil.bluetooth;

//每个字段为 true时是 1，false 是 0
public class PackageHead {

    private boolean ackR;
    private boolean packageToggle;
    private boolean fragmentation;
    private boolean encP;
    private boolean lastPackage;
    private boolean reserve1;
    private boolean reserve2;
    private boolean msgType;

    public boolean isAckR() {
        return ackR;
    }

    public void setAckR(boolean ackR) {
        this.ackR = ackR;
    }

    public boolean isPackageToggle() {
        return packageToggle;
    }

    public void setPackageToggle(boolean packageToggle) {
        this.packageToggle = packageToggle;
    }

    public boolean isFragmentation() {
        return fragmentation;
    }

    public void setFragmentation(boolean fragmentation) {
        this.fragmentation = fragmentation;
    }

    public boolean isEncP() {
        return encP;
    }

    public void setEncP(boolean encP) {
        this.encP = encP;
    }

    public boolean isLastPackage() {
        return lastPackage;
    }

    public void setLastPackage(boolean lastPackage) {
        this.lastPackage = lastPackage;
    }
    public boolean isReserve1() {
        return reserve1;
    }

    public void setReserve1(boolean reserve1) {
        this.reserve1 = reserve1;
    }

    public boolean isReserve2() {
        return reserve2;
    }

    public void setReserve2(boolean reserve2) {
        this.reserve2 = reserve2;
    }

    public boolean isMsgType() {
        return msgType;
    }

    public void setMsgType(boolean msgType) {
        this.msgType = msgType;
    }

    public void reset(){
        this.setAckR(false);
        this.setPackageToggle(false);
        this.setFragmentation(false);
        this.setEncP(false);
        this.setLastPackage(false);
        this.setReserve1(false);
        this.setReserve2(false);
        this.setMsgType(false);
    }
}
