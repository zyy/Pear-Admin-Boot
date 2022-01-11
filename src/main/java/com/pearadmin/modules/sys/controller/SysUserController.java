package com.pearadmin.modules.sys.controller;

import com.github.pagehelper.PageInfo;
import com.pearadmin.common.constant.ControllerConstant;
import com.pearadmin.common.aop.annotation.Log;
import com.pearadmin.common.aop.enums.BusinessType;
import com.pearadmin.common.aop.annotation.Repeat;
import com.pearadmin.common.context.UserContext;
import com.pearadmin.common.tools.SequenceUtil;
import com.pearadmin.common.web.base.BaseController;
import com.pearadmin.common.web.domain.request.PageDomain;
import com.pearadmin.common.web.domain.response.Result;
import com.pearadmin.common.web.domain.response.module.ResultTable;
import com.pearadmin.modules.sys.domain.SysUser;
import com.pearadmin.modules.sys.domain.SysPassword;
import com.pearadmin.modules.sys.service.ISysRoleService;
import com.pearadmin.modules.sys.service.ISysUserService;
import com.pearadmin.modules.sys.service.ISysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Describe: 用户控制器
 * Author: 就 眠 仪 式
 * CreateTime: 2019/10/23
 */
@RestController
@Api(tags = {"用户管理"})
@RequestMapping(ControllerConstant.API_SYSTEM_PREFIX + "user")
public class SysUserController extends BaseController {

    /**
     * Describe: 基础路径
     */
    private static String MODULE_PATH = "system/user/";

    /**
     * Describe: 用户模块服务
     */
    @Resource
    private ISysUserService sysUserService;

    /**
     * Describe: 角色模块服务
     */
    @Resource
    private ISysRoleService sysRoleService;

    /**
     * Describe: 日志模块服务
     */
    @Resource
    private ISysLogService sysLogService;

    /**
     * Describe: 获取用户列表视图
     * Param ModelAndView
     * Return 用户列表视图
     */
    @GetMapping("main")
    @ApiOperation(value = "获取用户列表视图")
    @PreAuthorize("hasPermission('/system/user/main','sys:user:main')")
    public ModelAndView main() {
        return jumpPage(MODULE_PATH + "main");
    }

    /**
     * Describe: 获取用户列表数据
     * Param ModelAndView
     * Return 用户列表数据
     */
    @GetMapping("data")
    @ApiOperation(value = "获取用户列表数据")
    @PreAuthorize("hasPermission('/system/user/data','sys:user:data')")
    @Log(title = "查询用户", describe = "查询用户", type = BusinessType.QUERY)
    public ResultTable data(PageDomain pageDomain, SysUser param) {
        PageInfo<SysUser> pageInfo = sysUserService.page(param, pageDomain);
        return pageTable(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * Describe: 用户新增视图
     * Param ModelAndView
     * Return 返回用户新增视图
     */
    @GetMapping("add")
    @ApiOperation(value = "获取用户新增视图")
    @PreAuthorize("hasPermission('/system/user/add','sys:user:add')")
    public ModelAndView add(Model model) {
        model.addAttribute("sysRoles", sysRoleService.list(null));
        return jumpPage(MODULE_PATH + "add");
    }

    /**
     * Describe: 用户新增接口
     * Param ModelAndView
     * Return 操作结果
     */
    @Repeat
    @PostMapping("save")
    @ApiOperation(value = "保存用户数据")
    @PreAuthorize("hasPermission('/system/user/add','sys:user:add')")
    @Log(title = "新增用户", describe = "新增用户", type = BusinessType.ADD)
    public Result save(@RequestBody SysUser sysUser) {
        sysUser.setLogin("0");
        sysUser.setEnable("1");
        sysUser.setStatus("1");
        sysUser.setUserId(SequenceUtil.makeStringId());
        sysUser.setPassword(new BCryptPasswordEncoder().encode(sysUser.getPassword()));
        sysUserService.saveUserRole(sysUser.getUserId(), Arrays.asList(sysUser.getRoleIds().split(",")));
        Boolean result = sysUserService.save(sysUser);
        return decide(result);
    }

    /**
     * Describe: 用户修改视图
     * Param ModelAndView
     * Return 返回用户修改视图
     */
    @GetMapping("edit")
    @ApiOperation(value = "获取用户修改视图")
    @PreAuthorize("hasPermission('/system/user/edit','sys:user:edit')")
    public ModelAndView edit(Model model, String userId) {
        model.addAttribute("sysRoles", sysUserService.getUserRole(userId));
        model.addAttribute("sysUser", sysUserService.getById(userId));
        return jumpPage(MODULE_PATH + "edit");
    }

    /**
     * Describe: 用户密码修改视图
     * Param ModelAndView
     * Return 返回用户密码修改视图
     */
    @GetMapping("editpasswordadmin")
    @ApiOperation(value = "获取管理员修改用户密码视图")
    @PreAuthorize("hasPermission('/system/user/editPasswordAdmin','sys:user:editPasswordAdmin')")
    public ModelAndView editPasswordAdminView(Model model, String userId) {
        model.addAttribute("userId", userId);
        return jumpPage(MODULE_PATH + "editPasswordAdmin");
    }

    /**
     * Describe: 管理员修改用户密码接口
     * Param editPassword
     * Return: Result
     */
    @PutMapping("editPasswordAdmin")
    @ApiOperation(value = "管理员修改用户密码")
    @PreAuthorize("hasPermission('/system/user/editPasswordAdmin','sys:user:editPasswordAdmin')")
    public Result editPasswordAdmin(@RequestBody SysPassword sysPassword) {
        SysUser editUser = sysUserService.getById(sysPassword.getUserId());
        editUser.setPassword(new BCryptPasswordEncoder().encode(sysPassword.getNewPassword()));
        boolean result = sysUserService.update(editUser);
        return decide(result, "修改成功", "修改失败");
    }

    /**
     * Describe: 用户密码修改视图
     * Param ModelAndView
     * Return 返回用户密码修改视图
     */
    @GetMapping("editPassword")
    public ModelAndView editPasswordView() {
        return jumpPage(MODULE_PATH + "password");
    }

    /**
     * Describe: 用户密码修改接口
     * Param editPassword
     * Return: Result
     */
    @PutMapping("editPassword")
    public Result editPassword(@RequestBody SysPassword sysPassword) {
        String oldPassword = sysPassword.getOldPassword();
        String newPassword = sysPassword.getNewPassword();
        String confirmPassword = sysPassword.getConfirmPassword();
        SysUser sysUser = UserContext.currentUser();
        SysUser editUser = sysUserService.getById(sysUser.getUserId());
        if (Strings.isBlank(confirmPassword)
                || Strings.isBlank(newPassword)
                || Strings.isBlank(oldPassword)) {
            return failure("输入不能为空");
        }
        if (!new BCryptPasswordEncoder().matches(oldPassword, editUser.getPassword())) {
            return failure("密码验证失败");
        }
        if (!newPassword.equals(confirmPassword)) {
            return failure("两次密码输入不一致");
        }
        editUser.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        boolean result = sysUserService.update(editUser);
        return decide(result, "修改成功", "修改失败");
    }

    /**
     * Describe: 用户修改接口
     * Param ModelAndView
     * Return 返回用户修改接口
     */
    @PutMapping("update")
    @ApiOperation(value = "修改用户数据")
    @PreAuthorize("hasPermission('/system/user/edit','sys:user:edit')")
    @Log(title = "修改用户", describe = "修改用户", type = BusinessType.EDIT)
    public Result update(@RequestBody SysUser sysUser) {
        sysUserService.saveUserRole(sysUser.getUserId(), Arrays.asList(sysUser.getRoleIds().split(",")));
        boolean result = sysUserService.update(sysUser);
        return decide(result);
    }

    /**
     * Describe: 头像修改接口
     * Param: SysUser
     * Return: Result
     */
    @PutMapping("updateAvatar")
    @ApiOperation(value = "修改用户头像")
    @Log(title = "修改头像", describe = "修改头像", type = BusinessType.EDIT)
    public Result updateAvatar(@RequestBody SysUser sysUser) {
        String userId = UserContext.currentUser().getUserId();
        sysUser.setUserId(userId);
        boolean result = sysUserService.update(sysUser);
        return decide(result);
    }

    /**
     * Describe: 用户批量删除接口
     * Param: ids
     * Return: Result
     */
    @DeleteMapping("batchRemove/{ids}")
    @ApiOperation(value = "批量删除用户")
    @PreAuthorize("hasPermission('/system/user/remove','sys:user:remove')")
    @Log(title = "删除用户", describe = "删除用户", type = BusinessType.REMOVE)
    public Result batchRemove(@PathVariable String ids) {
        boolean result = sysUserService.batchRemove(ids.split(","));
        return decide(result);
    }

    /**
     * Describe: 用户删除接口
     * Param: id
     * Return: Result
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("remove/{id}")
    @ApiOperation(value = "删除用户数据")
    @PreAuthorize("hasPermission('/system/user/remove','sys:user:remove')")
    @Log(title = "删除用户", describe = "删除用户", type = BusinessType.REMOVE)
    public Result remove(@PathVariable String id) {
        boolean result = sysUserService.remove(id);
        return decide(result);
    }



    /**
     * Describe: 根据 userId 开启用户
     * Param: SysUser
     * Return: 执行结果
     */
    @PutMapping("enable")
    @ApiOperation(value = "开启用户登录")
    public Result enable(@RequestBody SysUser sysUser) {
        sysUser.setEnable("1");
        boolean result = sysUserService.update(sysUser);
        return decide(result);
    }

    /**
     * Describe: 根据 userId 禁用用户
     * Param: SysUser
     * Return: 执行结果
     */
    @PutMapping("disable")
    @ApiOperation(value = "禁用用户登录")
    public Result disable(@RequestBody SysUser sysUser) {
        sysUser.setEnable("0");
        boolean result = sysUserService.update(sysUser);
        return decide(result);
    }

    /**
     * Describe: 个人资料
     * Param: null
     * Return: ModelAndView
     */
    @GetMapping("center")
    @ApiOperation(value = "个人资料")
    public ModelAndView center(Model model) {
        SysUser sysUser = UserContext.currentUser();
        model.addAttribute("userInfo", sysUserService.getById(sysUser.getUserId()));
        model.addAttribute("logs", sysLogService.selectTopLoginLog(sysUser.getUsername()));
        return jumpPage(MODULE_PATH + "center");
    }

    /**
     * Describe: 用户修改接口
     * Param ModelAndView
     * Return 返回用户修改接口
     */
    @PutMapping("updateInfo")
    @ApiOperation(value = "修改用户数据")
    public Result updateInfo(@RequestBody SysUser sysUser) {
        boolean result = sysUserService.update(sysUser);
        return decide(result);
    }


    /**
     * Describe: 更换头像
     * Param: null
     * Return: ModelAndView
     */
    @GetMapping("profile/{id}")
    public ModelAndView profile(Model model, @PathVariable("id") String userId) {
        model.addAttribute("userId", userId);
        return jumpPage(MODULE_PATH + "profile");
    }
}