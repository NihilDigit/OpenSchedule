package com.nihildigit.openschedule.model

/**
 * 课程数据模型
 * @param name 课程名称
 * @param day 星期几 (1-7, 7代表星期天)
 * @param room 教室
 * @param teacher 老师
 * @param startNode 开始节次
 * @param endNode 结束节次
 * @param startWeek 开始周
 * @param endWeek 结束周
 * @param type 单双周标记 (0=每周, 1=单周, 2=双周)
 * @param credit 学分
 * @param note 备注
 * @param color 课程颜色
 */
data class Course(
    val name: String,
    val day: Int,
    val room: String = "",
    val teacher: String = "",
    val startNode: Int,
    val endNode: Int,
    val startWeek: Int,
    val endWeek: Int,
    val type: Int = 0,
    val credit: Float = 0f,
    val note: String = "",
    val color: Long = 0xFFE57373
)

/**
 * 获取课程持续的节数
 */
fun Course.getStep(): Int = endNode - startNode + 1

/**
 * 判断课程是否在指定周上课
 */
fun Course.isInWeek(week: Int): Boolean {
    if (week < startWeek || week > endWeek) return false
    return when (type) {
        0 -> true // 每周
        1 -> week % 2 == 1 // 单周
        2 -> week % 2 == 0 // 双周
        else -> true
    }
}
