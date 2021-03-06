# ZoomCropImage
An Android library for zooming and cropping image.

![Demo](https://github.com/CrazyOrr/ZoomCropImage/blob/master/screenshots/demo.gif)

## Download
### Gradle
```gradle
compile 'com.github.crazyorr:zoom-crop-image:0.1.2'
```

### Maven
```xml
<dependency>
    <groupId>com.github.crazyorr</groupId>
    <artifactId>zoom-crop-image</artifactId>
    <version>0.1.2</version>
</dependency>
```

## Usage
Import library module and add it as dependency
```java
// call up ZoomCropImageActivity
Intent intent = new Intent(this, ZoomCropImageActivity.class);
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_URI, originImageUri);
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_OUTPUT_WIDTH, PICTURE_WIDTH);
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_OUTPUT_HEIGHT, PICTURE_HEIGHT);
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_CROP_SHAPE, CropShape.SHAPE_OVAL);   //optional
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_SAVE_DIR,
        Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName());   //optional
intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_FILE_NAME, "cropped.png");   //optional
startActivityForResult(intent, REQUEST_CODE_CROP_PICTURE);

// get result in onActivityResult callback
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
    case REQUEST_CODE_CROP_PICTURE:
            switch(resultCode){
                case ZoomCropImageActivity.CROP_SUCCEEDED:
                    if (data != null) {
                        Uri croppedImageUri = data
                                .getParcelableExtra(ZoomCropImageActivity.INTENT_EXTRA_URI);
                        // use cropped image to do things ...
                      }
                    break;
                case ZoomCropImageActivity.CROP_CANCELLED:
                case ZoomCropImageActivity.CROP_FAILED:
                    break;
            }
            break;
    }
}
```

## Credits
* [hongyangAndroid][1]'s [Android 高仿微信头像截取 打造不一样的自定义控件][2]
[1]: https://github.com/hongyangAndroid
[2]: http://blog.csdn.net/lmj623565791/article/details/39761281

## License

    Copyright 2015 Lei Wang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/CrazyOrr/zoomcropimage/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

