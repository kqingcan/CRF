public class Word {
    private String value;
    private String tag;

    public Word(String value, String tag){
        this.value = value;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
