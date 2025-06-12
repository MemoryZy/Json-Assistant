const fs = require('fs');
const path = require('path');
const hljs = require('highlight.js');

/**
 * 主函数：扫描报告目录，生成聚合HTML报告
 *
 * 主要流程：
 * 1. 定义报告目录路径
 * 2. 递归扫描报告目录结构
 * 3. 解析并收集报告内容
 * 4. 按产品版本排序报告
 * 5. 生成HTML报告
 * 6. 保存到文件
 */

// 定义报告目录路径（相对于当前脚本）
const reportsDir = path.resolve(__dirname, '../../reports');
// 存储所有报告文件的数组
const reportFiles = [];

console.log(`📂📂 开始扫描报告目录: ${reportsDir}`);

/**
 * 报告文件类型定义（使用数组保持顺序）
 * 格式：[文件名, 显示描述]
 *
 * 注意：
 * - 文件显示顺序由此数组顺序决定
 * - "废弃API使用"和"依赖项"将设置为默认折叠
 */
const reportFileTypes = [
    ['verification-verdict.txt', '验证结果'],
    ['compatibility-problems.txt', '兼容性问题'],
    ['internal-api-usages.txt', '内部API使用'],
    ['override-only-usages.txt', '仅重写API使用'],
    ['non-extendable-api-usages.txt', '不可扩展API使用'],
    ['invalid-plugin.txt', '无效插件错误'],
    ['compatibility-warnings.txt', '兼容性警告'],
    ['plugin-structure-warnings.txt', '插件结构警告'],
    ['experimental-api-usages.txt', '实验性API使用'],
    ['deprecated-usages.txt', '废弃API使用'],  // 设置为默认折叠
    ['dependencies.txt', '依赖项']             // 设置为默认折叠
];

/**
 * 递归查找所有报告文件
 *
 * 目录结构预期：
 * reports/
 *   [产品版本1]/
 *     reports/
 *       [插件名称]/
 *         [插件版本]/
 *           verification-verdict.txt
 *           ...其他报告文件
 *
 * @param {string} dir - 当前扫描的目录
 */
function findReportFiles(dir) {
    // 读取目录内容（包含文件类型信息）
    const entries = fs.readdirSync(dir, { withFileTypes: true });

    // 遍历目录中的所有条目
    for (const entry of entries) {
        const fullPath = path.join(dir, entry.name);

        if (entry.isDirectory()) {
            // 递归处理子目录
            findReportFiles(fullPath);
        } else if (entry.isFile() && entry.name === 'verification-verdict.txt') {
            /**
             * 找到验证结果文件（作为报告目录的标记）
             * 解析路径结构以获取产品版本、插件名称和插件版本
             */
            const pathParts = fullPath.split(path.sep);
            const baseIndex = pathParts.indexOf('reports');

            // 确保路径结构符合预期
            if (baseIndex !== -1 && pathParts.length > baseIndex + 3) {
                const productVersion = pathParts[baseIndex + 1];
                const pluginName = pathParts[baseIndex + 3];
                const pluginVersion = pathParts[baseIndex + 4];

                // 创建报告数据结构
                const reportData = {
                    productVersion,   // IDE产品版本
                    pluginName,       // 插件名称
                    pluginVersion,    // 插件版本
                    files: {},        // 存储报告文件内容
                    problemCounts: {} // 存储各类问题数量
                };

                // 获取当前报告目录（verification-verdict.txt所在的目录）
                const reportDir = path.dirname(fullPath);

                // 读取所有报告文件
                for (const [fileName, description] of reportFileTypes) {
                    const filePath = path.join(reportDir, fileName);

                    // 检查文件是否存在
                    if (fs.existsSync(filePath)) {
                        // 读取文件内容
                        let content = fs.readFileSync(filePath, 'utf8');

                        // 特别处理 deprecated-usages.txt 和 experimental-api-usages.txt
                        if (fileName === 'deprecated-usages.txt' || fileName === 'experimental-api-usages.txt') {
                            // 确保内容有换行符
                            if (content.trim() !== '') {
                                // 如果内容没有换行符，添加一个
                                if (content.indexOf('\n') === -1) {
                                    content += '\n';
                                }
                                // 确保每行都有换行符
                                else {
                                    content = content.split('\n').map(line => {
                                        return line.endsWith('\n') ? line : line + '\n\n';
                                    }).join('');
                                }
                            }
                        }

                        reportData.files[fileName] = {
                            content,
                            description
                        };

                        // 计算问题数量（排除验证结果和依赖项）
                        if (fileName !== 'verification-verdict.txt' &&
                            fileName !== 'dependencies.txt') {
                            // 计算非空行数作为问题数量
                            const lines = content.split('\n').filter(line => line.trim() !== '');
                            reportData.problemCounts[fileName] = lines.length;
                        }
                    }
                }

                // 计算总问题数（所有类型问题数量之和）
                reportData.totalProblems = Object.values(reportData.problemCounts).reduce((sum, count) => sum + count, 0);

                // 将报告添加到集合中
                reportFiles.push(reportData);
                console.log(`  找到报告: ${productVersion} (插件: ${pluginName}:${pluginVersion})`);
            }
        }
    }
}

/**
 * 获取本地化的北京时间
 * 格式：yyyy年MM月dd日 HH时mm分ss秒
 *
 * @returns {string} 格式化后的时间字符串
 */
function getLocalizedTime() {
    // 创建一个Date对象
    const now = new Date();

    // 手动计算北京时间（UTC+8）
    const utcTime = now.getTime() + (now.getTimezoneOffset() * 60000);
    const beijingTime = new Date(utcTime + (3600000 * 8));

    // 格式化为中文时间字符串
    return `${beijingTime.getFullYear()}年${
        (beijingTime.getMonth() + 1).toString().padStart(2, '0')
    }月${
        beijingTime.getDate().toString().padStart(2, '0')
    }日 ${
        beijingTime.getHours().toString().padStart(2, '0')
    }时${
        beijingTime.getMinutes().toString().padStart(2, '0')
    }分${
        beijingTime.getSeconds().toString().padStart(2, '0')
    }秒`;
}

/**
 * 生成HTML报告
 *
 * @param {Array} reports - 报告数据数组
 * @returns {string} HTML字符串
 */
function generateHTML(reports) {
    // 使用第一个报告的插件信息作为标题（如果存在）
    const mainPluginName = reports.length > 0 ? reports[0].pluginName : 'unknown';
    const mainPluginVersion = reports.length > 0 ? reports[0].pluginVersion : 'unknown';

    // 获取北京时间
    const formattedTime = getLocalizedTime();

    /**
     * HTML模板
     *
     * 结构：
     * 1. 头部信息（标题、样式、脚本）
     * 2. 报告概览统计
     * 3. 过滤和搜索控件
     * 4. 报告卡片列表
     * 5. 交互脚本（过滤、搜索、折叠）
     */
    return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>插件验证报告</title>

    <!-- 引入Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- 引入Bootstrap图标 -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.0/font/bootstrap-icons.css">
    <!-- 引入代码高亮样式 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/styles/github.min.css">
    <style>
        /* 定义CSS变量便于维护 */
        :root {
            --primary-color: #4f46e5;         /* 主色调 */
            --secondary-color: #f8f9fa;        /* 次要背景色 */
            --border-color: #dee2e6;           /* 边框颜色 */
            --hover-color: #e9ecef;             /* 悬停颜色 */
            --compatible-color: #166534;        /* 兼容状态颜色 */
            --incompatible-color: #b91c1c;      /* 不兼容状态颜色 */
            --warning-color: #c2410c;           /* 警告状态颜色 */
        }

        /* 基础样式 */
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: #f5f7fb;          /* 页面背景色 */
            padding-bottom: 3rem;               /* 底部留白 */
        }

        /* 头部样式 */
        .header {
            background: linear-gradient(135deg, #4f46e5, #7c3aed); /* 渐变背景 */
            color: white;                       /* 文字颜色 */
            padding: 2.5rem 0;                 /* 内边距 */
            margin-bottom: 2rem;               /* 底部外边距 */
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); /* 阴影效果 */
        }

        /* 报告卡片样式 */
        .plugin-card {
            background: white;                 /* 卡片背景 */
            border-radius: 8px;                /* 圆角 */
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
            transition: all 0.3s ease;          /* 动画效果 */
            margin-bottom: 1.25rem;             /* 卡片间距 */
            border: 1px solid var(--border-color); /* 边框 */
            overflow: hidden;                  /* 内容溢出隐藏 */
        }

        /* 卡片悬停效果 */
        .plugin-card:hover {
            box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1); /* 悬停时阴影加深 */
            transform: translateY(-3px);       /* 轻微上移 */
        }

        /* 折叠按钮样式 */
        .accordion-button {
            font-weight: 600;                 /* 字体加粗 */
            padding: 1.25rem;                 /* 内边距 */
            background-color: var(--secondary-color); /* 背景色 */
        }

        /* 折叠按钮展开状态 */
        .accordion-button:not(.collapsed) {
            background-color: var(--secondary-color);
            color: var(--primary-color);        /* 文字颜色 */
            box-shadow: none;                  /* 去除阴影 */
        }

        /* 报告内容区域 */
        .report-content {
            padding: 1.5rem;                  /* 内边距 */
            background-color: white;           /* 背景色 */
            border-radius: 0 0 8px 8px;        /* 底部圆角 */
        }

        /* 状态徽章样式 */
        .status-badge {
            font-size: 0.85em;                 /* 字体大小 */
            padding: 0.35em 0.75em;            /* 内边距 */
            border-radius: 20px;               /* 圆形 */
            margin-left: 12px;                 /* 左侧间距 */
        }

        /* 兼容状态样式 */
        .compatible-badge {
            background-color: #dcfce7;         /* 背景色 */
            color: var(--compatible-color);     /* 文字颜色 */
        }

        /* 不兼容状态样式 */
        .incompatible-badge {
            background-color: #fee2e2;         /* 背景色 */
            color: var(--incompatible-color);   /* 文字颜色 */
        }

        /* 警告状态样式 */
        .warning-badge {
            background-color: #ffedd5;          /* 背景色 */
            color: var(--warning-color);        /* 文字颜色 */
        }

        /* 问题数量徽章 */
        .problem-badge {
            background-color: #e0f2fe;         /* 背景色 */
            color: #0c4a6e;                    /* 文字颜色 */
            margin-right: 5px;                 /* 右侧间距 */
            margin-bottom: 5px;                /* 底部间距 */
        }

        /* 搜索容器 */
        .search-container {
            position: relative;                /* 相对定位 */
            margin-bottom: 1.5rem;              /* 底部间距 */
        }

        /* 搜索图标 */
        .search-icon {
            position: absolute;                /* 绝对定位 */
            left: 15px;                        /* 左侧定位 */
            top: 50%;                          /* 垂直居中 */
            transform: translateY(-50%);       /* 垂直偏移 */
            color: #6c757d;                    /* 图标颜色 */
        }

        /* 过滤控制区 */
        .filter-controls {
            display: flex;                     /* 弹性布局 */
            gap: 12px;                         /* 元素间距 */
            margin-bottom: 20px;               /* 底部间距 */
            flex-wrap: wrap;                   /* 允许换行 */
        }

        /* 过滤按钮 */
        .filter-btn {
            border: 1px solid var(--border-color); /* 边框 */
            background: white;                 /* 背景色 */
            padding: 5px 12px;                 /* 内边距 */
            border-radius: 20px;               /* 圆形 */
            font-size: 0.9rem;                 /* 字体大小 */
            transition: all 0.2s;              /* 动画效果 */
        }

        /* 激活状态的过滤按钮 */
        .filter-btn.active {
            background: var(--primary-color);   /* 背景色 */
            color: white;                       /* 文字颜色 */
            border-color: var(--primary-color); /* 边框颜色 */
        }

        /* 统计卡片 */
        .stats-card {
            background: white;                 /* 背景色 */
            border-radius: 8px;                /* 圆角 */
            padding: 1.25rem;                   /* 内边距 */
            box-shadow: 0 2px 4px rgba(0,0,0,0.05); /* 阴影 */
            margin-bottom: 1.5rem;             /* 底部间距 */
            border-left: 4px solid var(--primary-color); /* 左侧边框 */
            margin-top: 2.5rem; /* 增加了上边距 */
        }

        /* 统计数字 */
        .stats-number {
            font-size: 2rem;                   /* 字体大小 */
            font-weight: 700;                  /* 字体加粗 */
            color: var(--primary-color);        /* 文字颜色 */
        }

        /* 文件内容块 */
        .file-content {
            margin-bottom: 1.5rem;             /* 底部间距 */
            border: 1px solid #dee2e6;          /* 边框 */
            border-radius: 5px;                /* 圆角 */
            overflow: hidden;                  /* 溢出隐藏 */
        }

        /* 文件头部 */
        .file-header {
            padding: 0.75rem;                 /* 内边距 */
            background-color: #f8f9fa;         /* 背景色 */
            border-bottom: 1px solid #dee2e6;   /* 底部边框 */
            font-weight: 600;                  /* 字体加粗 */
            display: flex;                     /* 弹性布局 */
            justify-content: space-between;     /* 两端对齐 */
            align-items: center;               /* 垂直居中 */
        }

        /* 文件内容体 */
        .file-body {
            padding: 1rem;                     /* 内边距 */
            background-color: white;           /* 背景色 */
        }

        /* 问题徽章容器 */
        .problem-badges {
            display: flex;                     /* 弹性布局 */
            flex-wrap: wrap;                   /* 允许换行 */
            margin-bottom: 1rem;               /* 底部间距 */
        }

        /* 代码块样式 */
        pre {
            background-color: #f8f9fa;         /* 背景色 */
            border-radius: 6px;                /* 圆角 */
            padding: 1rem;                     /* 内边距 */
            overflow: auto;                   /* 自动滚动条 */
            white-space: pre-wrap;             /* 允许换行 */
        }

        /* 版本计数 */
        .version-count {
            font-size: 1rem;                   /* 字体大小 */
            font-weight: normal;               /* 正常字重 */
            color: #6c757d;                    /* 文字颜色 */
        }

        /* 版本ID */
        .version-id {
            font-size: 0.9em;                  /* 字体大小 */
            color: #6c757d;                    /* 文字颜色 */
            font-weight: normal;               /* 正常字重 */
            margin-left: 8px;                  /* 左侧间距 */
        }

        /* 插件头部 */
        .plugin-header {
            display: flex;                     /* 弹性布局 */
            justify-content: space-between;     /* 两端对齐 */
            align-items: center;               /* 垂直居中 */
            flex-wrap: wrap;                   /* 允许换行 */
        }

        /* 插件信息 */
        .plugin-info {
            background: rgba(255, 255, 255, 0.2); /* 半透明白色背景 */
            padding: 0.5rem 1rem;              /* 内边距 */
            border-radius: 20px;               /* 圆形 */
            font-size: 1.1rem;                 /* 字体大小 */
        }

        /* 文件折叠开关 */
        .file-toggle {
            cursor: pointer;                   /* 手型指针 */
            font-size: 0.9rem;                 /* 字体大小 */
            color: #6c757d;                    /* 文字颜色 */
            display: flex;                     /* 弹性布局 */
            align-items: center;               /* 垂直居中 */
        }

        /* 响应式设计 - 移动端适配 */
        @media (max-width: 768px) {
            /* 头部标题 */
            .header h1 {
                font-size: 1.8rem;             /* 字体大小 */
            }

            /* 统计卡片布局 */
            .stats-container {
                flex-direction: column;         /* 垂直排列 */
            }

            /* 折叠按钮布局 */
            .accordion-button > div {
                flex-direction: column;         /* 垂直排列 */
                align-items: flex-start !important; /* 左对齐 */
            }

            /* 折叠按钮内部元素 */
            .accordion-button > div > div {
                margin-top: 8px;               /* 顶部间距 */
            }

            /* 插件头部布局 */
            .plugin-header {
                flex-direction: column;         /* 垂直排列 */
                align-items: flex-start;        /* 左对齐 */
            }

            /* 插件信息 */
            .plugin-info {
                margin-top: 1rem;              /* 顶部间距 */
                width: 100%;                   /* 全宽 */
            }
        }
        
                    /* 新增时间戳样式 */
            .timestamp {
                font-size: 0.9rem;
                color: rgba(255, 255, 255, 0.85);
                text-align: right;
                margin-top: -15px;
                margin-bottom: 10px;
            }
            
                    /* 新增总问题数徽章样式 */
        .total-problems-badge {
            background-color: #3b82f6; /* 蓝色背景 */
            color: white;              /* 白色文字 */
        }
    </style>

</head>

<body>
    <!-- 报告头部 -->
    <div class="header">
        <div class="container">
            <div class="plugin-header">
                <div>
                    <h1 class="mb-3">插件验证报告</h1>
                    <!-- 添加时间戳 -->
                    <div class="timestamp">报告生成时间: ${formattedTime}</div>
                </div>

                <div class="plugin-info">
                    <div><strong>插件名称:</strong>
 ${mainPluginName}</div>

                    <div><strong>插件版本:</strong>
 ${mainPluginVersion}</div>

                </div>

            </div>


            <!-- 统计信息 -->
            <div class="d-flex align-items-center">
                <div class="stats-card me-4">
                    <div class="text-muted">验证版本</div>

                    <div class="stats-number">${reports.length}</div>

                </div>

                <div class="stats-card">
                    <div class="text-muted">总问题数</div>

                    <div class="stats-number">${reports.reduce((sum, r) => sum + r.totalProblems, 0)}</div>

                </div>

            </div>

        </div>

    </div>


    <!-- 主内容区 -->
    <div class="container">
        <!-- 过滤和搜索控件 -->
        <div class="d-flex justify-content-between align-items-center flex-wrap mb-4">
            <div class="filter-controls">
                <button class="filter-btn active" data-filter="all">全部版本</button>

                <button class="filter-btn" data-filter="compatible">兼容版本</button>

                <button class="filter-btn" data-filter="incompatible">不兼容版本</button>

                <button class="filter-btn" data-filter="problems">存在问题版本</button>

            </div>


            <div class="search-container w-100 w-md-50">
                <i class="bi bi-search search-icon"></i>

                <input type="text" class="form-control ps-5" id="searchInput" placeholder="搜索版本号或内容...">
            </div>

        </div>


        <!-- 报告卡片列表 -->
        <div class="accordion" id="reportsAccordion">
            ${reports.map((report, index) => {
        // 获取验证结果
        const verdict = report.files['verification-verdict.txt']?.content || '未知';
        // 判断兼容状态
        const isCompatible = verdict.includes('Compatible');
        const isIncompatible = verdict.includes('Incompatible') || verdict.includes('Invalid');

        // 计算问题总数并分组
        const problemCounts = Object.entries(report.problemCounts)
            .filter(([_, count]) => count > 0)
            .map(([type, count]) => {
                const fileName = type;
                // 查找对应的描述文本
                const description = reportFileTypes.find(item => item[0] === fileName)?.[1] || fileName;
                return {description, count};
            });

        // 生成报告卡片
        return `
                <div class="plugin-card" data-verdict="${isCompatible ? 'compatible' : isIncompatible ? 'incompatible' : 'unknown'}" data-problems="${report.totalProblems > 0}">
                    <!-- 卡片头部 -->
                    <div class="accordion-header" id="heading${index}">
                        <button class="accordion-button d-flex justify-content-between align-items-center flex-wrap" type="button" data-bs-toggle="collapse" data-bs-target="#collapse${index}" aria-expanded="${index === 0 ? 'true' : 'false'}" aria-controls="collapse${index}">
                            <div class="d-flex flex-column">
                                <div>
                                    <span>${report.productVersion}</span>
                                    <!-- 状态徽章 -->
                                    <span class="badge ${isCompatible ? 'compatible-badge' : isIncompatible ? 'incompatible-badge' : 'warning-badge'} status-badge">
                                        ${isCompatible ? '兼容' : isIncompatible ? '不兼容' : '警告'}
                                    </span>
                                </div>
                                <small class="version-id">插件: ${report.pluginName}:${report.pluginVersion}</small>
                            </div>
                            <div class="d-flex flex-column mt-2 mt-md-0">
                                <div class="problem-badges">
                                    <!-- 各类问题数量徽章 -->
                                    ${problemCounts.map(p => `
                                        <span class="badge problem-badge">
                                            ${p.description}: ${p.count}
                                        </span>

                                    `).join('')}
                                </div>
                                <div>
                                    <span class="badge status-badge total-problems-badge">
                                        <i class="bi bi-exclamation-circle me-1"></i>总问题数: ${report.totalProblems}
                                    </span>
                                </div>
                            </div>
                        </button>
                    </div>

                    <!-- 卡片内容（可折叠） -->
                    <div id="collapse${index}" class="accordion-collapse collapse ${index === 0 ? 'show' : ''}" aria-labelledby="heading${index}" data-bs-parent="#reportsAccordion">
                        <div class="report-content">
                            <!-- 验证结果标题 -->
                            <h4 class="mb-4">验证结果: ${verdict}</h4>

                            <!-- 各类报告文件内容 -->
                            ${reportFileTypes.map(([fileName, description]) => {
            // 跳过不存在的文件和验证结果
            if (!report.files[fileName]) return '';
            if (fileName === 'verification-verdict.txt') return '';

            const fileData = report.files[fileName];
            // 获取问题数量
            const problemCount = report.problemCounts[fileName] || 0;
            // 生成唯一ID用于折叠控制
            const uniqueId = `file-${index}-${fileName.replace('.', '-')}`;

            // 确定默认折叠状态
            const isDependency = fileName === 'dependencies.txt';
            const isDeprecated = fileName === 'deprecated-usages.txt';
            const isExperimental = fileName === 'experimental-api-usages.txt';
            const shouldCollapse = isDependency || isDeprecated || isExperimental;

            return `
                                <div class="file-content">
                                    <div class="file-header">
                                        <div>${description}</div>

                                        <div class="file-toggle" data-bs-toggle="collapse" data-bs-target="#${uniqueId}" aria-expanded="${!shouldCollapse}">
                                            <!-- 问题数量徽章 -->
                                            <span>${problemCount > 0 ? `<span class="badge bg-primary">${problemCount} 个问题</span>` : ''}</span>

                                            <!-- 折叠图标（根据状态显示不同图标） -->
                                            <i class="bi ${shouldCollapse ? 'bi-chevron-right' : 'bi-chevron-down'} ms-2"></i>

                                        </div>

                                    </div>

                                    <!-- 文件内容（根据折叠状态设置） -->
                                    <div class="collapse ${shouldCollapse ? '' : 'show'}" id="${uniqueId}">
                                        <div class="file-body">
                                            <pre><code>${fileData.content}</code>
</pre>

                                        </div>

                                    </div>

                                </div>

                                `;
        }).join('')}
                        </div>
                    </div>
                </div>
                `;
    }).join('')}
        </div>

    </div>


    <!-- 引入JavaScript库 -->
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.10.2/dist/umd/popper.min.js"></script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js"></script>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/highlight.min.js"></script>

    <script>hljs.highlightAll();</script>
 <!-- 初始化代码高亮 -->

    <!-- 交互脚本 -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // 过滤器功能
            const filterButtons = document.querySelectorAll('.filter-btn');
            const reports = document.querySelectorAll('.plugin-card');

            // 为每个过滤按钮添加点击事件
            filterButtons.forEach(btn => {
                btn.addEventListener('click', function() {
                    const filter = this.dataset.filter;

                    // 更新按钮激活状态
                    filterButtons.forEach(b => b.classList.remove('active'));
                    this.classList.add('active');

                    // 根据过滤条件显示/隐藏报告
                    reports.forEach(report => {
                        report.style.display = 'block';

                        if (filter === 'all') return;

                        if (filter === 'compatible' && report.dataset.verdict !== 'compatible') {
                            report.style.display = 'none';
                        }

                        if (filter === 'incompatible' && report.dataset.verdict !== 'incompatible') {
                            report.style.display = 'none';
                        }

                        if (filter === 'problems' && report.dataset.problems === 'false') {
                            report.style.display = 'none';
                        }
                    });
                });
            });

            // 搜索功能
            const searchInput = document.getElementById('searchInput');

            searchInput.addEventListener('input', function() {
                const query = this.value.toLowerCase().trim();

                // 无查询时显示所有报告
                if (!query) {
                    reports.forEach(r => r.style.display = 'block');
                    return;
                }

                // 根据查询内容过滤报告
                reports.forEach(report => {
                    const content = report.textContent.toLowerCase();
                    report.style.display = content.includes(query) ? 'block' : 'none';
                });
            });

            // 文件折叠开关图标更新
            document.querySelectorAll('.file-toggle').forEach(toggle => {
                toggle.addEventListener('click', function() {
                    const icon = this.querySelector('i');
                    // 切换图标状态
                    if (icon.classList.contains('bi-chevron-down')) {
                        icon.classList.replace('bi-chevron-down', 'bi-chevron-right');
                    } else {
                        icon.classList.replace('bi-chevron-right', 'bi-chevron-down');
                    }
                });
            });
        });
    </script>
</body>
</html>
    `;
}

// 主函数
function main() {
    console.log(`📂📂 开始扫描报告目录: ${reportsDir}`);

    // 查找所有报告文件
    findReportFiles(reportsDir);

    if (reportFiles.length === 0) {
        console.error('❌❌ 未找到任何验证报告文件');
        process.exit(1);
    }

    console.log(`✅ 找到 ${reportFiles.length} 个报告文件`);

    // 按产品版本排序（最近的版本在前）
    reportFiles.sort((a, b) => {
        // 尝试从版本字符串中提取数字部分进行排序
        const extractNumbers = (str) => {
            return str.split(/[.-]/).filter(part => /^\d+$/.test(part)).map(Number);
        };

        const aParts = extractNumbers(a.productVersion);
        const bParts = extractNumbers(b.productVersion);

        // 比较每个部分
        for (let i = 0; i < Math.max(aParts.length, bParts.length); i++) {
            const aVal = aParts[i] || 0;
            const bVal = bParts[i] || 0;

            if (bVal !== aVal) {
                return bVal - aVal; // 降序排列
            }
        }

        // 如果数字部分相同，按原始字符串排序
        return b.productVersion.localeCompare(a.productVersion);
    });

    // 生成HTML
    const htmlContent = generateHTML(reportFiles);

    // 保存到文件
    const outputPath = path.resolve(__dirname, '../../aggregated-report.html');
    fs.writeFileSync(outputPath, htmlContent);
    console.log(`✅ 已生成聚合报告: ${outputPath}`);
}

main();