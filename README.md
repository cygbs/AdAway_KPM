# ![AdAway logo](app/src/main/res/mipmap-mdpi/icon.png) AdAway（KPM / KernelPatch 定制版）

本项目是 [AdAway](https://github.com/AdAway/AdAway) 的一个 **Fork（定制分支）**，面向使用
**KernelPatch / APatch（KPM）** 做 hosts 重定向的 Root 用户，去掉了不必要的能力并针对该场景做了增强。

> 📄 原版说明文档（Upstream README）：<https://github.com/AdAway/AdAway/blob/master/README.md>

AdAway 本身是一个基于 hosts 文件的开源 Android 广告拦截器。本 Fork 只保留并强化了
**Root + KPM hosts 重定向** 这一条路径。

---

## ⚠️ 重要依赖：KPM 内核模块

本版本**依赖**一个 KernelPatch 内核模块 `hosts_file_redirect`：它 hook 内核
`do_filp_open`，把 root 进程对 `/system/etc/hosts` 的打开**重定向**到 `/data/adb/hosts`。
据此，本版本把 hosts 直接写入 `/data/adb/hosts`，**不改动系统分区、不做 remount、不动 boot**。

KPM 模块下载地址：

### 👉 <https://github.com/AndroidPatch/kpm/releases>

（在该 Release 页面下载 `hosts_file_redirect_x.y.z.kpm`，通过 KernelPatch / APatch 的管理器加载。）

同时，为了让系统解析器（`netd`）能读取 `/data/adb/hosts`，需要在 **保持 SELinux Enforcing**
的前提下放行 netd 读取 `/data/adb`。本版本已内置该逻辑（见下文「新增功能」），**无需再单独安装
SELinux 规则模块**。

---

## 与原版的差异

### 1. 移除的内容
- **移除 Sentry（遥测 / 崩溃上报）**：删除 `sentrystub` 模块与全部 `io.sentry` 依赖、初始化代码
  及清单元数据；设置中的「遥测」开关保留但**置灰不可点**。
- **移除 VPN 模式**：删除 `VpnService`、DNS 代理、worker、VPN 排除应用界面，以及
  `pcap4j` / `dnsjava` / `slf4j` / `okhttp-dnsoverhttps` 等依赖；设置中的「VPN 广告拦截」
  与欢迎页的 VPN 卡片保留但**置灰、不可点**。
- 相应删除 `AdBlockMethod.VPN`、VPN 通知渠道、VPN 相关错误枚举/偏好等。

### 2. Root 模式的改动
- 生成 hosts 后**直接写入 `/data/adb/hosts`**（KPM 的重定向目标），
  **删除了 `remount` / `isWritable` 等重新挂载逻辑**，不再触碰 `/system` 分区。
- 打开 hosts 文件、检测是否已应用等操作同样指向 `/data/adb/hosts`。

### 3. 新增功能
- **内置 SELinux 规则集成**：应用每次启动都会检查
  `/data/adb/post-fs-data.d/adaway_hosts_selinux.sh`，不存在则创建并立即应用。脚本在开机
  `post-fs-data` 阶段由 APatch 自动执行，注入两条 `allow` 规则：
  ```
  allow netd adb_data_file dir search
  allow netd adb_data_file file { read getattr map open }
  ```
  该方式**始终保持 SELinux 为 Enforcing**，不需要宽容（permissive）模式。
- **抽屉新增「SELinux 规则」页面**：展示上述脚本的路径与内容，以及创建/应用状态。
- **抽屉新增「KPM 内核模块」页面**：检测内核是否已加载 `hosts_file_redirect` 及其启用状态，
  并展示模块信息（名称 / 版本 / 许可 / 作者 / 描述 / 加载来源）。检测方式为
  **功能探测 + 内核日志解析**，内容无关、结果可靠。

### 4. 修复的问题
- **修复 webserver 被冻结**：内置 HTTP 服务器原本与 App 处于同一 freezer cgroup，
  App 进入后台被系统缓存时会把服务器一并冻结，导致请求无响应、时好时坏。现在启动后会把进程
  移出该 cgroup（`/dev/cpufreezer/cgroup.procs`），服务器不再随 App 被冻结。`tcpdump` 同样受益。
- **webserver 增加 IPv6 监听**：除 `127.0.0.1` 外同时监听 `[::1]`，解决 `localhost` 在
  IPv6 优先的客户端下连接被拒的问题。
- **修复原生可执行文件打包**：原构建在 clean 构建时会把空 stub 打进 APK，导致 webserver /
  tcpdump 实际无法运行；现改为将可执行文件打包到确定性的 `jniLibs` 生成目录，构建稳定可靠。
- 菜单文案修正：`Github 项目` → `GitHub 项目`。

---

## 需求与安装
- Android 8 _Oreo_（API 26）及以上
- 已 Root，并使用 **KernelPatch / APatch**，且已加载 `hosts_file_redirect` KPM 模块
  （下载见 <https://github.com/AndroidPatch/kpm/releases>）
- 安装后首次启动会请求 Root 权限，用于创建/应用 SELinux 规则脚本与启动内置 webserver

> 本 Fork 移除了 VPN 模式与遥测，请只从可信来源获取安装包。

## 构建
```bash
git clone <your-fork-url>
cd AdAway
# 需要 JDK 17+、Android SDK（compileSdk 34、build-tools 34、NDK 27.2.12479018）
# 在 local.properties 中设置 sdk.dir，或导出 ANDROID_HOME / ANDROID_SDK_ROOT
./gradlew :app:assembleDebug     # 调试包（Debug 签名，可直接安装）
./gradlew :app:assembleRelease   # 发布包（未签名，需自行签名）
```

## 许可证
本项目沿用原项目的 **GPLv3+** 许可，完整文本见 [LICENSE.md](LICENSE.md)。

## 致谢与链接
- 原版项目：<https://github.com/AdAway/AdAway>
- **原版 README**：<https://github.com/AdAway/AdAway/blob/master/README.md>
- 原版官网：<https://adaway.org>
- **KPM 模块下载**：<https://github.com/AndroidPatch/kpm/releases>
