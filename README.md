# GestureSurfaceView
支持手势拖动旋转缩放及点计算的基View

![Image](https://github.com/huweijian5/GestureSurfaceView/blob/master/screenshot/device-2016-12-16-231042.mp4_1481901674.gif)
***
![Image](https://github.com/huweijian5/GestureSurfaceView/blob/master/screenshot/UC%E6%88%AA%E5%9B%BE20161216232559.png)
***
![Image](https://github.com/huweijian5/GestureSurfaceView/blob/master/screenshot/UC%E6%88%AA%E5%9B%BE20161216232849.png)

##使用说明

* GestureSurfaceView提供了对图片的手势操作，包括拖动、旋转、缩放，同时开放了大量接口进行自定义，比如设置缩放比例范围、手势的灵敏度、坐标系、图片居中、适应窗体以及对背景设置等，让用户可以继承它后实现自己的业务代码，在drawOther方法中用户可以实现自己的绘制以实现不同的特殊效果（如上面的例子就是一个简单的特效），而不用关心对图片的操作、对坐标的换算等等，具体接口请看在线文档
* GestureSurfaceView继承于BaseSurfaceView,BaseSurfaceView里封装了对绘制线程的处理，用户可以只关注绘制的部分，省去重复写代码的麻烦。使用也极其简单，继承它后实现抽象方法doDraw即可
* 在实现对图片的拖动时，增加了边界检测，原本只是简单的矩形边界，但由于旋转后边界也发生了变化，故而扩展RectF产生了带有旋转角度的RotateRectF，使用它可以方便地判断点是否在其区域内。

## JavaDoc

* [在线JavaDoc](https://jitpack.io/com/github/huweijian5/GestureSurfaceView/1.0.0/javadoc/index.html)

* 网址：`https://jitpack.io/com/github/huweijian5/GestureSurfaceView/[VersionCode]/javadoc/index.html`
* 其中[VersionCode](https://github.com/huweijian5/GestureSurfaceVie/releases)请替换为最新版本号
* 注意文档使用UTF-8编码，如遇乱码，请在浏览器选择UTF-8编码即可

## 引用

* 如果需要引用此库,做法如下：
* Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```	
* and then,add the dependecy:
```
dependencies {
	        compile 'com.github.huweijian5:GestureSurfaceVie:latest_version'
}
```
* 其中latest_version请到[releases](https://github.com/huweijian5/GestureSurfaceVie/releases)中查看
