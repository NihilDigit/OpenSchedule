package com.nihildigit.openschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.nihildigit.openschedule.importer.WakeUpScheduleParser
import com.nihildigit.openschedule.model.Course
import com.nihildigit.openschedule.ui.schedule.ScheduleScreen
import com.nihildigit.openschedule.ui.theme.OpenScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val courses = loadWakeUpSchedule() ?: getSampleCourses()
        setContent {
            OpenScheduleTheme {
                ScheduleApp(courses)
            }
        }
    }

    private fun loadWakeUpSchedule(): List<Course>? {
        val parser = WakeUpScheduleParser()
        return runCatching {
            assets.open("未命名.wakeup_schedule").bufferedReader().use { it.readText() }
        }.mapCatching { parser.parse(it) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleApp(courses: List<Course>) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OpenSchedule 课程表",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        ScheduleScreen(
            courses = courses,
            currentWeek = 1,
            maxNode = 12,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 获取示例课程数据
 */
fun getSampleCourses(): List<Course> {
    return listOf(
        // 周一
        Course(
            name = "高等数学",
            day = 1,
            room = "A101",
            teacher = "张教授",
            startNode = 1,
            endNode = 2,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 4.0f,
            color = 0xFFE57373
        ),
        Course(
            name = "大学英语",
            day = 1,
            room = "B203",
            teacher = "李老师",
            startNode = 3,
            endNode = 4,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 3.0f,
            color = 0xFF64B5F6
        ),
        Course(
            name = "体育",
            day = 1,
            room = "操场",
            teacher = "王教练",
            startNode = 9,
            endNode = 10,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 1.0f,
            color = 0xFF81C784
        ),

        // 周二
        Course(
            name = "程序设计基础",
            day = 2,
            room = "C301",
            teacher = "刘教授",
            startNode = 1,
            endNode = 3,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 4.0f,
            color = 0xFFFFB74D
        ),
        Course(
            name = "数据结构",
            day = 2,
            room = "C302",
            teacher = "陈老师",
            startNode = 5,
            endNode = 6,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 3.5f,
            color = 0xFFBA68C8
        ),

        // 周三
        Course(
            name = "线性代数",
            day = 3,
            room = "A102",
            teacher = "赵教授",
            startNode = 1,
            endNode = 2,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 3.0f,
            color = 0xFF4DB6AC
        ),
        Course(
            name = "计算机网络",
            day = 3,
            room = "D201",
            teacher = "孙老师",
            startNode = 3,
            endNode = 4,
            startWeek = 1,
            endWeek = 10,
            type = 1, // 单周
            credit = 3.0f,
            color = 0xFFF06292
        ),

        // 周四
        Course(
            name = "操作系统",
            day = 4,
            room = "C303",
            teacher = "周教授",
            startNode = 1,
            endNode = 2,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 4.0f,
            color = 0xFF9575CD
        ),
        Course(
            name = "数据库原理",
            day = 4,
            room = "C304",
            teacher = "吴老师",
            startNode = 5,
            endNode = 7,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 3.5f,
            color = 0xFF4FC3F7
        ),

        // 周五
        Course(
            name = "软件工程",
            day = 5,
            room = "D301",
            teacher = "郑教授",
            startNode = 1,
            endNode = 2,
            startWeek = 1,
            endWeek = 16,
            type = 0,
            credit = 3.0f,
            color = 0xFFAED581
        ),
        Course(
            name = "算法设计",
            day = 5,
            room = "C305",
            teacher = "钱老师",
            startNode = 3,
            endNode = 4,
            startWeek = 2,
            endWeek = 16,
            type = 2, // 双周
            credit = 3.0f,
            color = 0xFFFFD54F
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ScheduleAppPreview() {
    OpenScheduleTheme {
        ScheduleApp(getSampleCourses())
    }
}
