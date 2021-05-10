# QRScan
 基于CameraX和ZXing、Mlkit的二维码扫描，并实现解析二维码图片获取数据。

**如何引入：**

1. 在根项目的build.gradle文件中添加：

   ```kotlin
   allprojects {
		   repositories {
			   ...
			   maven { url 'https://jitpack.io' }
		   }
	   }
   ```

2. 在所使用的Module的build.gradle文件下，引入：

   ```kotlin
   dependencies {
   	        implementation 'com.github.hijiaopeng:QRScan:1.1.0'
   	}
   ```

#### 使用方法：

**二维码扫描**

1. 在界面中，引入PreviewView组件，用于展示相机预览画面

   ```kotlin
   <androidx.camera.view.PreviewView
           android:id="@+id/scanCameraPreview"
           android:layout_width="match_parent"
           android:layout_height="match_parent" />
   ```

2. 通过PreviewView调用useCamera2Scan()方法，开始进行二维码扫描，该库配置了两种扫描模式：ZXING和MLKIT，默认使用ZXING。

   ```kotlin
   /**
    * 参数说明：
    * context：AppCompatActivity用于绑定Lifecycle
    * scanResult：扫描回调监听，子线程，处理UI或吐司，需要自行切到主线程
    * scannerSDK：配置扫描模式：ScannerSDK.ZXING 或 ScannerSDK.MLKIT
    */
   scanCameraPreview?.useCamera2Scan(
               this@MainActivity,
               this,
               ScannerSDK.MLKIT
           )
   ```

3. 暂时不支持循环扫描，如果需要重置扫描，调用reScan(scanResult: ScanResult? = null) 方法。注意：回调如果重置之后的处理和useCamera2Scan一致，建议和useCamera2Scan函数使用同一个。
4. 鉴于CameraX在使用时，已经绑定生命周期，所以并不需要使用者在页面层手动销毁相机实例。但是以防万一，还是暴露了destroyCamera2Scan()方法，用于手动销毁二维码扫描实例。

**二维码图片解析**

1. 需要使用者自己实现权限申请，并注意Android Q分区存储权限变更及适配，即在 AndroidManifest.xml文件中加上android:requestLegacyExternalStorage="true"。

2. 选择完图片后，可以直接调用analyzeQRCode2String(context: Context, imagePath: String)方法。在Android Q下，可直接传入content://media格式路径，方法里面已做处理。

   ```kotlin
   val r = analyzeQRCode2String(this@MainActivity, "图片地址")
   ```
