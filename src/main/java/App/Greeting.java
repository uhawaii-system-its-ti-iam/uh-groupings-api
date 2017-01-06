package App;

public class Greeting {

    private final long id;
    private final String content;
    private final String someStuff = "some stuff";
    private Number one = 1;
    private String string = "a string";

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSomeStuff(){
        return someStuff;
    }

    public Number getOne(){
        return one;
    }
    public String getString(){
        return string;
    }
}
