package systems.v.wallet.ui.view.transaction.adapter;

public class SuperNodeInfo {
    public String superNodeName;
    public String subNodeName;
    public int subNodeId;
    public String address;
    public boolean isSubNode;

    // supernode constructor
    public SuperNodeInfo(String superNodeName, String address) {
        this.superNodeName = superNodeName;
        this.address = address;
        this.isSubNode = false;
    }

    // subnode constructor
    public SuperNodeInfo(String superNodeName, String address, String subNodeName, int subNodeId) {
        this.superNodeName = superNodeName;
        this.subNodeName = subNodeName;
        this.subNodeId = subNodeId;
        this.address = address;
        this.isSubNode = true;
    }
}
