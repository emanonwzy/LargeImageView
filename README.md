# LargeImageView
分片加载的超大图浏览

# 效果
![image](raw/output.gif)

## Dependency

```groovy
dependencies {
    implementation 'io.github.emanonwzy:largeimage:0.2.0'
}
```

## Features
- 手势放大缩小／双击放大
- 快速滑动(fling)
- 状态保存(onSaveInstance/onRestoreInstanceState)

## Usage
```xml
<LargeImageView
        android:id="@+id/img"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

```java
LargeImageView image = (LargeImageView) findViewById(R.id.img);
image.setImageResource(new File("/mnt/sdcard/test.jpg"));
```
