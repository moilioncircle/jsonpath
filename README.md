# jsonpath

a sexy json reading like the xml-sax.


## 适用场景

目前，JSON的读操作多采用了类似XML的DOM方式，
即使只读一个字段，也要全部加载和解析，很不经济。

jsonpath，采用类似XML的SAX方式，按需读取，
特别适合，读取的属性数不超过总属性20%的场景。

jsonpath的特点就是：速度快，省资源，规则简单。

## 不适用场景

  * JSON写操作。
  * JSON序列化和反序列化。
  * JSON读取属性数超过总属性的50%。


## 参考资料

[JSON Pointer :RFC6901](http://tools.ietf.org/html/rfc6901)
