# Json Assistant

> **[English](./README.md) / 简体中文**

<br/>

基于 IntelliJ IDEs 的 JSON 工具插件，让 JSON 处理变得更轻松！

[![Homepage][shields:hp]][jb:plugin-link]
&nbsp;
[![Version][shields:version]][jb:version]
&nbsp;
[![Build][shields:build]][gh:build]
&nbsp;
[![Downloads][shields:download]][jb:version]

<br/>

## 功能 
- 完全支持 [JSON5][json5]
- JSON 窗口
  - 历史记录
  - JSONPath/JMESPath 查询
- JSON 处理
  - 美化
  - 压缩
  - 树视图
  - 比较
  - 转义
  - 展开嵌套 JSON
- 序列化
  - JavaBean 转换为 JSON
  - 运行时对象转换为 JSON
  - JSON 转换为 JavaBean
  - Kotlin 属性转为 JSON
  - 提取 Java 常量为 JSON
- 格式转换
  - XML
  - YAML
  - Toml
  - Properties
  - URL Params

<br/>

## 兼容
 - Android Studio — Arctic Fox | 2020.3.1+
 - AppCode — 2020.3+
 - Aqua — 2024.1.1+
 - CLion — 2020.3+
 - Code With Me Guest — 1.0+
 - DataGrip — 2020.3+
 - DataSpell — 2021.3+
 - GoLand — 2020.3+
 - IntelliJ IDEA Community — 2020.3+
 - IntelliJ IDEA Ultimate — 2020.3+
 - JetBrains Client — 1.0+
 - JetBrains Gateway — 2022.2+
 - MPS — 2020.3+
 - PhpStorm — 2020.3+
 - PyCharm Community — 2020.3+
 - PyCharm Professional — 2020.3+
 - Rider — 2020.3+
 - RubyMine — 2020.3+
 - RustRover — 2024.1+
 - WebStorm — 2020.3+
 - Writerside — 2024.1+

<br/>

## 安装

- 使用 IDE 内置插件系统：

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>搜索 "Json Assistant"</kbd> >
  <kbd>Install</kbd>


- 手动安装:

  在 [JetBrains Marketplace][jb:plugin-link] 或 [GitHub Releases][gh:release] 下载与 IDE 版本兼容的插件包，并按照以下流程安装：

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd> > <kbd>选择插件包并进行安装（无需解压 zip）</kbd> 

<br/>

## 使用

> 


<br/>

## 反馈
> 使用过程中发现任何问题，可随时提交 [Issue][gh:issue] 和 [PR][gh:pr]，感谢支持。

<br/>

## 捐赠列表

| 名称         | 网站链接 | 捐赠总额     |
|------------|------|----------|
| ت          | 无    | 6.66 CNY |
| *瑞         | 无    | 5.00 CNY |
| JiaoJunWei | 无    | 5.00 CNY |

[shields:hp]: https://img.shields.io/badge/Jetbrains%20Plugin-Json%20%20Assistant-4285F4.svg?style=Plastic&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTEwIDE5di01aDR2NWMwIC41NS40NSAxIDEgMWgzYy41NSAwIDEtLjQ1IDEtMXYtN2gxLjdjLjQ2IDAgLjY4LS41Ny4zMy0uODdMMTIuNjcgMy42Yy0uMzgtLjM0LS45Ni0uMzQtMS4zNCAwbC04LjM2IDcuNTNjLS4zNC4zLS4xMy44Ny4zMy44N0g1djdjMCAuNTUuNDUgMSAxIDFoM2MuNTUgMCAxLS40NSAxLTEiLz48L3N2Zz4=
[shields:build]: https://github.com/MemoryZy/Json-Assistant/workflows/Build/badge.svg
[shields:version]: https://img.shields.io/jetbrains/plugin/v/24738-json-assistant.svg?label=Version&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNCIgaGVpZ2h0PSIxNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTIwLjUwMiA1LjkyMkwxMiAxTDMuNDk4IDUuOTIyTDEyIDEwLjg0NXpNMi41IDcuNjU2VjE3LjVsOC41IDQuOTIxdi05Ljg0NHpNMTMgMjIuNDJsOC41LTQuOTIxVjcuNjU2bC04LjUgNC45MnoiLz48L3N2Zz4=
[shields:download]: https://img.shields.io/jetbrains/plugin/d/24738-json-assistant.svg?label=Download&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNCIgaGVpZ2h0PSIxNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0ibTEyIDE2bC01LTVsMS40LTEuNDVsMi42IDIuNlY0aDJ2OC4xNWwyLjYtMi42TDE3IDExem0tNiA0cS0uODI1IDAtMS40MTItLjU4N1Q0IDE4di0zaDJ2M2gxMnYtM2gydjNxMCAuODI1LS41ODcgMS40MTNUMTggMjB6Ii8+PC9zdmc+
[jb:plugin-link]: https://plugins.jetbrains.com/plugin/24738-json-assistant
[gh:pr]: https://github.com/MemoryZy/Json-Assistant/pulls
[gh:issue]: https://github.com/MemoryZy/Json-Assistant/issues/new/choose
[gh:release]: https://github.com/MemoryZy/Json-Assistant/releases/latest
[jb:version]: https://plugins.jetbrains.com/plugin/24738-json-assistant/versions
[gh:build]: https://github.com/MemoryZy/Json-Assistant/actions/workflows/build.yml
[plugin:docs]: https://json.memoryzy.cn/overview
[json5]: https://json5.org/
