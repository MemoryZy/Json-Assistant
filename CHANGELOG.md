# Json Assistant Plugin Changelog

## [Unreleased]

- 优化历史记录窗口。
- 增加 JSON 排序功能。
- 增加字符统计功能。
- 优化 JSON 树的过滤功能。
- 修复表格化错误的问题。
- 用户体验优化与改进。
- 兼容性优化。
- Optimize the history window.
- Add JSON sorting functionality.
- Add character counting functionality.
- Optimize the filtering functionality of the JSON tree.
- Fix tabulation error issues.
- User experience optimization and improvement.
- Compatibility optimization.

## [1.8.5] - 2025-08-10

- 更新历史记录展示形式。
- 切换识别并粘贴剪贴板数据的方式。
- 增加从文件导入 JSON 数据功能。
- 优化 JSON 转义功能的格式问题。
- 增加反序列化类型自动推导开关控制。
- 为 JSON 树结构增加编辑功能，并支持将变更同步至源文件。
- 优化插件性能。
- 用户体验优化与改进。
- 兼容性优化。
- Update the display format of the history record.
- Switch the method of recognizing and pasting clipboard data.
- Add the function of importing JSON data from files.
- Optimize the format issues of the JSON escape function.
- Added switch control for automatic type inference during deserialization.
- Enhanced JSON tree structure with editing capabilities and source file synchronization support.
- Optimize the performance of the plugin.
- User experience optimization and improvement.
- Compatibility optimization.

## [1.8.2] - 2025-06-16

- 支持选择部分字段进行序列化为JSON。
- 支持将 JSON 以表格视图展示。
- 大幅提升扩展功能（如时间戳转换等）的执行速度。
- 解决运行时的大型对象转 JSON 时的窗口UI冻结问题。
- Support the selection of certain fields for serialization into JSON.
- Support displaying JSON in a table view.
- Significantly enhance the execution speed of extended functions such as timestamp conversion, etc.
- Solve the problem of window UI freezing when converting large objects to JSON at runtime.

## [1.8.0] - 2025-04-16

- 新增 JavaBean 转 JSON5（包含属性注释）功能。
- 新增 运行时对象 转 JSON5（包含属性注释）功能。
- 在 JSON 树结构中新增注释展示功能。
- 支持 JSON 反序列化为 JavaBean 时带上注释或 Swagger 注解。
- 增加 时间转时间戳 的意图操作和全局操作。
- 解决 JSON5 处理时的大数值丢失精度问题。
- 优化时间戳转换功能中数值识别的逻辑。
- 优化 JSON 反序列化为 JavaBean 功能的实现方式，提升效率。
- 优化 Java 常量转为 JSON 功能中的判断逻辑，支持更多方式定义的常量。
- 用户体验优化与改进。
- 兼容性优化。
- BUG修复。
- Added functionality to convert JavaBeans to JSON5 (including property comments).
- Added functionality to convert runtime objects to JSON5 (including property comments).
- Added a comment display feature to the JSON tree structure.
- Supports JSON deserialization to Javabeans with comments or Swagger annotations.
- Increased the intent operation and global operation for converting time to timestamp.
- Solve the problem of losing precision of large values in json5 processing.
- Optimize the logic for numeric recognition in the timestamp conversion functionality.
- Optimize the implementation of JSON deserialization to JavaBean functionality to improve efficiency.
- Optimize the judgment logic in the functionality of converting Java constants to JSON, supporting constants defined in more ways.
- User experience optimization and improvement.
- Compatibility optimization.
- BUG fixes.

## [1.7.8] - 2025-03-25

- 将历史记录保存切换为手动模式。
- 解决【运行时对象转 JSON】功能中无法解析内部类的问题。
- 解决时间戳转换的问题。
- 解决序列化时的类的嵌套递归问题。
- 解决之前的遗留问题。
- 优化 XML 文本判断逻辑。
- 用户体验优化与改进。
- 兼容性优化。
- Switch history saving to manual mode.
- Fixed an issue where inner classes could not be handled in the runtime Object to JSON function.
- Resolve timestamp conversion issues.
- Solves the problem of nested recursion of classes when serializing.
- Solve the remaining problems.
- Optimize XML text judgment logic.
- User experience optimization and improvement.
- Compatibility optimization.

## [1.7.7] - 2025-03-06

- 检测 JSON 中是否存在时间戳，并提供了时间戳转为时间格式的快速修复功能。
- 新增对比窗口两侧标题的编辑功能。
- 在工具窗口编辑器中新增自动导入剪贴板数据前的确认提示。
- 新增两项扩展功能
  - 将 JSON 内所有时间戳转为时间格式。
  - 将 JSON 内所有嵌套 JSON 字符串转为 JSON 对象。
- 解决插件兼容问题。
- 用户体验优化与改进。
- BUG修复。
- Detects whether timestamp exists in JSON, and provides a quick repair function to convert timestamp into time format.
- Added the ability to edit the titles on both sides of the comparison window.
- Added a confirmation prompt before automatically importing clipboard data in the Tool Window Editor.
- Two new extensions
  - Converts all timestamps in JSON to time format.
  - Converts all nested JSON strings in JSON to JSON objects.
- Solve plugin compatibility issues.
- User experience optimization and improvement.
- BUG fixes.
- 解决在 GoLand、PyCharm 平台上的兼容问题。
- Resolve compatibility issues on GoLand and PyCharm platforms.

## [1.7.7-beta] - 2025-02-22

- 解决在 GoLand、PyCharm 平台上的兼容问题。
- Resolve compatibility issues on GoLand and PyCharm platforms.

## [1.7.6] - 2025-02-22

- 新增了从 JSON 树结构中提取节点路径的功能。
- 新增了从运行环境中提取对象为 JSON 的功能。
- 新增了 JSON 对比的优化排序功能。
- 优化了反序列化配置选项。
- 优化了历史记录，增加了名称指定功能。
- 实现了对嵌套 JSON 结构的智能感知能力。
- 当选定目标为对象实例或对象、数组、集合时，类序列化将以此对象实例或对象、数组、集合代表的对象为准 [#52](https://github.com/MemoryZy/Json-Assistant/issues/52)。
- 修复了配置持久化过程中因非法字符导致的错误。
- 用户体验优化与改进。
- BUG修复。
- Added the capability to extract node paths from JSON tree structures.
- Added the ability to extract objects as JSON from a running environment.
- Added optimized sorting function for JSON comparison.
- Optimized deserialization configuration options.
- Optimized history and added name assignment.
- Implemented intelligent detection capabilities for nested JSON structures.
- When an object instance or object, array, or collection is selected as the target, the class serialization takes the object represented by this object instance or object, array, or collection as the standard [#52](https://github.com/MemoryZy/Json-Assistant/issues/52).
- Fixed the error caused by invalid characters during configuration persistence.
- User experience optimization and improvement.
- BUG fixes.

## [1.7.5] - 2025-01-19

- 支持 JMESPath 查询语言。
- 支持 JSON 树视图多展示形式切换。
- 反序列化支持 Jackson、FastJSON、Lombok 相关注解（可选）。
- 增加 JSON 历史记录的开关功能。
- 解决 格式化后的JSON无法保留原始的数字表现 （[#47](https://github.com/MemoryZy/Json-Assistant/issues/47)） 问题。
- 优化 JavaBean 序列化为 JSON 的逻辑，确保生成的 JSON 对象中的属性顺序与属性的声明顺序一致。
- 解决 [#41](https://github.com/MemoryZy/Json-Assistant/issues/41) 问题。
- Support for JMESPath query language.
- Support JSON tree view multi-display mode switch.
- Deserialization supports Jackson, FastJSON, Lombok annotations (optional).
- Added the ability to switch JSON history.
- Resolved that formatted JSON does not retain its original numeric representation ([#47](https://github.com/MemoryZy/Json-Assistant/issues/47)).
- Optimize the JavaBean-to-JSON serialization process to preserve the order of properties according to their declaration sequence in the Java class.
- Solve problem [#41](https://github.com/MemoryZy/Json-Assistant/issues/41).

## [1.7.2] - 2024-11-30

- 完全支持 [JSON5](https://json5.org/)。
- 解决 [#31](https://github.com/MemoryZy/Json-Assistant/issues/31) 问题。
- 新增 URL 参数 与 JSON 互转。
- 新增 Properties 格式参数与 JSON 互转。
- 新增 JSON 转义功能。
- 增加树状历史记录视图，可按日期分组查看记录。
- 新增工具窗口背景色自定义功能。
- 兼容2024.3版本。
- 用户体验优化与改进。
- BUG修复。
- Full support for [JSON5](https://json5.org/).
- Solve problem [#31](https://github.com/MemoryZy/Json-Assistant/issues/31).
- Added URL parameter interconversion with JSON.
- Added the Properties format parameter to interconvert with JSON.
- Added JSON escape function.
- Added a tree history view to view records grouped by date.
- Added the tool window background color customization function.
- Compatible with version 2024.3.
- User experience optimization and improvement.
- BUG fixes.

## [1.6.7] - 2024-10-27

- 优化插件配置项，新增以下开关配置（属性序列化时）
  - 序列化时是否包含随机值 ([#28](https://github.com/MemoryZy/Json-Assistant/issues/28))。
  - 序列化时是否识别 FastJson 相关注解。
  - 序列化时是否识别 Jackson 相关注解。
- 更改侧边栏工具窗口名称及图标为插件的名称与图标。
- 用户体验优化。
- Optimize plug-in configuration items, add the following switch configuration (when serializing properties)
  - Whether random values are included in serialization ([#28](https://github.com/MemoryZy/Json-Assistant/issues/28)).
  - Whether FastJSON related annotations are recognized during serialization.
  - Whether to recognize Jackson related annotations during serialization.
- Change the sidebar tool window name and icon to the plugin name and icon.
- User experience optimization.

## [1.6.5] - 2024-09-30

- 修复工具窗口 JSON 编辑器无法保存状态问题。
- 兼容 IDEA 2024.3 eap版本。
- 支持 Yaml 与 Json 互相转换，并支持多文档 Yaml 转换。
- 支持 Toml 与 Json 互相转换。
- 新增将 Java 字符串常量提取为 JSON 功能。
- 新增插件异常提交工具。
- 修复加载最新历史记录功能的问题。
- 用户体验优化。
- Fixed an issue where tool window JSON editor could not save state.
- Compatible with IDEA 2024.3 eap version.
- Supports Yaml and Json conversion, and supports multi-document Yaml conversion.
- Supports conversion between Toml and Json.
- Added the ability to extract Java string constants into JSON.
- Added the plug-in exception submission tool.
- Fixed an issue with loading the latest history feature.
- User experience optimization.

## [1.6.0] - 2024-09-03

- 解决后台保存历史记录的问题。
- 将 Json 展示窗口由单窗口切换为多选项卡多窗口，并新增多个操作项。
  - 侧边栏快捷操作项
  - 新增选项卡
  - 选项卡更名
  - 移动选项卡到主编辑器
  - 将选项卡到新窗口打开
- 优化输入框警告（弹出警告后，继续输入可取消警告）。
- 优化旧操作项逻辑。
- 用户体验优化。
- Switch the Json display window from single window to multi-tabbed multi-window and add multiple action items.
  - Sidebar shortcut action items
  - Add Tab
  - Rename Tab
  - Move Tab to Editor
  - Move Tab to New Window
- Optimize input box warnings (After a warning pops up, continue typing to cancel the warning).
- Optimize old action item logic.
- User experience optimization.

## [1.5.1] - 2024-08-21

- 新增 JSONPath 历史记录功能。
- 解决属性序列化时的无限递归问题。
- 优化历史记录保存逻辑，避免保存太多临时性JSON。
- 在 JSON 结构对话框中区分单数与复数术语 ([#21](https://github.com/MemoryZy/Json-Assistant/pull/21))。
- 用户体验优化。
- Added the JSONPath history function.
- Solves the problem of infinite recursion when serializing properties.
- Optimize the history saving logic to avoid storing too much temporary JSON.
- Distinguish singular or plural terms in the JSON structure dialog ([#21](https://github.com/MemoryZy/Json-Assistant/pull/21)).
- User experience optimization.

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

[Unreleased]: https://github.com/MemoryZy/Json-Assistant/compare/v1.8.5...HEAD
[1.8.5]: https://github.com/MemoryZy/Json-Assistant/compare/v1.8.2...v1.8.5
[1.8.2]: https://github.com/MemoryZy/Json-Assistant/compare/v1.8.0...v1.8.2
[1.8.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.8...v1.8.0
[1.7.8]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.7...v1.7.8
[1.7.7]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.7-beta...v1.7.7
[1.7.7-beta]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.6...v1.7.7-beta
[1.7.6]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.5...v1.7.6
[1.7.5]: https://github.com/MemoryZy/Json-Assistant/compare/v1.7.2...v1.7.5
[1.7.2]: https://github.com/MemoryZy/Json-Assistant/compare/v1.6.7...v1.7.2
[1.6.7]: https://github.com/MemoryZy/Json-Assistant/compare/v1.6.5...v1.6.7
[1.6.5]: https://github.com/MemoryZy/Json-Assistant/compare/v1.6.0...v1.6.5
[1.6.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.5.1...v1.6.0
[1.5.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.1...v1.3.0
[1.2.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.2...v1.2.0
[1.1.2]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.0...v1.1.2
[1.1.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/MemoryZy/Json-Assistant/commits/v1.0.0
