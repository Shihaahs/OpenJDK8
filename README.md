# Mac Catalina 10.15.4 编译OpenJDK 8

Created By: 石 傻傻
Last Edited By: 石 傻傻
Last Edited Time: Apr 22, 2020 4:17 PM

### 机器配置

![Mac%20Catalina%2010%2015%204%20OpenJDK%208/Untitled.png](Mac%20Catalina%2010%2015%204%20OpenJDK%208/Untitled.png)

mac OS Catalina 10.15.4

Xcode 11，xcode-select version 2373

当前系统jdk版本 

**java version "1.8.0_221"**

**Java(TM) SE Runtime Environment (build 1.8.0_221-b11)**

**Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, mixed mode)**

### 环境准备

需要 Xcode 11 、Homebrew 、 XQuartz 、JDK8 、Mercurial

Xcode 11 → [https://developer.apple.com/download/more/](https://developer.apple.com/download/more/)

XQuartz → [https://www.xquartz.org/](https://www.xquartz.org/)

依赖准备

    xcode-select install
    
    brew install mercurial
    
    brew install freetype
    
    brew install gcc@4.9
    
    brew install llvm
    
    
    #添加软连接
    // llvm 
    sudo ln -s /usr/bin/llvm-gcc /Applications/Xcode.app/Contents/Developer/usr/bin/llvm-gcc
    sudo ln -s /usr/bin/llvm-g++ /Applications/Xcode.app/Contents/Developer/usr/bin/llvm-g++
    
    // XQuartz
    sudo ln -s /usr/X11/include/X11 /usr/include/X11
    sudo ln -s /usr/X11/include/freetype2/freetype/ /usr/X11/include/freetype
    // sudo ln -s /opt/X11 /usr/include/X11

### 源码准备

    ### 准备源码 ###  最好要代理
    # mercurial -> 
    hg clone http://hg.openjdk.java.net/jdk8u/jdk8u41/
    sh get_source.sh

### 环境准备

    # 设定语言选项，必须设置
    export LANG=C
    # Mac平台，C编译器不再是GCC，而是clang
    export CC=clang
    export CXX=clang++
    export CXXFLAGS=-stdlib=libc++
    # 是否使用clang，如果使用的是GCC编译，该选项应该设置为false
    export USE_CLANG=true
    # 跳过clang的一些严格的语法检查，不然会将N多的警告作为Error
    export COMPILER_WARNINGS_FATAL=false
    # 链接时使用的参数
    export LFLAGS='-Xlinker -lstdc++'
    # 使用64位数据模型
    export LP64=1
    # 告诉编译平台是64位，不然会按照32位来编译
    export ARCH_DATA_MODEL=64
    # 允许自动下载依赖
    export ALLOW_DOWNLOADS=true
    # 并行编译的线程数，编译时长，为了不影响其他工作，可以选择2
    export HOTSPOT_BUILD_JOBS=2
    export PARALLEL_COMPILE_JOBS=2 #ALT_PARALLEL_COMPILE_JOBS=2
    # 是否跳过与先前版本的比较
    export SKIP_COMPARE_IMAGES=true
    # 是否使用预编译头文件，加快编译速度
    export USE_PRECOMPILED_HEADER=true
    # 是否使用增量编译
    export INCREMENTAL_BUILD=true
    # 编译内容
    export BUILD_LANGTOOL=true
    export BUILD_JAXP=true
    export BUILD_JAXWS=true
    export BUILD_CORBA=true
    export BUILD_HOTSPOT=true
    export BUILD_JDK=true
    # 编译版本
    export SKIP_DEBUG_BUILD=true
    export SKIP_FASTDEBUG_BULID=false
    export DEBUG_NAME=debug
    # 避开javaws和浏览器Java插件之类部分的build
    export BUILD_DEPLOY=false
    export BUILD_INSTALL=false
    
    # 最后需要干掉这两个环境变量（如果你配置过），不然会发生诡异的事件
    unset JAVA_HOME
    unset CLASSPATH

### 编译命令

    //--with-freetype-include=freetype头文件
    //--with-freetype-lib= libfreetype.dylib 所在
    sh ./configure --with-target-bits=64 --with-debug-level=slowdebug --with-jvm-variants=server --with-zlib=system --with-freetype-include=/usr/local/include/freetype2 --with-freetype-lib=/usr/local/lib --with-boot-jdk=/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home
    make clean
    make all

### Troubleshooting

configure阶段

- configure: error: GCC compiler is required. Try setting --with-tools-dir.

    编辑
    common/autoconf/generated-configure.sh
    
    搜索上述关键字'GCC compiler is required. Try setting --with-tools-dir.'
    
    注释掉那段 （两处）
    if test $? -ne 0; then
    ...
    fi

- fatal error: 'iostream' file not found

    把xcode9里面的
    /Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/c++/4.2.1
    复制到对应xcode11里的对应c++下面（和v1保持并列）
    
    把xcode9里面的
    /Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/lib/libstdc++.tbd
    /Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/lib/libstdc++.6.tbd
    复制到对应xcode11的对应路径

xcode9附件

[xcode9.zip](Mac%20Catalina%2010%2015%204%20OpenJDK%208/xcode9.zip)

make阶段

- hotspot/src/share/vm/opto/lcm.cpp:60:35: error: ordered comparison between pointer and zero ('address' (aka 'unsigned char *') and 'int')
  if (Universe::narrow_oop_base() > 0) { // Implies UseCompressedOops.

    编辑
    hotspot/src/share/vm/opto/lcm.cpp
    60行
    
    if (Universe::narrow_oop_base() > 0
    改为
    if (Universe::narrow_oop_base() != NULL)

- hotspot/src/share/vm/gc_implementation/g1/heapRegionSet.hpp:38:38: note: expanded from macro 'HEAP_REGION_SET_FORCE_VERIFY'
#define HEAP_REGION_SET_FORCE_VERIFY defined(ASSERT)

    编辑
    hotspot/src/share/vm/gc_implementation/g1/heapRegionSet.hpp
    38行
    
    defined(ASSERT)
    改为
    0

- hotspot/src/share/vm/opto/loopPredicate.cpp:775:73: error: ordered comparison between pointer and zero ('const TypeInt *' and 'int')
      assert(rng->Opcode() == Op_LoadRange || _igvn.type(rng)->is_int() >= 0, "must be");

    编辑
    hotspot/src/share/vm/opto/loopPredicate.cpp
    775行
    
    _igvn.type(rng)->is_int() >= 0
    改为
    _igvn.type(rng)->is_int()->_lo >= 0

- hotspot/src/share/vm/runtime/virtualspace.cpp:335:14: error: ordered comparison between pointer and zero ('char *' and 'int')
  if (base() > 0) {
      ~~~~~~ ^ ~

    编辑
    hotspot/src/share/vm/runtime/virtualspace.cpp
    335行
    
    if (base() > 0) {
    改为
    if (base() != 0) {

- hotspot/agent/src/os/bsd/MacosxDebuggerLocal.m:27:9: fatal error: 'JavaNativeFoundation/JavaNativeFoundation.h' file not found
#import <JavaNativeFoundation/JavaNativeFoundation.h>

    因为Xcode之前会安装类似 xxx-for-java-command-lines-tools 的框架包到 /System/Library/Frameworks, 
    而自从 macOS 10.14 开始，这些框架包全部都被安装到了 /Library/Developer/CommandLineTools/SDKs/MacOSX10.1x.sdk
    
    find / -name "*JavaNativeFoundation.h*"
    
    ...
    /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Frameworks/JavaNativeFoundation.framework/Versions/A/Headers/JavaNativeFoundation.h
    /Library/Developer/CommandLineTools/SDKs/MacOSX10.15.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Frameworks/JavaNativeFoundation.framework/Versions/A/Headers/JavaNativeFoundation.h
    ....
    找到framework的所在路径
    
    编辑
    hotspot/make/bsd/makefiles/saproc.make
    67行
    
    SALIBS = -g -framework Foundation -F/System/Library/Frameworks/JavaVM.framework/Frameworks -framework JavaNativeFoundation -framework Security -framework CoreFoundation
    改为
    SALIBS = -g -framework Foundation -F/Library/Developer/CommandLineTools/SDKs/MacOSX10.15.sdk/System/Library/Frameworks/JavaVM.framework/Frameworks -framework JavaNativeFoundation -framework Security -framework CoreFoundation
    
    hotspot/make/bsd/makefiles/saproc.make:106
    
    -I/System/Library/Frameworks/JavaVM.framework/Headers
    改为
    -I/Library/Developer/CommandLineTools/SDKs/MacOSX10.15.sdk/System/Library/Frameworks/JavaVM.framework/Headers

- fatal error: 'CoreGraphics/CGBase.h' file not found

    编辑
    jdk/make/lib/PlatformLibraries.gmk
    jdk/make/lib/Awt2dLibraries.gmk
    
    将包含 ApplicationServices.framework 的路径替换成 
    /Library/Developer/CommandLineTools/SDKs/MacOSX10.15.sdk/System/Library/Frameworks/CoreGraphics.framework
    
    将包含 JavaVM.framwork 的路径替换成 
    /Library/Developer/CommandLineTools/SDKs/MacOSX10.15.sdk/System/Library/Frameworks/JavaVM.framework

- Undefined symbols for architecture x86_64:
"_attachCurrentThread", referenced from:
    +[ThreadUtilities getJNIEnv] in ThreadUtilities.o
    +[ThreadUtilities getJNIEnvUncached] in ThreadUtilities.o
ld: symbol(s) not found for architecture x86_64

    编辑
    jdk/src/macosx/native/sun/osxapp/ThreadUtilities.m
    39行
    
    inline void attachCurrentThread(void** env) {
    改为
    static inline void attachCurrentThread(void** env) {

---

---

最后，Finish！

![Mac%20Catalina%2010%2015%204%20OpenJDK%208/Untitled%201.png](Mac%20Catalina%2010%2015%204%20OpenJDK%208/Untitled%201.png)

---

**参考文档:**

- [https://www.zhoujunwen.com/2019/building-openjdk-8-on-mac-osx-catalina-10-15](https://www.zhoujunwen.com/2019/building-openjdk-8-on-mac-osx-catalina-10-15)
- [https://imkiva.com/blog/2018/02/body/building-openjdk8-on-macos/](https://imkiva.com/blog/2018/02/body/building-openjdk8-on-macos/)
- [https://www.meetkiki.com/archives/Mac Pro编译OpenJDK](https://www.meetkiki.com/archives/Mac%20Pro%E7%BC%96%E8%AF%91OpenJDK)
