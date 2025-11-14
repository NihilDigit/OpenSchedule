# OpenSchedule - 课程表应用

OpenSchedule是一个基于Jetpack Compose开发的Android课程表应用，可以展示和管理学生的课程安排。

## 功能特性

- 📅 **周课程表视图** - 清晰展示每周的课程安排
- 🔄 **周次切换** - 支持在不同周次间快速切换
- 📝 **课程详情** - 点击课程卡片查看详细信息
- 🎨 **彩色课程卡** - 不同课程使用不同颜色区分
- ⚡ **单双周支持** - 支持每周、单周、双周课程类型

## 项目结构

```
app/src/main/java/com/nihildigit/openschedule/
├── model/
│   └── Course.kt                    # 课程数据模型
├── ui/
│   ├── schedule/
│   │   └── ScheduleScreen.kt        # 课程表UI组件
│   └── theme/                        # 主题配置
└── MainActivity.kt                   # 主Activity
```

## 数据模型

### Course (课程)

课程数据模型包含以下字段：

- `name`: 课程名称
- `day`: 星期几 (1-7, 7代表星期天)
- `room`: 教室
- `teacher`: 教师
- `startNode`: 开始节次
- `endNode`: 结束节次
- `startWeek`: 开始周
- `endWeek`: 结束周
- `type`: 单双周标记 (0=每周, 1=单周, 2=双周)
- `credit`: 学分
- `note`: 备注
- `color`: 课程颜色

## UI组件

### ScheduleScreen

主课程表界面，包含以下子组件：

1. **WeekSelector** - 周次选择器，支持快速切换不同周次
2. **ScheduleTable** - 课程表网格，显示一周7天的课程安排
3. **CourseCard** - 课程卡片，显示课程基本信息
4. **CourseDetailDialog** - 课程详情对话框，显示完整的课程信息

## 技术栈

- **Kotlin** - 开发语言
- **Jetpack Compose** - 现代化UI框架
- **Material Design 3** - UI设计规范
- **Android SDK 28+** - 最低支持Android 9.0

## 集成OpenCourseAdapter

本项目设计为与 [OpenCourseAdapter](https://github.com/NihilDigit/OpenCourseAdapter) 解析引擎配合使用。

OpenCourseAdapter提供了标准化的课程数据解析接口，支持多种教务系统的课程数据导入。

### 数据结构映射

OpenCourseAdapter的 `Course` 类可以直接映射到本项目的 `Course` 数据模型：

```kotlin
// OpenCourseAdapter的Course数据
val parsedCourses: List<bean.Course> = parser.generateCourseList()

// 转换为应用的Course模型
val appCourses = parsedCourses.map { course ->
    Course(
        name = course.name,
        day = course.day,
        room = course.room,
        teacher = course.teacher,
        startNode = course.startNode,
        endNode = course.endNode,
        startWeek = course.startWeek,
        endWeek = course.endWeek,
        type = course.type,
        credit = course.credit,
        note = course.note,
        color = generateColor() // 自动生成颜色
    )
}
```

## 使用示例

### 1. 创建课程数据

```kotlin
val course = Course(
    name = "高等数学",
    day = 1,                // 周一
    room = "A101",
    teacher = "张教授",
    startNode = 1,          // 第1节
    endNode = 2,            // 第2节
    startWeek = 1,          // 第1周
    endWeek = 16,           // 第16周
    type = 0,               // 每周上课
    credit = 4.0f,
    color = 0xFFE57373
)
```

### 2. 展示课程表

```kotlin
ScheduleScreen(
    courses = courseList,
    currentWeek = 1,        // 当前周次
    maxNode = 12            // 最大节次
)
```

## 导入 WakeUpSchedule 数据

- 首次启动若未检测到 WakeUp 课表，界面会提示“选择文件”；点击即可通过系统文件选择器导入 `.wakeup_schedule` / `.ics` 文件。
- 选择完成后应用会使用 `WakeUpScheduleParser` 解析并即时更新课程表，解析失败会提示重新选择。
- 如果想在开发阶段内置测试数据，可将文件放入 `app/src/main/assets/未命名.wakeup_schedule`，应用会自动读取该资产文件。
- 更复杂的字段映射或节次规则可以直接在 `WakeUpScheduleParser` 中调整。

## 开发计划

- [ ] 支持从文件导入课程数据
- [ ] 集成OpenCourseAdapter解析引擎
- [ ] 支持自定义时间表
- [ ] 添加课程搜索功能
- [ ] 支持课程表导出和分享
- [ ] 添加课程提醒功能
- [ ] 支持多课程表管理

## 构建项目

1. 克隆项目
```bash
git clone https://github.com/NihilDigit/OpenSchedule.git
cd OpenSchedule
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖

4. 运行项目

## 许可证

本项目采用MIT许可证。

## 贡献

欢迎提交Issue和Pull Request！

## 相关项目

- [OpenCourseAdapter](https://github.com/NihilDigit/OpenCourseAdapter) - 课程表解析引擎
