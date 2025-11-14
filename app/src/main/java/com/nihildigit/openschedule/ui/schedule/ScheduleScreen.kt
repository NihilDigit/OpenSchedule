package com.nihildigit.openschedule.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihildigit.openschedule.model.Course
import com.nihildigit.openschedule.model.isInWeek

/**
 * 课程表主界面
 */
@Composable
fun ScheduleScreen(
    courses: List<Course>,
    currentWeek: Int = 1,
    maxNode: Int = 12,
    modifier: Modifier = Modifier
) {
    var selectedWeek by remember { mutableStateOf(currentWeek) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 周次选择器
        WeekSelector(
            currentWeek = selectedWeek,
            maxWeek = courses.maxOfOrNull { it.endWeek } ?: 20,
            onWeekSelected = { selectedWeek = it }
        )

        // 课程表
        ScheduleTable(
            courses = courses.filter { it.isInWeek(selectedWeek) },
            maxNode = maxNode,
            onCourseClick = { course ->
                selectedCourse = course
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // 课程详情对话框
    selectedCourse?.let { course ->
        CourseDetailDialog(
            course = course,
            currentWeek = selectedWeek,
            onDismiss = { selectedCourse = null }
        )
    }
}

/**
 * 周次选择器
 */
@Composable
fun WeekSelector(
    currentWeek: Int,
    maxWeek: Int,
    onWeekSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "第 $currentWeek 周",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(List(maxWeek) { it + 1 }) { _, week ->
                    WeekChip(
                        week = week,
                        isSelected = week == currentWeek,
                        onClick = { onWeekSelected(week) }
                    )
                }
            }
        }
    }
}

/**
 * 周次选择芯片
 */
@Composable
fun WeekChip(
    week: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color(0xFFE0E0E0)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$week",
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 课程表网格
 */
@Composable
fun ScheduleTable(
    courses: List<Course>,
    maxNode: Int,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Row(modifier = modifier.padding(8.dp)) {
        // 节次列
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
        ) {
            // 空白头部（对齐星期行）
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            )

            // 节次数字
            for (node in 1..maxNode) {
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .border(0.5.dp, Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$node",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 课程表主体
        LazyRow(
            modifier = Modifier.fillMaxSize()
        ) {
            items(7) { dayIndex ->
                Column(
                    modifier = Modifier.width(100.dp)
                ) {
                    // 星期头部
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(0.5.dp, Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = weekDays[dayIndex],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // 该天的课程网格
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // 背景网格
                        Column {
                            for (node in 1..maxNode) {
                                Box(
                                    modifier = Modifier
                                        .height(80.dp)
                                        .fillMaxWidth()
                                        .border(0.5.dp, Color(0xFFE0E0E0))
                                )
                            }
                        }

                        // 课程卡片
                        val dayCourses = courses.filter { it.day == dayIndex + 1 }
                        dayCourses.forEach { course ->
                            CourseCard(
                                course = course,
                                onClick = { onCourseClick(course) },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .offset(y = ((course.startNode - 1) * 80).dp)
                                    .height((course.endNode - course.startNode + 1) * 80.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 课程卡片
 */
@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(course.color)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = course.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (course.room.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.room,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 课程详情对话框
 */
@Composable
fun CourseDetailDialog(
    course: Course,
    currentWeek: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = course.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CourseDetailItem("教师", course.teacher)
                CourseDetailItem("教室", course.room)
                CourseDetailItem("时间", "第${course.startNode}-${course.endNode}节")
                CourseDetailItem(
                    "周次",
                    "${course.startWeek}-${course.endWeek}周 ${
                        when (course.type) {
                            1 -> "(单周)"
                            2 -> "(双周)"
                            else -> "(每周)"
                        }
                    }"
                )
                if (course.credit > 0) {
                    CourseDetailItem("学分", course.credit.toString())
                }
                if (course.note.isNotEmpty()) {
                    CourseDetailItem("备注", course.note)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        containerColor = Color.White
    )
}

/**
 * 课程详情项
 */
@Composable
fun CourseDetailItem(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}
