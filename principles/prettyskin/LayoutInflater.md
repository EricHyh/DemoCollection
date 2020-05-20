# Android动态换肤框架PrettySkin原理篇（一）- LayoutInflater的理解及使用

## 前言
PrettySkin是我之前实现的一个Android动态换肤框架，实现了应用内主题换肤、外部APK文件换肤，自定义皮肤包等功能，功能齐全，更多说明请移步[Github项目](https://github.com/EricHyh/PrettySkin-master)中查看。我打算专门写一个系列，来记录我实现这个库的关键部分原理以及思路，感兴趣的朋友可以看一看。本篇我们先探讨第一个问题，LayoutInflater的理解及使用。闲话少说，先上图：

<img width="300"  src="https://raw.githubusercontent.com/EricHyh/file-repo/master/PrettySkin/gif/homepage.gif"/>


## 思考
在刚接触动态换肤这个问题时，我就感觉这不就是换个主题就解决了吗？所以起初我总是把思路聚焦在Activity的**setTheme**函数上，但这其实是一个错误的想法。一个Activity的主题只能在**setContentView**之前设置，在**setContentView**之后再通过**setTheme**设置主题是没有意义的；一般情况下Activity的View是在**setContentView**的时候创建的，创建View时会使用**setContentView**之前设置的主题，之后再设置的主题是不会主动应用在已经创建好的View上，所以也就没办法通过这种方式达到我们想要的切换主题的效果。  

仔细想想，我们为什么要设置应用主题，其实根本目的是想让View用到主题中定义的资源，例如背景色、图片、文字颜色等等。但是View创建后，我们只能通过**view.setXXX**这类函数去修改View的某些属性，例如通过**view.setBackground**去设置View的背景。额~~难道我们只能通过这类函数去实现View的换肤吗？没错，你想得没错，Android动态换肤就是这么简单，这个想法就是整个实现中的核心中的核心。

上面我们说到，我们要通过**view.setXXX**这类函数去实现一个换肤框架。很显然遍历View树上所有的View一个个去设置它们要改变的属性是不科学的，我们需要定义一套规则去方便我们管理整个换肤过程，最好是使用的时候跟我们平时使用主题一样，不要让我们操太多心。

说了这么多，我们先整理下思路：
1. 拿到所有需要更换属性的View（以下统称为皮肤View）；
2. 记录皮肤View需要更换的属性，例如background、src、text_color之类的（以下统称为皮肤属性）；
3. 记录每个皮肤View的皮肤属性与主题中属性的对应关系；
4. 主题变化时，通知每个View从新的主题中拿出对应的资源进行刷新；


## 如何拿到所有的皮肤View，LayoutInflater闪亮登场
已经有过很多的文章介绍LayoutInflater的源码，这里我就不做过多的撰述，直接讲与本文相关部分。Android的布局文件都是通过LayoutInflater去解析成对应的View树，一般来说LayoutInflater内部已经完全的实现了整个过程，我们不需要去关心它的内部实现。如果我们想接管View的创建过程，可以通过LayoutInflater的**setFactory**或者**setFactory2**去实现。这里简单看下**Factory**和**Factory2**究竟是何方神圣。

Factory源码：
```
public interface Factory {
    /**
     * @param name 在layout文件中定义的View标签名称
     * @param context 当前LayoutInflater对象中保存的上下文对象
     * @param attrs View标签下的属性.
     *
     * @return View 返回创建的View，
     */
    @Nullable
    View onCreateView(@NonNull String name, @NonNull Context context,
            @NonNull AttributeSet attrs);
}
```
Factory2源码：
```
public interface Factory2 extends Factory {
    /**
     * @param parent 需要创建的View的父容器
     * @param name 在layout文件中定义的View标签名称
     * @param context 当前LayoutInflater对象中保存的上下文对象
     * @param attrs View标签下的属性.
     *
     * @return View 返回创建的View，
     */
    @Nullable
    View onCreateView(@Nullable View parent, @NonNull String name,
            @NonNull Context context, @NonNull AttributeSet attrs);
}
```
总的来说当我们设置了**Factory**或者**Factory2**后，我们可以接管创建View的步骤，在这里我们可以实现对皮肤View的收集，伪代码如下：
```
public class SkinInflateFactory implements LayoutInflater.Factory2 {

    //皮肤View列表
    private List<SkinView> mSkinViews = new ArrayList<>();

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (isSkinView(attrs)) {//根据View标签下的属性，判断是否是皮肤View
            View view = createViewByTag(parent, name, context, attrs);//根据View标签创建View
            Map<String, String> attrKeyMap = getAttrKeyMap(attrs);//拿到每个皮肤View的属性与主题中属性的对应关系
            SkinView skinView = new SkinView(view, attrKeyMap);//构造一个皮肤View的数据结构
            mSkinViews.add(skinView);//添加到列表中，切换主题时遍历该列表实现View的属性更换
            ISkin currentSkin = getCurrentSkin();//获取当前的主题皮肤
            if(currentSkin != null){//如果有皮肤，就让新创建的View使用这个皮肤
                skinView.changeSkin(currentSkin);
            }
            return view;
        }
        return null;
    }

    private boolean isSkinView(AttributeSet attrs);

    private View createViewByTag(View parent, String name, Context context, AttributeSet attrs);
    
    private Map<String, String> getAttrKeyMap(AttributeSet attrs);

    private ISkin getCurrentSkin();
    
}
```
关于设置**Factory**或者**Factory2**我们还需要注意的是，一个LayoutInflater对象只能设置一次**Factory**或者**Factory2**，设置第二次的时候会直接抛异常。为了避免出现这个情况，在一个LayoutInflater已经设置**Factory**或者**Factory2**的情况下，我们需要用到反射的方式去完成这个操作，核心代码如下：
```
public void setLayoutInflaterSkinable(LayoutInflater layoutInflater) {
    LayoutInflater.Factory factory = layoutInflater.getFactory();
    if (factory == null) {
        //先看看LayoutInflater有没有设置Factory，如果没有，直接设置我们的LayoutInflater
        layoutInflater.setFactory2(new SkinInflateFactory(this));
    } else {
        //如果有，那就看看是否是我们之前设置的，避免重复设置
        if (factory instanceof SkinInflateFactory) {
            return;
        }
        //如果不是我们之前设置的，那就把原始的factory做一层包装，在它原有逻辑的基础上加入我们的逻辑
        
        //优先包装原始的factory2
        LayoutInflater.Factory2 factory2 = layoutInflater.getFactory2();
        if (factory2 == null) {
            //反射将我们创建的factory设置给LayoutInflater
            SkinInflateFactory skinInflateFactory = new SkinInflateFactory(this, factory);
            Reflect.from(LayoutInflater.class)
                    .filed("mFactory", LayoutInflater.Factory.class)
                    .set(layoutInflater, skinInflateFactory);
        } else {
            //反射将我们创建的factory设置给LayoutInflater
            SkinInflateFactory skinInflateFactory = new SkinInflateFactory(this, factory2);
            Reflect.from(LayoutInflater.class)
                    .filed("mFactory2", LayoutInflater.Factory.class)
                    .set(layoutInflater, skinInflateFactory);
        }
    }
}
```

## 如何拿到应用中所有的LayoutInflater（本文重点）
在上节中，我们已经知道如何通过给LayoutInflater设置**Factory**去收集皮肤View。那么我们现在就需要拿到进程中LayoutInflater对象去设置**Factory**，我们通常会通过以下两种方式去获取LayoutInflater对象：
```
//方式一
LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//方式二
LayoutInflater layoutInflater = LayoutInflater.from(context);
```
方式一与方式二本质上上是同一种方式，看看方式二的实现代码就知道了：
```
public static LayoutInflater from(Context context) {
    LayoutInflater LayoutInflater =
            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    if (LayoutInflater == null) {
        throw new AssertionError("LayoutInflater not found.");
    }
    return LayoutInflater;
}
```

那么现在问题来了，我怎么拿到所有的LayoutInflater对象？在解答这个问题前，请大家先思考一个问题，一个应用进程会有几个LayoutInflater对象？  
理论上来说LayoutInflater的数量可以多到内存爆炸，因为LayoutInflater是可以由我们随意创建的，这里我们不讨论这种情况。一般情况下，根据我个人愚见：

> LayoutInflater的数量 = 1 + ContextThemeWrapper的数量 + AsyncLayoutInflater的数量;

简单的给大家介绍一下各个组成的来历：
1. 其中的**1**代表的是什么
每个应用进程都有一个原始的LayoutInflater对象，这个对象来源于**android.app.SystemServiceRegistry**，相关源码片段如下：
```
final class SystemServiceRegistry {

    ...

    static{
        
        ...
        
        registerService(Context.LAYOUT_INFLATER_SERVICE, LayoutInflater.class,
                new CachedServiceFetcher<LayoutInflater>() {
            @Override
            public LayoutInflater createService(ContextImpl ctx) {
                return new PhoneLayoutInflater(ctx.getOuterContext());//这就是那个原始的LayoutInflater
        }});
        
        ...
    }
    
    public static String getSystemServiceName(Class<?> serviceClass) {
        return SYSTEM_SERVICE_NAMES.get(serviceClass);
    }
    ...
   
}
```
我们如果通过**android.app.ContextImpl**这个上下文去获取LayoutInflater时，拿到的就是它，相关源码片段如下；
```
class ContextImpl extends Context {
    
    ...
    
    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        return SystemServiceRegistry.getSystemServiceName(serviceClass);
    }
    
    ...

}
```
顺便说一下，Android的Context采用的是装饰设计模式，其它所有的Context对象的本质上都是基于**ContextImpl**包装实现的，所以有一些Context它们获取到的LayoutInflater就是通过**ContextImpl**获取的，例如Application、Service、BroadcastReceiver#onCreate中的Context参数、ContentProvider中的Context成员变量；

2. ContextThemeWrapper有何独特之处  

ContextThemeWrapper会创建一个LayoutInflater对象，用于解析布局，相关源码片段如下：
```
public class ContextThemeWrapper extends ContextWrapper {
    
    private LayoutInflater mInflater;

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return getBaseContext().getSystemService(name);
    }
}
```
所以每个ContextThemeWrapper都会有一个LayoutInflater，知道这个还不够，还得知道怎么拿到所有的ContextThemeWrapper；一般情况下：
> ContextThemeWrapper的数量 = Activity的数量 + dialog的数量 + 开发者创建的ContextThemeWrapper的数量；

其中Activity的数量是因为它自己本身就继承自ContextThemeWrapper，Dialog它会把构造函数中传入的Context包装成ContextThemeWrapper，开发者创建的就不用多说了，你自己创建的你自己最清楚；相关源码片段如下：
```
public class Activity extends ContextThemeWrapper { //这里继承ContextThemeWrapper 
}

public class Dialog {
        
        Dialog(@NonNull Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
            if (createContextThemeWrapper) {
                if (themeResId == ResourceId.ID_NULL) {
                    final TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.dialogTheme, outValue, true);
                    themeResId = outValue.resourceId;
                }
                mContext = new ContextThemeWrapper(context, themeResId);//这里创建了ContextThemeWrapper
            } else {
                mContext = context;
            }
            
            ...
        } 
}
```


3. AsyncLayoutInflater
AsyncLayoutInflater是官方推出的一款用于异步布局的扩展库，翻阅内部的源码可知，它也会创建一个LayoutInflater对象：

```
public final class AsyncLayoutInflater {
    
    LayoutInflater mInflater;

    public AsyncLayoutInflater(@NonNull Context context) {
        this.mInflater = new AsyncLayoutInflater.BasicInflater(context);
        ...
    }
}
```

看完上面的分析，你可能会觉拿个LayoutInflater这么复杂，还能不能玩；别着急，上面的分析只是尽可能的涵盖所有情况，大多数情况下，我们只需要处理原始的LayoutInflater和Activity的LayoutInflater就行了；没有特殊的需求，一般不会去自己创建ContextThemeWrapper。这样的话，问题就简单了，我们完全可以做到在Application初始中去完成给LayoutInflater设置Factory的这个设置操作，在其它地方就不需要在关心这个问题，伪代码如下：
```
public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setContextSkinable(this);//让原始的LayoutInflater支持换肤
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                setContextSkinable(activity);//让Activity中的LayoutInflater支持换肤
            }
        });
    }

    public void setContextSkinable(Context context){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        setLayoutInflaterSkinable(layoutInflater);
    }
    
    public void setLayoutInflaterSkinable(LayoutInflater layoutInflater);
}
```

## 文末总结
LayoutInflater在整个换肤框架中起到一个桥梁的作用，它让皮肤View与皮肤之间建立了联系，这样就可以实现当皮肤变化时通知皮肤View做相应的变化。  

在下一篇中我会介绍如何定义皮肤View以及皮肤属性，关键类 - AttributeSet，敬请期待。