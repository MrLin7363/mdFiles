spring设计模式

#### 观察者模式

JDK提供的观察者变量

```
// 问题
public class Question {
    private String userName;
    private String content;
}
```

```
// 观察者
public class Teacher implements Observer {
    private String name;
    public Teacher(String name){
        this.name = name;
    }
	// 重写的方法
    public void update(Observable o, Object arg) {
        GPer gper = (GPer)o;
        Question question = (Question)arg;
        System.out.println("===============================");
        System.out.println(name + "老师，你好！\n" +
        "您收到了一个来自“" + gper.getName() + "”的提问，希望您解答，问题内容如下：\n" +
        question.getContent() + "\n" +
        "提问者：" + question.getUserName());
    }
}
```

```
/**
 * JDK提供的一种观察者的实现方式，被观察者
 */
public class GPer extends Observable{

    private String name = "微信提问圈";
    private static GPer gper = null;
    private GPer(){}

    public static GPer getInstance(){
        if(null == gper){
            gper = new GPer();
        }
        return gper;
    }

    public String getName() {
        return name;
    }

    public void publishQuestion(Question question){
        System.out.println(question.getUserName() + "在" + this.name + "上提交了一个问题。");
        setChanged(); // 当前对象已更改
        notifyObservers(question); // 通知观察者,遍历观察者，回调观察者的update方法
    }
}
```

```
public class ObserverTest {
    public static void main(String[] args) {
        GPer gper = GPer.getInstance();
        Teacher tom = new Teacher("Tom");
        Teacher mic = new Teacher("Mic");

        Question question = new Question();
        question.setUserName("小明");
        question.setContent("观察者设计模式适用于哪些场景？");
        gper.addObserver(tom);
        gper.addObserver(mic);
        gper.publishQuestion(question);
    }
}
```

```
小明在微信提问圈上提交了一个问题。
===============================
Mic老师，你好！
您收到了一个来自“微信提问圈”的提问，希望您解答，问题内容如下：
观察者设计模式适用于哪些场景？
提问者：小明
===============================
Tom老师，你好！
您收到了一个来自“微信提问圈”的提问，希望您解答，问题内容如下：
观察者设计模式适用于哪些场景？
提问者：小明
```

