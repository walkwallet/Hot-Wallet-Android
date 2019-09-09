package systems.v.wallet.data.bean;

public class ContractContentBean {
    private String languageCode;
    private int languageVersion;
    private String[] triggers;
    private String[] descriptors;
    private String[] stateVariables;
    private ContractTextualBean textual;
    private long height;

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public int getLanguageVersion() {
        return languageVersion;
    }

    public void setLanguageVersion(int languageVersion) {
        this.languageVersion = languageVersion;
    }

    public String[] getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(String[] descriptors) {
        this.descriptors = descriptors;
    }

    public String[] getStateVariables() {
        return stateVariables;
    }

    public void setStateVariables(String[] stateVariables) {
        this.stateVariables = stateVariables;
    }


    public ContractTextualBean getTextual() {
        return textual;
    }

    public void setTextual(ContractTextualBean textual) {
        this.textual = textual;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String[] getTriggers() {
        return triggers;
    }

    public void setTriggers(String[] triggers) {
        this.triggers = triggers;
    }

    public static class ContractTextualBean {
        private String triggers;
        private String descriptors;
        private String stateVariables;

        public String getTriggers() {
            return triggers;
        }

        public void setTriggers(String triggers) {
            this.triggers = triggers;
        }

        public String getDescriptors() {
            return descriptors;
        }

        public void setDescriptors(String descriptors) {
            this.descriptors = descriptors;
        }

        public String getStateVariables() {
            return stateVariables;
        }

        public void setStateVariables(String stateVariables) {
            this.stateVariables = stateVariables;
        }
    }

}
