# SPI 自适应拓展

在 Dubbo 中，很多拓展都是通过 SPI 机制进行加载的，比如 Protocol、Cluster、LoadBalance 等。有时，有些拓展并不想在框架启动阶段被加载，而是希望在拓展方法被调用时，根据运行时参数进行加载。这听起来有些矛盾。拓展未被加载，那么拓展方法就无法被调用（静态方法除外）。拓展方法未被调用，拓展就无法被加载。对于这个矛盾的问题，Dubbo 通过自适应拓展机制很好的解决了。自适应拓展机制的实现逻辑比较复杂，首先 Dubbo 会为拓展接口生成具有代理功能的代码。然后通过 javassist 或 jdk 编译这段代码，得到 Class 类。最后再通过反射创建代理类，整个过程比较复杂。

## 演示示例

车轮制造厂接口 `WheelMaker`：

```java
public interface WheelMaker {
    Wheel makeWheel(URL url);
}
```
WheelMaker 接口的自适应实现类如下：

```java
public class AdaptiveWheelMaker implements WheelMaker {
    public Wheel makeWheel(URL url) {
        if (url == null) {
        throw new IllegalArgumentException("url == null");
        }
    	// 1.从 URL 中获取 WheelMaker 名称
        String wheelMakerName = url.getParameter("Wheel.maker");
        if (wheelMakerName == null) {
            throw new IllegalArgumentException("wheelMakerName == null");
        }
        // 2.通过 SPI 加载具体的 WheelMaker
        WheelMaker wheelMaker = ExtensionLoader
            .getExtensionLoader(WheelMaker.class).getExtension(wheelMakerName);
        // 3.调用目标方法
        return wheelMaker.makeWheel(url);
    }
}
```
`AdaptiveWheelMaker` 是一个代理类，与传统的代理逻辑不同，`AdaptiveWheelMaker` 所代理的对象是在 `makeWheel` 方法中通过 SPI 加载得到的。

`makeWheel` 方法主要做了三件事情：

- 从 URL 中获取 `WheelMaker` 名称
- 通过 SPI 加载具体的 `WheelMaker` 实现类
- 调用目标方法

接下来，我们来看看汽车制造厂 `CarMaker` 接口与其实现类
```java
public interface CarMaker {
    Car makeCar(URL url);
}

public class RaceCarMaker implements CarMaker {
    WheelMaker wheelMaker;

    // 通过 setter 注入 AdaptiveWheelMaker
    public setWheelMaker(WheelMaker wheelMaker) {
        this.wheelMaker = wheelMaker;
    }

    public Car makeCar(URL url) {
        Wheel wheel = wheelMaker.makeWheel(url);
        return new RaceCar(wheel, ...);
    }
}
```
`RaceCarMaker` 持有一个 WheelMaker 类型的成员变量，在程序启动时，我们可以将 `AdaptiveWheelMaker` 通过 `setter` 方法注入到 `RaceCarMaker` 中。在运行时，假设有这样一个 url 参数传入：

```properties
dubbo://192.168.0.101:20880/XxxService?wheel.maker=MichelinWheelMaker
```
`RaceCarMaker` 的 `makeCar` 方法将上面的 url 作为参数传给 `AdaptiveWheelMaker` 的 `makeWheel` 方法，`makeWheel` 方法从 url 中提取 `wheel.maker` 参数，得到 `MichelinWheelMaker`。之后再通过 SPI 加载配置名为 `MichelinWheelMaker` 的实现类，得到具体的 `WheelMaker` 实例


## 源码分析

### Adaptive 注解

该注解的定义如下：

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
String[] value() default {};
}
```

Adaptive 可注解在类或方法上。

当 Adaptive 注解在类上时，Dubbo 不会为该类生成代理类。注解在方法（接口方法）上时，Dubbo 则会为该方法生成代理逻辑。

Adaptive 注解在类上的情况很少，在 Dubbo 中，仅有两个类被 Adaptive 注解了，分别是 `AdaptiveCompiler` 和 `AdaptiveExtensionFactory`。表示拓展的加载逻辑由人工编码完成。

更多时候，Adaptive 是注解在接口方法上的，表示拓展的加载逻辑需由框架自动生成


### 获取自适应拓展
`getAdaptiveExtension` 方法首先会检查缓存，缓存未命中，则调用 `createAdaptiveExtension` 方法创建自适应拓展

`createAdaptiveExtension` 方法的代码比较少，包含了三个逻辑：

- 调用 `getAdaptiveExtensionClass` 方法获取自适应拓展 Class 对象
- 通过反射进行实例化
- 调用 `injectExtension` 方法向拓展实例中注入依赖

Dubbo 中有两种类型的自适应拓展，一种是 **手工编码** ，一种是 **自动生成**

手工编码的自适应拓展中可能存在着一些依赖，而自动生成的 Adaptive 拓展则不会依赖其他类。调用 `injectExtension` 方法的目的是为手工编码的自适应拓展注入依赖



`getAdaptiveExtensionClass` 方法同样包含了三个逻辑，如下：

- 调用 `getExtensionClasses` 获取所有的拓展类
- 检查缓存，若缓存不为空，则返回缓存
- 若缓存为空，则调用 `createAdaptiveExtensionClass` 创建自适应拓展类

`getExtensionClasses` 这个方法用于获取某个接口的所有实现类。比如该方法可以获取 `Protocol` 接口的 `DubboProtocol`、`HttpProtocol`、`InjvmProtocol` 等实现类

在获取实现类的过程中，如果某个实现类被 `Adaptive` 注解修饰了，那么该类就会被赋值给 `cachedAdaptiveClass` 变量。此时，上面步骤中的第二步条件成立（缓存不为空），直接返回 `cachedAdaptiveClass` 即可。如果所有的实现类均未被 `Adaptive` 注解修饰，那么执行第三步逻辑，创建自适应拓展类

`createAdaptiveExtensionClass` 方法用于生成自适应拓展类，该方法首先会生成自适应拓展类的源码，然后通过 `Compiler` 实例（Dubbo 默认使用 `javassist` 作为编译器）编译源码，得到代理类 Class 实例



