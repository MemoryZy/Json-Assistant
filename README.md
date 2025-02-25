# Json Assistant

> **English / [简体中文](./README_zh.md)**

<br/>

JSON Tool Plugin based on IntelliJ IDEs.It makes JSON processing a breeze!

基于 IntelliJ IDEs 的 JSON 工具插件，让 JSON 处理变得更轻松！

[![Homepage][shields:hp]][jb:plugin-link]
&nbsp;
[![Version][shields:version]][jb:version]
&nbsp;
[![Build][shields:build]][gh:build]
&nbsp;
[![Downloads][shields:download]][jb:plugin-link]


<br/>


## Key Features
- Support [JSON5](https://json5.org/)
- JSON Window
  - History
  - JSONPath/JMESPath Evaluator
- JSON Processing
  - Beautify
  - Minify
  - Tree View
  - Compare
  - Escape
  - Expand Nested JSON
- Serialization
  - JavaBean to JSON
  - Runtime Objects to JSON
  - JSON to JavaBean
  - Kotlin Properties to JSON
  - Extract Java Constants to JSON
- Format Conversion
  - XML
  - YAML
  - Toml
  - Properties
  - URL Params

<br/>

## Getting Started 🚀
When in or selecting JSON data, you can press the shortcut key `Alt+K` or right-click and choose `Json Assistant` to bring up the action menu.

For more operations, please refer to the [plugin documentation][plugin:docs].


<br/>

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Json Assistant"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release][gh:release] and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

<br/>

## Feedback
If you find any problem during use, you can submit [Issue][gh:issue] and [PR][gh:pr] at any time, thank you for your support.

<br/>

## Donors List

Heartfelt thanks to every donor who supports us! Your generosity helps us improve and enhance the project. Thank you for your support!❤️

| Name       | Website Link | Accompanying Message | Total Donation |
|------------|--------------|----------------------|----------------|
| ت          | N/A          | Json插件很好用，加油         | 6.66 CNY       |
| *瑞         | N/A          | N/A                  | 5.00 CNY       |
| JiaoJunWei | N/A          | N/A                  | 5.00 CNY       |
| 广柔散人       | N/A          | good                 | 6.66 CNY       |
| One        | N/A          | N/A                  | 6.66 CNY       |



[shields:hp]: https://img.shields.io/badge/Jetbrains%20Plugin-Json%20%20Assistant-4285F4.svg?style=Plastic&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNCIgaGVpZ2h0PSIxNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48ZyBmaWxsPSJub25lIj48cGF0aCBkPSJNMjQgMHYyNEgwVjB6TTEyLjU5MyAyMy4yNThsLS4wMTEuMDAybC0uMDcxLjAzNWwtLjAyLjAwNGwtLjAxNC0uMDA0bC0uMDcxLS4wMzVxLS4wMTYtLjAwNS0uMDI0LjAwNWwtLjAwNC4wMWwtLjAxNy40MjhsLjAwNS4wMmwuMDEuMDEzbC4xMDQuMDc0bC4wMTUuMDA0bC4wMTItLjAwNGwuMTA0LS4wNzRsLjAxMi0uMDE2bC4wMDQtLjAxN2wtLjAxNy0uNDI3cS0uMDA0LS4wMTYtLjAxNy0uMDE4bS4yNjUtLjExM2wtLjAxMy4wMDJsLS4xODUuMDkzbC0uMDEuMDFsLS4wMDMuMDExbC4wMTguNDNsLjAwNS4wMTJsLjAwOC4wMDdsLjIwMS4wOTNxLjAxOS4wMDUuMDI5LS4wMDhsLjAwNC0uMDE0bC0uMDM0LS42MTRxLS4wMDUtLjAxOS0uMDItLjAyMm0tLjcxNS4wMDJhLjAyLjAyIDAgMCAwLS4wMjcuMDA2bC0uMDA2LjAxNGwtLjAzNC42MTRxLjAwMS4wMTguMDE3LjAyNGwuMDE1LS4wMDJsLjIwMS0uMDkzbC4wMS0uMDA4bC4wMDQtLjAxMWwuMDE3LS40M2wtLjAwMy0uMDEybC0uMDEtLjAxeiIvPjxwYXRoIGZpbGw9IndoaXRlIiBkPSJNMiA5YTMgMyAwIDAgMSAzLTNoMi44NTNjLjI5NyAwIC40OC0uMzA5LjM2Ni0uNTgzQTIuNSAyLjUgMCAwIDEgOC4wODMgNWMtLjMzMS0xLjQ4Ny43OTItMyAyLjQxNy0zYzEuNjI2IDAgMi43NDggMS41MTMgMi40MTcgM2EyLjUgMi41IDAgMCAxLS4xMzYuNDE3Yy0uMTE1LjI3NC4wNjkuNTgzLjM2Ni41ODNIMTVhMyAzIDAgMCAxIDMgM3YxLjg1M2MwIC4yOTcuMzA4LjQ4LjU4My4zNjZjLjEzNS0uMDU2LjI3My0uMTA0LjQxNy0uMTM2YzEuNDg3LS4zMzEgMyAuNzkxIDMgMi40MTdzLTEuNTEzIDIuNzQ4LTMgMi40MTdhMi41IDIuNSAwIDAgMS0uNDE3LS4xMzZjLS4yNzQtLjExNS0uNTgzLjA2OS0uNTgzLjM2NlYxOWEzIDMgMCAwIDEtMyAzaC0xLjg5M2MtLjI4OCAwLS40NzMtLjI5MS0uMzktLjU2NnEuMDYzLS4yMS4wODUtLjQzNGEyLjMxIDIuMzEgMCAxIDAtNC42MDQgMHEuMDIxLjIyNC4wODYuNDM0Yy4wODIuMjc1LS4xMDMuNTY2LS4zOS41NjZINWEzIDMgMCAwIDEtMy0zdi0yLjg5M2MwLS4yODguMjkxLS40NzMuNTY2LS4zOXEuMjEuMDYzLjQzNC4wODVhMi4zMSAyLjMxIDAgMSAwIDAtNC42MDRxLS4yMjQuMDIxLS40MzQuMDg2Yy0uMjc1LjA4Mi0uNTY2LS4xMDMtLjU2Ni0uMzl6Ii8+PC9nPjwvc3ZnPg==
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
