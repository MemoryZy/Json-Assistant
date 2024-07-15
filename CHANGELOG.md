# Json Assistant Plugin Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.1...HEAD
[1.2.1]: https://github.com/MemoryZy/Json-Assistant/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.2...v1.2.0
[1.1.2]: https://github.com/MemoryZy/Json-Assistant/compare/v1.1.0...v1.1.2
[1.1.0]: https://github.com/MemoryZy/Json-Assistant/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/MemoryZy/Json-Assistant/commits/v1.0.0
