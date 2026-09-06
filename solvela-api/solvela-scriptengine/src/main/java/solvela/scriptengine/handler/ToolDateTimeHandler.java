package solvela.scriptengine.handler;

import org.springframework.stereotype.Component;
import solvela.exception.BusinessException;
import solvela.scriptengine.annotation.ScriptFunction;
import solvela.scriptengine.spi.ScriptDomain;
import solvela.scriptengine.spi.ScriptFunctionHandler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 内置工具：日期时间处理。注册后脚本里以 {@code tool_xxx} 调用。
 *
 * <h3>🔴 时间参数一律声明成 {@code Object}，这不是偷懒</h3>
 * 脚本手里的时间有两种形态：{@code tool_now()} 给的是字符串，而场景变量
 * （{@code eventTime}）绑进来的是 {@code LocalDateTime}。如果函数把参数声明成
 * {@code String}，传 {@code LocalDateTime} 进来会被 QL 的
 * {@code ObjTypeConvertor} 判成「转不了」，然后<b>静默变成 null</b> ——
 * 不报错、不警告，函数收到的就是个 null。
 *
 * <p>所以这里统一收 {@code Object}，由 {@link #toDateTime} 自己认：
 * {@code LocalDateTime} / {@code LocalDate} / {@code Date} / 时间戳数字 /
 * 常见格式的字符串都认，认不出来<b>直接抛</b>并说清收到的是什么。
 *
 * <h3>「可选的时间参数」为什么写成变参</h3>
 * {@code tool_hourOfDay()} 是「现在几点」，{@code tool_hourOfDay(eventTime)} 是
 * 「那个事件发生时几点」—— 同一个问题的两种问法。而脚本函数注册表<b>不支持按参数个数重载</b>
 * （同名直接在启动期抛），所以只能用变参，多传直接报错。
 */
@Component
public class ToolDateTimeHandler implements ScriptFunctionHandler {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 认得出来的字符串格式，按从长到短试
     */
    private static final DateTimeFormatter[] ACCEPTED = {
            DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
    };

    /**
     * 时间戳大于这个数就按毫秒解释，否则按秒。约等于 2286 年的秒级时间戳
     */
    private static final long MILLIS_THRESHOLD = 100_000_000_000L;

    @Override
    public ScriptDomain domain() {
        return ScriptDomain.TOOL;
    }

    // ------------------------------------------------------------------
    // 当前时间
    // ------------------------------------------------------------------

    @ScriptFunction(name = "now", description = "获取系统当前时间，格式 yyyy-MM-dd HH:mm:ss")
    public String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    @ScriptFunction(name = "today", description = "获取系统当前日期，格式 yyyy-MM-dd")
    public String today() {
        return LocalDate.now().format(DATE);
    }

    @ScriptFunction(name = "timestamp", description = "当前时间的 Unix 时间戳（秒）")
    public Long timestamp() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    // ------------------------------------------------------------------
    // 取某个时间的组成部分。不传参数 = 现在
    // ------------------------------------------------------------------

    /**
     * 日期串，常用来拼「当天」的缓存键：
     * <pre>
     *   times = cache_incr('join:' + memberId + ':' + tool_dateOf(), 86400);
     * </pre>
     */
    @ScriptFunction(name = "dateOf",
            description = "取日期部分，格式 yyyy-MM-dd。不传参数就是今天。常用来拼当天的缓存键")
    public String dateOf(Object... time) {
        return optional(time, "tool_dateOf").format(DATE);
    }

    @ScriptFunction(name = "hourOfDay",
            description = "取小时，0~23。不传参数就是现在。用来做时段判定，如 tool_hourOfDay() >= 20")
    public Integer hourOfDay(Object... time) {
        return optional(time, "tool_hourOfDay").getHour();
    }

    @ScriptFunction(name = "dayOfWeek",
            description = "取星期几，1=周一 ... 7=周日。不传参数就是现在")
    public Integer dayOfWeek(Object... time) {
        return optional(time, "tool_dayOfWeek").getDayOfWeek().getValue();
    }

    @ScriptFunction(name = "isWeekend", description = "是不是周六或周日。不传参数就是现在")
    public Boolean isWeekend(Object... time) {
        int day = optional(time, "tool_isWeekend").getDayOfWeek().getValue();
        return day >= 6;
    }

    /**
     * 当前时刻是否落在某个「每天的时段」里。
     *
     * <p>支持跨零点：{@code tool_nowBetween('22:00', '02:00')} 指的是晚上 10 点到次日凌晨 2 点。
     * 不支持的话，运营只能写成两个条件或的形式，而那个写法几乎每次都会漏掉一边的等号。
     */
    @ScriptFunction(name = "nowBetween",
            description = "当前时刻是否在每天的某个时段内，参数格式 HH:mm，如 tool_nowBetween('09:00','12:00')。"
                    + "支持跨零点：('22:00','02:00') 表示晚 10 点到次日凌晨 2 点。含起点、不含终点")
    public Boolean nowBetween(String startHourMinute, String endHourMinute) {
        LocalTime start = parseHourMinute(startHourMinute);
        LocalTime end = parseHourMinute(endHourMinute);
        LocalTime current = LocalTime.now();
        if (start.equals(end)) {
            throw new BusinessException("tool_nowBetween 的起止时刻都是 " + startHourMinute
                    + "，这是个空区间。要表达「全天」就别调这个函数");
        }
        // 跨零点：区间被零点切成两段，落在任一段都算在内
        return start.isBefore(end)
                ? !current.isBefore(start) && current.isBefore(end)
                : !current.isBefore(start) || current.isBefore(end);
    }

    // ------------------------------------------------------------------
    // 两个时间之间
    // ------------------------------------------------------------------

    @ScriptFunction(name = "daysBetween",
            description = "两个时间相差的天数（按日期算，后者减前者）。可以传时间字符串、场景里的时间变量或时间戳")
    public Long daysBetween(Object startTime, Object endTime) {
        return ChronoUnit.DAYS.between(
                required(startTime, "tool_daysBetween").toLocalDate(),
                required(endTime, "tool_daysBetween").toLocalDate());
    }

    @ScriptFunction(name = "secondsBetween",
            description = "两个时间相差的秒数（后者减前者），可以是负数")
    public Long secondsBetween(Object startTime, Object endTime) {
        return ChronoUnit.SECONDS.between(
                required(startTime, "tool_secondsBetween"), required(endTime, "tool_secondsBetween"));
    }

    /**
     * 从那个时间到现在过了几天。会员注册天数这类判据用它。
     */
    @ScriptFunction(name = "daysSince",
            description = "从给定时间到现在过了几天（按日期算）。如 tool_daysSince(注册时间) <= 7 就是新人")
    public Long daysSince(Object time) {
        return ChronoUnit.DAYS.between(required(time, "tool_daysSince").toLocalDate(), LocalDate.now());
    }

    @ScriptFunction(name = "isBefore", description = "前一个时间是否早于后一个")
    public Boolean isBefore(Object time, Object other) {
        return required(time, "tool_isBefore").isBefore(required(other, "tool_isBefore"));
    }

    @ScriptFunction(name = "isAfter", description = "前一个时间是否晚于后一个")
    public Boolean isAfter(Object time, Object other) {
        return required(time, "tool_isAfter").isAfter(required(other, "tool_isAfter"));
    }

    // ------------------------------------------------------------------
    // 变形
    // ------------------------------------------------------------------

    @ScriptFunction(name = "plusDays",
            description = "在给定时间上加减天数（负数就是往前推），返回时间对象，可以再喂给别的时间函数")
    public LocalDateTime plusDays(Object time, long days) {
        return required(time, "tool_plusDays").plusDays(days);
    }

    @ScriptFunction(name = "plusHours", description = "在给定时间上加减小时数（负数就是往前推）")
    public LocalDateTime plusHours(Object time, long hours) {
        return required(time, "tool_plusHours").plusHours(hours);
    }

    @ScriptFunction(name = "format",
            description = "按指定格式输出时间，如 tool_format(eventTime, 'yyyy-MM-dd HH:mm')。"
                    + "格式写错会直接报错，不会给你一串乱码")
    public String format(Object time, String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new BusinessException("tool_format 的格式串是空的。常用的是 'yyyy-MM-dd HH:mm:ss'");
        }
        try {
            return required(time, "tool_format").format(DateTimeFormatter.ofPattern(pattern));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("tool_format 的格式串 [" + pattern + "] 不合法：" + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 参数归一化
    // ------------------------------------------------------------------

    /**
     * 「不传就是现在」的可选参数
     */
    private LocalDateTime optional(Object[] time, String functionName) {
        if (time == null || time.length == 0 || time[0] == null) {
            return LocalDateTime.now();
        }
        if (time.length > 1) {
            throw new BusinessException(functionName + " 最多只收一个时间参数，实际给了 " + time.length + " 个");
        }
        return required(time[0], functionName);
    }

    private LocalDateTime required(Object time, String functionName) {
        LocalDateTime result = toDateTime(time);
        if (result == null) {
            throw new BusinessException(functionName + " 收到的时间是 "
                    + (time == null ? "null" : "[" + time + "]（" + time.getClass().getSimpleName() + "）")
                    + "，认不出来。可以传：时间字符串（yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）、"
                    + "场景里的时间变量、或 Unix 时间戳");
        }
        return result;
    }

    /**
     * 把脚本给的东西认成时间。认不出来返回 null，由调用方决定怎么报错。
     */
    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Number number) {
            long epoch = number.longValue();
            Instant instant = epoch > MILLIS_THRESHOLD
                    ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (value instanceof CharSequence sequence) {
            return parseText(sequence.toString().trim());
        }
        return null;
    }

    private LocalDateTime parseText(String text) {
        if (text.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : ACCEPTED) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // 换下一个格式试
            }
        }
        try {
            // 纯日期：补成当天零点
            return LocalDate.parse(text, DATE).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime parseHourMinute(String text) {
        try {
            return LocalTime.parse(text, HOUR_MINUTE);
        } catch (Exception e) {
            throw new BusinessException("时刻 [" + text + "] 不合法，要写成 HH:mm，如 '09:00'、'22:30'");
        }
    }
}
