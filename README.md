# LargeImageView
分片加载的超大图浏览

## Dependency

```gradle
allprojects {
        repositories {
            maven { url "" }
        }
}
```

在 `build.gradle` 中添加依赖
```gradle
dependencies {
    compile 'org.wzy:largeimage:0.1.0'
}
```

## Features
- 手势放大缩小／双击放大
- 快速滑动(fling)
- 状态保存(onSaveInstance/onRestoreInstanceState)

## Usage
```xml
<org.wzy.largeimage.LargeImageView
        android:id="@+id/img"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

```java
LargeImageView image = (LargeImageView) findViewById(R.id.img);
image.setImageResource(new File("/mnt/sdcard/test.jpg"));
```

That's it!