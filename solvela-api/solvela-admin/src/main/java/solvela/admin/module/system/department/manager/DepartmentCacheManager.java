package solvela.admin.module.system.department.manager;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import solvela.admin.constant.AdminCacheConst;
import solvela.admin.module.system.department.dao.DepartmentDao;
import solvela.admin.module.system.department.domain.vo.DepartmentTreeVO;
import solvela.admin.module.system.department.domain.vo.DepartmentVO;
import solvela.base.util.SolvelaBeanUtil;
import solvela.base.util.SolvelaCollectionUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * 部门 缓存相关
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2022-01-12 20:37:48
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Service
public class DepartmentCacheManager {

    @Resource
    private DepartmentDao departmentDao;

    private void logClearInfo(String cache) {
        log.info("clear " + cache);
    }

    @CacheEvict(value = {AdminCacheConst.Department.DEPARTMENT_LIST_CACHE, AdminCacheConst.Department.DEPARTMENT_SELF_CHILDREN_CACHE, AdminCacheConst.Department.DEPARTMENT_TREE_CACHE, AdminCacheConst.Department.DEPARTMENT_PATH_CACHE,}, allEntries = true)
    public void clearCache() {
        logClearInfo(AdminCacheConst.Department.DEPARTMENT_LIST_CACHE);
    }


    /**
     * 部门列表
     */
    @Cacheable(AdminCacheConst.Department.DEPARTMENT_LIST_CACHE)
    public List<DepartmentVO> getDepartmentList() {
        return departmentDao.listAll();
    }

    /**
     * 缓存部门树结构
     */
    @Cacheable(AdminCacheConst.Department.DEPARTMENT_TREE_CACHE)
    public List<DepartmentTreeVO> getDepartmentTree() {
        List<DepartmentVO> departmentVOList = departmentDao.listAll();
        return this.buildTree(departmentVOList);
    }

    /**
     * 缓存某个部门的下级id列表
     */
    @Cacheable(AdminCacheConst.Department.DEPARTMENT_SELF_CHILDREN_CACHE)
    public List<Long> getDepartmentSelfAndChildren(Long departmentId) {
        List<DepartmentVO> departmentVOList = departmentDao.listAll();
        return this.selfAndChildrenIdList(departmentId, departmentVOList);
    }


    /**
     * 部门的路径名称
     */
    @Cacheable(AdminCacheConst.Department.DEPARTMENT_PATH_CACHE)
    public Map<Long, String> getDepartmentPathMap() {
        List<DepartmentVO> departmentVOList = departmentDao.listAll();
        Map<Long, DepartmentVO> departmentMap = departmentVOList.stream().collect(Collectors.toMap(DepartmentVO::getDepartmentId, Function.identity()));

        Map<Long, String> pathNameMap = new HashMap<>();
        for (DepartmentVO departmentVO : departmentVOList) {
            String pathName = this.buildDepartmentPath(departmentVO, departmentMap);
            pathNameMap.put(departmentVO.getDepartmentId(), pathName);
        }

        return pathNameMap;
    }

    /**
     * 构建父级考点路径
     */
    private String buildDepartmentPath(DepartmentVO departmentVO, Map<Long, DepartmentVO> departmentMap) {
        if (Objects.equals(departmentVO.getParentId(), NumberUtils.LONG_ZERO)) {
            return departmentVO.getDepartmentName();
        }
        //父节点
        DepartmentVO parentDepartment = departmentMap.get(departmentVO.getParentId());
        if (parentDepartment == null) {
            return departmentVO.getDepartmentName();
        }
        String pathName = buildDepartmentPath(parentDepartment, departmentMap);
        return pathName + "/" + departmentVO.getDepartmentName();

    }
    // ---------------------- 构造树的一些方法 ------------------------------

    /**
     * 构建部门树结构
     */
    public List<DepartmentTreeVO> buildTree(List<DepartmentVO> voList) {
        if (SolvelaCollectionUtil.isEmpty(voList)) {
            return new ArrayList<>();
        }
        List<DepartmentVO> rootList = voList.stream().filter(e -> e.getParentId() == null || Objects.equals(e.getParentId(), NumberUtils.LONG_ZERO)).collect(Collectors.toList());
        if (SolvelaCollectionUtil.isEmpty(rootList)) {
            return new ArrayList<>();
        }
        List<DepartmentTreeVO> treeVOList = SolvelaBeanUtil.copyList(rootList, DepartmentTreeVO.class);
        this.recursiveBuildTree(treeVOList, voList);
        return treeVOList;
    }

    /**
     * 构建所有根节点的下级树形结构
     * 返回值为层序遍历结果
     * [由于departmentDao中listAll给出数据根据Sort降序 所以同一层中Sort值较大的优先遍历]
     */
    private List<Long> recursiveBuildTree(List<DepartmentTreeVO> nodeList, List<DepartmentVO> allDepartmentList) {
        List<Long> descendantIds = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            linkSiblings(nodeList, i);
            descendantIds.addAll(buildSubtree(nodeList.get(i), allDepartmentList));
        }

        // 本层的 id 整体插到最前面，从而得到「先本层、后下层」的层序结果
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            descendantIds.add(0, nodeList.get(i).getDepartmentId());
        }
        return descendantIds;
    }

    /**
     * 给节点挂上同层的前驱/后继 id。
     *
     * <p>前端的「上移/下移」直接用这两个字段，不必自己在数组里找位置 ——
     * 它拿到的是一棵树而不是一个数组，找位置要先递归定位到本层。
     */
    private static void linkSiblings(List<DepartmentTreeVO> nodeList, int index) {
        DepartmentTreeVO node = nodeList.get(index);
        if (index - 1 > -1) {
            node.setPreId(nodeList.get(index - 1).getDepartmentId());
        }
        if (index + 1 < nodeList.size()) {
            node.setNextId(nodeList.get(index + 1).getDepartmentId());
        }
    }

    /**
     * 递归展开一个节点的子树，并在它身上填好 {@code selfAndAllChildrenIdList}。
     *
     * <p>那个字段是数据权限的核心：「本部门及下级」这条范围最终就是拿它去 IN 查询的，
     * 少一个 id 就是少看见一整个分支的数据，而页面上不会有任何异常。
     *
     * @return 这个节点的<b>全部后代</b> id（不含它自己）
     */
    private List<Long> buildSubtree(DepartmentTreeVO node, List<DepartmentVO> allDepartmentList) {
        List<DepartmentTreeVO> children = getChildren(node.getDepartmentId(), allDepartmentList);
        List<Long> descendantIds = new ArrayList<>();
        if (SolvelaCollectionUtil.isNotEmpty(children)) {
            node.setChildren(children);
            descendantIds = this.recursiveBuildTree(children, allDepartmentList);
        }

        if (SolvelaCollectionUtil.isEmpty(node.getSelfAndAllChildrenIdList())) {
            node.setSelfAndAllChildrenIdList(new ArrayList<>());
        }
        node.getSelfAndAllChildrenIdList().add(node.getDepartmentId());
        node.getSelfAndAllChildrenIdList().addAll(descendantIds);
        return descendantIds;
    }


    /**
     * 获取子元素
     */
    private List<DepartmentTreeVO> getChildren(Long departmentId, List<DepartmentVO> voList) {
        List<DepartmentVO> childrenEntityList = voList.stream().filter(e -> departmentId.equals(e.getParentId())).collect(Collectors.toList());
        if (SolvelaCollectionUtil.isEmpty(childrenEntityList)) {
            return new ArrayList<>();
        }
        return SolvelaBeanUtil.copyList(childrenEntityList, DepartmentTreeVO.class);
    }


    /**
     * 通过部门id,获取当前以及下属部门
     */
    public List<Long> selfAndChildrenIdList(Long departmentId, List<DepartmentVO> voList) {
        List<Long> selfAndChildrenIdList = new ArrayList<>();
        if (SolvelaCollectionUtil.isEmpty(voList)) {
            return selfAndChildrenIdList;
        }
        selfAndChildrenIdList.add(departmentId);
        List<DepartmentTreeVO> children = this.getChildren(departmentId, voList);
        if (SolvelaCollectionUtil.isEmpty(children)) {
            return selfAndChildrenIdList;
        }
        List<Long> childrenIdList = children.stream().map(DepartmentTreeVO::getDepartmentId).collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
        return selfAndChildrenIdList;
    }

    /**
     * 递归查询
     */
    public void selfAndChildrenRecursion(List<Long> selfAndChildrenIdList, Long departmentId, List<DepartmentVO> voList) {
        List<DepartmentTreeVO> children = this.getChildren(departmentId, voList);
        if (SolvelaCollectionUtil.isEmpty(children)) {
            return;
        }
        List<Long> childrenIdList = children.stream().map(DepartmentTreeVO::getDepartmentId).collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
    }
}
