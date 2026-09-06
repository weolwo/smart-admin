package solvela.admin.module.system.datascope.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import solvela.admin.AdminApplication;
import solvela.admin.module.system.datascope.DataScope;
import solvela.admin.module.system.datascope.constant.DataScopeTypeEnum;
import solvela.admin.module.system.datascope.constant.DataScopeViewTypeEnum;
import solvela.admin.module.system.datascope.constant.DataScopeWhereInTypeEnum;
import solvela.admin.module.system.datascope.domain.DataScopeSqlConfig;
import solvela.admin.module.system.datascope.strategy.AbstractDataScopeStrategy;
import solvela.base.util.SolvelaCollectionUtil;
import solvela.admin.auth.CurrentEmployee;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sql配置
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2020/11/28  20:59:17
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Service
public class DataScopeSqlConfigService {

    /**
     * 注解joinsql 参数
     */
    private static final String EMPLOYEE_PARAM = "#employeeIds";

    private static final String DEPARTMENT_PARAM = "#departmentIds";

    /**
     * 用于拼接查看本人数据范围的 SQL
     */
    private static final String CREATE_USER_ID_EQUALS = "create_user_id = ";

    private final ConcurrentHashMap<String, DataScopeSqlConfig> dataScopeMethodMap = new ConcurrentHashMap<>();

    @Resource
    private DataScopeViewService dataScopeViewService;

    @Resource
    private ApplicationContext applicationContext;


    @PostConstruct
    private void initDataScopeMethodMap() {
        this.refreshDataScopeMethodMap();
    }

    /**
     * 刷新 所有添加数据范围注解的接口方法配置<class.method,DataScopeSqlConfigDTO></>
     */
    private Map<String, DataScopeSqlConfig> refreshDataScopeMethodMap() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(AdminApplication.COMPONENT_SCAN)).setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(DataScope.class);
        for (Method method : methods) {
            DataScope dataScopeAnnotation = method.getAnnotation(DataScope.class);
            if (dataScopeAnnotation != null) {
                DataScopeSqlConfig configDTO = new DataScopeSqlConfig();
                configDTO.setDataScopeType(dataScopeAnnotation.dataScopeType());
                configDTO.setJoinSql(dataScopeAnnotation.joinSql());
                configDTO.setWhereIndex(dataScopeAnnotation.whereIndex());
                configDTO.setDataScopeWhereInType(dataScopeAnnotation.whereInType());
                configDTO.setParamName(dataScopeAnnotation.paramName());
                configDTO.setJoinSqlImplClazz(dataScopeAnnotation.joinSqlImplClazz());
                dataScopeMethodMap.put(method.getDeclaringClass().getSimpleName() + "." + method.getName(), configDTO);
            }
        }
        return dataScopeMethodMap;
    }

    /**
     * 根据调用的方法获取，此方法的配置信息
     */
    public DataScopeSqlConfig getSqlConfig(String method) {
        return this.dataScopeMethodMap.get(method);
    }

    /**
     * 组装需要拼接的sql
     */
    public String getJoinSql(Map<String, Object> paramMap, DataScopeSqlConfig sqlConfigDTO) {
        Long employeeId = CurrentEmployee.idOrNull();
        if (employeeId == null) {
            return "";
        }

        DataScopeTypeEnum dataScopeTypeEnum = sqlConfigDTO.getDataScopeType();
        DataScopeViewTypeEnum viewTypeEnum = dataScopeViewService.getEmployeeDataScopeViewType(dataScopeTypeEnum, employeeId);

        // 数据权限设置为仅本人可见时 直接返回 create_user_id = employeeId
        if (DataScopeViewTypeEnum.ME == viewTypeEnum) {
            return CREATE_USER_ID_EQUALS + employeeId;
        }

        /*
         * 🔴 每一条不确定的路径都返回空串，而不是「不加条件」。
         * 空串会让调用方拼出一个查不到任何数据的 SQL —— 数据权限出问题时，
         * <b>看不到本该看到的数据</b>是可以被发现并投诉的，
         * 而「看到了本不该看到的数据」没有人会来报障。
         */
        return switch (sqlConfigDTO.getDataScopeWhereInType()) {
            case CUSTOM_STRATEGY -> customStrategySql(paramMap, sqlConfigDTO, viewTypeEnum);
            case EMPLOYEE -> inSql(sqlConfigDTO.getJoinSql(), EMPLOYEE_PARAM,
                    dataScopeViewService.getCanViewEmployeeId(viewTypeEnum, employeeId));
            case DEPARTMENT -> inSql(sqlConfigDTO.getJoinSql(), DEPARTMENT_PARAM,
                    dataScopeViewService.getCanViewDepartmentId(viewTypeEnum, employeeId));
            case null, default -> "";
        };
    }

    /** 业务自定义的范围策略。Bean 拿不到就返回空串 —— 配错了要表现为「查不到」，不是「不限制」 */
    private String customStrategySql(Map<String, Object> paramMap, DataScopeSqlConfig sqlConfigDTO,
                                     DataScopeViewTypeEnum viewTypeEnum) {
        Class<?> strategyClass = sqlConfigDTO.getJoinSqlImplClazz();
        if (strategyClass == null) {
            log.warn("data scope custom strategy class is null");
            return "";
        }
        AbstractDataScopeStrategy powerStrategy =
                (AbstractDataScopeStrategy) applicationContext.getBean(strategyClass);
        if (powerStrategy == null) {
            log.warn("data scope custom strategy class：{} ,bean is null", strategyClass);
            return "";
        }
        return powerStrategy.getCondition(viewTypeEnum, paramMap, sqlConfigDTO);
    }

    /** 把可见 id 列表填进 joinSql 的占位符。可见范围为空时返回空串，同上 */
    private static String inSql(String joinSql, String paramPlaceholder, List<Long> visibleIds) {
        if (SolvelaCollectionUtil.isEmpty(visibleIds)) {
            return "";
        }
        return joinSql.replaceAll(paramPlaceholder, StringUtils.join(visibleIds, ","));
    }
}
