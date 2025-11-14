package com.nihildigit.openschedule

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihildigit.openschedule.importer.WakeUpScheduleParser
import com.nihildigit.openschedule.model.Course
import com.nihildigit.openschedule.ui.schedule.ScheduleScreen
import com.nihildigit.openschedule.ui.theme.OpenScheduleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val wakeUpCourses = loadWakeUpSchedule()
        setContent {
            OpenScheduleTheme {
                ScheduleApp(initialCourses = wakeUpCourses)
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
fun ScheduleApp(initialCourses: List<Course>?) {
    val context = LocalContext.current
    val parser = remember { WakeUpScheduleParser() }
    var courses by remember { mutableStateOf(initialCourses) }
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val parsed = readCoursesFromUri(context, parser, uri)
            withContext(Dispatchers.Main) {
                if (parsed.isNullOrEmpty()) {
                    Toast.makeText(context, "解析失败，请确认文件格式", Toast.LENGTH_SHORT).show()
                } else {
                    courses = parsed
                    takePersistablePermission(context, uri)
                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openPicker: () -> Unit = {
        filePickerLauncher.launch(
            arrayOf(
                "text/calendar",
                "application/octet-stream",
                "*/*"
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OpenSchedule",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (!courses.isNullOrEmpty()) {
                        FilledTonalButton(
                            onClick = openPicker,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "导入",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentCourses = courses
        if (currentCourses.isNullOrEmpty()) {
            WakeUpImportPlaceholder(
                modifier = Modifier.padding(innerPadding),
                onImportClick = openPicker
            )
        } else {
            ScheduleScreen(
                courses = currentCourses,
                currentWeek = 1,
                maxNode = 12,
                modifier = Modifier.padding(innerPadding)
            )
        }
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
        ScheduleApp(initialCourses = getSampleCourses())
    }
}

/**
 * WakeUp导入占位符 - Material Design 3 Expressive风格
 */
@Composable
private fun WakeUpImportPlaceholder(
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FF),
                        Color(0xFFFEFBFF)
                    )
                )
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "欢迎使用 OpenSchedule",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "导入您的 WakeUp 课表文件\n即可开始使用",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "支持 .wakeup_schedule 和 .ics 格式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 导入按钮 - Expressive风格
        Button(
            onClick = onImportClick,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "选择文件",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private suspend fun readCoursesFromUri(
    context: Context,
    parser: WakeUpScheduleParser,
    uri: Uri
): List<Course>? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }.getOrNull()?.let { parser.parse(it) }
}

private fun takePersistablePermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}
