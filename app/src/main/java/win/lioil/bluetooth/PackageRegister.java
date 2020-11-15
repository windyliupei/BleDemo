package win.lioil.bluetooth;

import java.util.ArrayList;
import java.util.List;

public class PackageRegister {

    public static PackageRegister getInstance() {
        return PackageRegisterHolder.register;
    }

    private static class PackageRegisterHolder {
        private static final PackageRegister register = new PackageRegister();
    }

    List<IPackageNotification> list = new ArrayList<>();

    public void addedPackageListener(IPackageNotification notification){
        list.add(notification);
    }

    public void notification(){
        for (IPackageNotification p : list) {
            p.receiveLastPackage();
        }
    }

//    public List<IPackageNotification> getListeners(){
//        return list;
//    }

}
