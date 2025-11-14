package com.nihildigit.openschedule.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihildigit.openschedule.model.Course
import com.nihildigit.openschedule.model.isInWeek

/**
 * 课程表主界面 - Material Design 3 Expressive
 *
 * 性能优化:
 * - 使用 remember 缓存计算结果
 * - 使用 derivedStateOf 优化状态派生
 * - 使用 key 优化列表渲染
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

    // 性能优化: 缓存maxWeek计算
    val maxWeek = remember(courses) {
        courses.maxOfOrNull { it.endWeek } ?: 20
    }

    // 性能优化: 使用derivedStateOf缓存过滤结果
    val filteredCourses by remember(selectedWeek) {
        derivedStateOf {
            courses.filter { it.isInWeek(selectedWeek) }
        }
    }

    Box(
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 周次选择器
            WeekSelector(
                currentWeek = selectedWeek,
                maxWeek = maxWeek,
                onWeekSelected = { selectedWeek = it },
                modifier = Modifier.fillMaxWidth()
            )

            // 课程表
            ScheduleTable(
                courses = filteredCourses,
                maxNode = maxNode,
                onCourseClick = { course ->
                    selectedCourse = course
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // 课程详情对话框 - 使用动画
    AnimatedVisibility(
        visible = selectedCourse != null,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(animationSpec = tween(200)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(200))
    ) {
        selectedCourse?.let { course ->
            CourseDetailDialog(
                course = course,
                currentWeek = selectedWeek,
                onDismiss = { selectedCourse = null }
            )
        }
    }
}

/**
 * 周次选择器 - Material Design 3 Expressive风格
 */
@Composable
fun WeekSelector(
    currentWeek: Int,
    maxWeek: Int,
    onWeekSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 性能优化: 缓存周列表
    val weekList = remember(maxWeek) {
        List(maxWeek) { it + 1 }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "第 $currentWeek 周",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 周选择器滚动列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = weekList,
                    key = { week -> week }
                ) { week ->
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
 * 周次选择芯片 - Expressive设计，带弹性动画
 */
@Composable
fun WeekChip(
    week: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 动画状态
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "contentColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        tonalElevation = elevation,
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .widthIn(min = 42.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$week",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * 课程表网格 - 优化渲染性能
 */
@Composable
fun ScheduleTable(
    courses: List<Course>,
    maxNode: Int,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    // 性能优化: 预先按天分组课程
    val coursesByDay = remember(courses) {
        courses.groupBy { it.day }
    }

    Row(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        // 节次列
        Column(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
        ) {
            // 空白头部（对齐星期行）
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            )

            // 节次数字
            for (node in 1..maxNode) {
                Box(
                    modifier = Modifier
                        .height(88.dp)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$node",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 课程表主体
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = 7,
                key = { dayIndex -> dayIndex }
            ) { dayIndex ->
                DayColumn(
                    dayName = weekDays[dayIndex],
                    dayIndex = dayIndex,
                    courses = coursesByDay[dayIndex + 1] ?: emptyList(),
                    maxNode = maxNode,
                    onCourseClick = onCourseClick
                )
            }
        }
    }
}

/**
 * 单独的一天的列 - 性能优化
 */
@Composable
fun DayColumn(
    dayName: String,
    dayIndex: Int,
    courses: List<Course>,
    maxNode: Int,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(110.dp)
    ) {
        // 星期头部 - Expressive设计
        Surface(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 该天的课程网格
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(2.dp)
        ) {
            // 背景网格
            Column {
                for (node in 1..maxNode) {
                    Box(
                        modifier = Modifier
                            .height(88.dp)
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }

            // 课程卡片 - 使用key优化
            courses.forEach { course ->
                key(course.name, course.startNode, course.teacher) {
                    CourseCard(
                        course = course,
                        onClick = { onCourseClick(course) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .offset(y = ((course.startNode - 1) * 88).dp)
                            .height(((course.endNode - course.startNode + 1) * 88 - 4).dp)
                    )
                }
            }
        }
    }
}

/**
 * 课程卡片 - Material Design 3 Expressive风格
 */
@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "courseCardScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color(course.color),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(course.color),
                            Color(course.color).copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = course.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                if (course.room.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = course.room,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // 重置按压状态
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * 课程详情对话框 - Material Design 3 Expressive风格
 */
@Composable
fun CourseDetailDialog(
    course: Course,
    currentWeek: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(32.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 课程颜色指示器
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(course.color),
                                    Color(course.color).copy(alpha = 0.7f)
                                )
                            )
                        )
                        .shadow(8.dp, CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                CourseDetailItem(
                    icon = Icons.Default.Person,
                    label = "教师",
                    value = course.teacher,
                    iconTint = Color(0xFF6750A4)
                )

                CourseDetailItem(
                    icon = Icons.Default.LocationOn,
                    label = "教室",
                    value = course.room,
                    iconTint = Color(0xFF00897B)
                )

                CourseDetailItem(
                    icon = Icons.Default.Schedule,
                    label = "时间",
                    value = "第${course.startNode}-${course.endNode}节",
                    iconTint = Color(0xFFD84315)
                )

                CourseDetailItem(
                    icon = Icons.Default.CalendarToday,
                    label = "周次",
                    value = "${course.startWeek}-${course.endWeek}周 ${
                        when (course.type) {
                            1 -> "(单周)"
                            2 -> "(双周)"
                            else -> "(每周)"
                        }
                    }",
                    iconTint = Color(0xFF5E35B1)
                )

                if (course.credit > 0) {
                    CourseDetailItem(
                        icon = Icons.Default.CalendarToday,
                        label = "学分",
                        value = course.credit.toString(),
                        iconTint = Color(0xFFEF6C00)
                    )
                }

                if (course.note.isNotEmpty()) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "备注",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = course.note,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "关闭",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * 课程详情项 - 带图标的Expressive设计
 */
@Composable
fun CourseDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (value.isNotEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
