# Json Assistant Plugin Changelog

## [Unreleased]

## [1.5.0] - 2024-08-15

- 增加 JSONPath 匹配 JSON 功能。
- 增加 JSON 历史记录功能。
- 增加 JSON 文本比对功能。
- 插件性能优化。
- 增设快捷入门指南。
- Added the JSONPath matching JSON function.
- Added JSON history function.
- Added JSON text comparison function.
- Plug-in performance optimization.
- Added the quick Start Guide.

## [1.4.0] - 2024-08-06

- Json 与 其他格式（例如 XML）的互相转化功能
- Json 文本转义处理
- 支持快速修改操作快捷键
- 增加入门指南入口
- 插件运行效率优化
- 功能界面优化
- The ability to convert JSON to and from other formats, such as XML.
- Json text escape processing.
- Supports quick change of operation shortcut keys.
- Added Getting Started Guide entry.
- Plug-in efficiency optimization.
- Functional interface optimization.

## [1.3.1] - 2024-07-30

- 增加 Json Viewer 窗口的历史记录、从剪贴板解析 Json 功能。
- 增强 Json 序列化中对于 List、数组 的支持。
- 增加插件首次安装的欢迎弹窗、更新插件弹窗。
- 处理低版本IDE兼容性。
- BUG解决。
- 用户体验优化。
- Added Json Viewer window history and Json parsing from the clipboard.
- Enhanced support for lists and arrays in Json serialization.
- Added the welcome popup window for the first installation of the plugin and the pop-up window for updating the plugin.
- Handle low version IDE compatibility.
- Bug fixes.
- User experience optimization.

## [1.3.0] - 2024-07-19

- 增强格式化、压缩、树化等基础功能，在原先的基础上增加了新的能力：选中 Json 文本进行操作。
- 处理 （Json 反序列化为 Java 类） 功能入口展示时机的问题，使其与 （New -> Java Class） 同步。
- 自定义 IDE 新 UI 图标支持。
- 解决 （Json 反序列化为 Java 类） 功能无法转小数的问题，并自动选择性导入 java.util.Date、java.util.List 等类。
- 解决 （Json 树结构）窗口无法展示数组嵌套数组的问题。
- 解决 （Json 树结构）窗口无法高亮显示数组下的元素的问题。
- 为 （Json 反序列化为 Java 类）功能生成的内部类加上 static 关键字。
- 解决 Json 判断不准确问题。
- 部分展示文本优化。
- 优化插件部分展示文本，增加 Json Assistant 插件标识。
- Enhanced formatting, compression, tree and other basic features, on the basis of the original added a new ability: select Json text to operate.
- Handles the timing of the deserialization feature entry display, synchronizing it with (New -> Java Class).
- Custom IDE new UI icon support.
- Solve the problem that the deserialization function cannot convert decimals, and automatically import java.util.Date, java.util.List and other classes selectively.
- Fixed an issue where the Json tree structure window could not display nested arrays.
- Fixed an issue where the Json tree structure window could not highlight elements under an array.
- Add the static keyword to the inner class generated for deserialization.
- Resolve the problem of inaccurate Json judgment.
- Part shows text optimization.
- Optimize the text displayed in the plug-in section and add the Json Assistant plug-in identifier.

## [1.2.1] - 2024-07-15

- BUG 解决
- 增强 FastJson、Jackson 注解支持
- 序列化时增加被忽略属性提示
- JavaBean 进行 Json 序列化时判断是否存在 `transient` 关键字
- Kotlin 属性进行 Json 序列化时判断是否存在 `kotlin.jvm.Transient`
- Bug fixes.
- Enhanced FastJson and Jackson annotation support.
- Added hints for ignored attributes when serializing.
- JavaBean determines the presence of the `transient` keyword during JSON serialization.
- When using Kotlin properties for JSON serialization, check if there is `kotlin. jvm.Transient`.

## [1.2.0] - 2024-07-12

- Bug修复
- 用户体验优化
- 实现 Kotlin 属性转 JSON 功能
- Bug fixes.
- User experience optimization.
- Added Kotlin attribute to JSON functionality.

## [1.1.2] - 2024-07-08

- Optimization function
- Added Json to JavaBean functionality
- The JavaBean to Json function adds internal class judgment (When the focus is on the inner class, the inner class is converted to Json)
- 优化功能
- 增加Json转为JavaBean功能
- JavaBean转Json功能增加内部类判断（当焦点处于内部类，优先将内部类转为Json）

## [1.1.0] - 2024-07-03

- New feature: Convert JavaBean to Json.
- Change the Json function entry to the popup menu.
- Optimize icon
- 功能新增：将JavaBean转为Json
- 修改 Json 功能入口为弹出菜单
- 优化图标

## [1.0.0] - 2024-06-29

- Initial plugin project setup.
- Provides core plugin functionality.
- Supports major IDEs.
- Supports multi-language internationalization.
- 初始化插件项目
- 提供核心插件功能
- 支持主要的IDE
- 多语言国际化

[Unreleased]: https://github.com/MemoryZy/Json-Assistant/compare/v1.5.0...HEAD
[1.5.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.1...v1.3.0
[1.2.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.2...v1.2.0
[1.1.2]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.0...v1.1.2
[1.1.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/MemoryZy/Json-Assistant/commits/v1.0.0
