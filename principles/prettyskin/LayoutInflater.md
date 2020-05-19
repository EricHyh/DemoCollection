# Android动态换肤框架PrettySkin原理篇（一） —— LayoutInflater的理解及使用

## 前言
相信大家一定接触到过如何在应用不重启的情况下，实现切换应用主题这个问题。我会专门写一个系列，来介绍如何实现动态切换应用皮肤（包含动态切换主题），今天我们来一起探讨第一个问题，LayoutInflater的理解及使用。闲话少说，先上图：

<img width="300"  src="https://raw.githubusercontent.com/EricHyh/file-repo/master/PrettySkin/gif/homepage.gif"/>


## 思考
在刚接触这个问题时，我总是把思路聚焦在Activity的**setTheme**函数上，但这其实是一个错误的想法。一个Activity的主题只能在**setContentView**之前设置，在**setContentView**之后再通过**setTheme**设置主题是没有意义的；一般情况下Activity的View是在**setContentView**的时候创建的，创建View时会使用**setContentView**之前设置的主题，之后再设置的主题是不会主动应用在已经创建好的View上，所以也就没办法通过这种方式达到我们想要的切换主题的效果。  

我们仔细想想，我们为什么要动态更换应用主题，其实根本目的是想动态的更换View的某些属性，例如背景色、图片、文字颜色等等。那这些东西我们平时是怎么更换它们的？额~~难道是通过**view.setBackground**这些函数？没错，你想得没错，android动态换肤就是这么简单，这个想法就是整个实现中的核心中的核心。你可能在想，我读书少，你不要忽悠我。我真没骗你，你往下看就知道了。  

上面我们说到，我们要通过**view.setBackground**这类函数去实现一个换肤框架。很显然遍历View树上所有的View一个个去设置它们要改变的属性是不科学的，我们需要定义一套规则去方便我们管理整个换肤过程，最好是使用的时候跟我们平时使用主题一样，不要让我们操太多心。

说了这么多，我们先整理下思路：
1. 拿到所有需要更换属性的View；
2. 记录View需要更换的属性，例如background、src、text_color之类的；
3. 记录每个View的属性与主题中属性的对应关系；
4. 主题变化时，通知每个View从新的主题中拿出对应的资源进行刷新；


## 如何拿到所有需要跟换属性的View，LayoutInflater闪亮登场
已经有过很多的文章介绍LayoutInflater的源码，这里我就不做过多的撰述，直接讲与本文相关部分。Android的布局文件都是通过LayoutInflater去解析成对应的View树，一般来说LayoutInflater内部已经完全的实现了整个过程，我们不需要去关心它的内部实现。如果我们想接管View的创建过程，可以通过LayoutInflater的**setFactory**或者**setFactory2**去实现。这里我们来看下**Factory**和**Factory2**究竟是何方神圣。
