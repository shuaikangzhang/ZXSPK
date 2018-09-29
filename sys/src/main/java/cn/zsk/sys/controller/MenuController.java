package cn.zsk.sys.controller;

import cn.zsk.core.base.BaseController;
import cn.zsk.core.exception.MyException;
import cn.zsk.core.util.BeanUtil;
import cn.zsk.core.util.JsonUtil;
import cn.zsk.sys.core.annotation.Log;
import cn.zsk.sys.core.annotation.Log.LOG_TYPE;
import cn.zsk.sys.core.shiro.ShiroUtil;
import cn.zsk.sys.entity.SysMenu;
import cn.zsk.sys.entity.SysRoleMenu;
import cn.zsk.sys.service.MenuService;
import cn.zsk.sys.service.RoleMenuService;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zsk
 * @date 2017/12/13.
 *
 * 菜单
 */
@RequestMapping("/menu")
@Controller
public class MenuController extends BaseController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleMenuService roleMenuService;

    /**
     * 展示tree
     *
     * @param model
     * @return
     */
    @ApiOperation(value = "/showMenu", httpMethod = "GET", notes = "展示菜单")
    @GetMapping(value = "showMenu")
    @RequiresPermissions("menu:show")
    public String showMenu(Model model) {
        JSONArray ja = menuService.getMenuJsonList();
        model.addAttribute("menus", ja.toJSONString());
        return "/system/menu/menuList";
    }

    @GetMapping(value = "showAddMenu")
    public String addMenu(Model model) {
        JSONArray ja = menuService.getMenuJsonList();
        model.addAttribute("menus", ja.toJSONString());
        return "/system/menu/add-menu";
    }

    @Log(desc = "添加菜单", type = LOG_TYPE.UPDATE)
    @ApiOperation(value = "/addMenu", httpMethod = "POST", notes = "添加菜单")
    @PostMapping(value = "addMenu")
    @ResponseBody
    public JsonUtil addMenu(SysMenu sysMenu, Model model) {
        JsonUtil jsonUtil = new JsonUtil();
        jsonUtil.setFlag(false);
        if (sysMenu == null) {
            jsonUtil.setMsg("获取数据失败");
            return jsonUtil;
        }
        if (StringUtils.isEmpty(sysMenu.getPId())) {
            sysMenu.setPId(null);
        }
        if (StringUtils.isEmpty(sysMenu.getUrl())) {
            sysMenu.setUrl(null);
        }
        if (StringUtils.isEmpty(sysMenu.getPermission())) {
            sysMenu.setPermission(null);
        }

        try {
            if (sysMenu.getMenuType() == 2) {
                sysMenu.setMenuType((byte) 0);
            }
            menuService.insertSelective(sysMenu);
            jsonUtil.setMsg("添加成功");
        } catch (MyException e) {
            e.printStackTrace();
            jsonUtil.setMsg("添加失败");
        }
        /*
        * 删除用户权限信息，使用户重新加载权限信息，为了触发权限分配方法中的有关菜单项的操作，实现菜单数据的刷新
        * */
        ShiroUtil shiroUtil = new ShiroUtil();
        shiroUtil.clearAuth();
        return jsonUtil;
    }

    @GetMapping(value = "showUpdateMenu")
    public String showUpdateMenu(Model model, String id) {
        SysMenu sysMenu = menuService.selectByPrimaryKey(id);
        JSONArray ja = menuService.getMenuJsonList();
        model.addAttribute("menus", ja.toJSONString());
        model.addAttribute("sysMenu", sysMenu);
        if (null != sysMenu.getPId()) {
            SysMenu pSysMenu = menuService.selectByPrimaryKey(sysMenu.getPId());
            model.addAttribute("pName", pSysMenu.getName());
        }
        return "/system/menu/update-menu";
    }


    @Log(desc = "更新菜单", type = LOG_TYPE.ADD)
    @PostMapping(value = "updateMenu")
    @ResponseBody
    public JsonUtil updateMenu(SysMenu sysMenu) {
        SysMenu oldMenu = menuService.selectByPrimaryKey(sysMenu.getId());
        BeanUtil.copyNotNullBean(sysMenu, oldMenu);
        menuService.updateByPrimaryKeySelective(oldMenu);
        /*
         * 删除用户权限信息，使用户重新加载权限信息，为了触发权限分配方法中的有关菜单项的操作，实现菜单数据的刷新
         * */
        ShiroUtil shiroUtil = new ShiroUtil();
        shiroUtil.clearAuth();
        return JsonUtil.sucess("保存成功");
    }

    @Log(desc = "删除菜单", type = LOG_TYPE.DEL)
    @PostMapping("/menu-del")
    @ResponseBody
    public JsonUtil del(String id) {
        JsonUtil json = new JsonUtil();
        json.setFlag(false);
        if (StringUtils.isEmpty(id)) {
            json.setMsg("获取数据失败,请刷新重试!");
            return json;
        }
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        sysRoleMenu.setMenuId(id);
        int count = roleMenuService.selectCount(sysRoleMenu);
        //存在角色绑定不能删除
        if (count > 0) {
            json.setMsg("本菜单存在绑定角色,请先解除绑定!");
            return json;
        }
        //存在下级菜单 不能解除
        SysMenu sysMenu = new SysMenu();
        sysMenu.setPId(id);
        if (menuService.selectCount(sysMenu) > 0) {
            json.setMsg("存在子菜单,请先删除子菜单!");
            return json;
        }
        boolean isDel = menuService.deleteByPrimaryKey(id) > 0;
        if (isDel) {
            json.setMsg("删除成功");
            json.setFlag(true);
        } else {
            json.setMsg("删除失败");
        }
        /*
         * 删除用户权限信息，使用户重新加载权限信息，为了触发权限分配方法中的有关菜单项的操作，实现菜单数据的刷新
         * */
        ShiroUtil shiroUtil = new ShiroUtil();
        shiroUtil.clearAuth();
        return json;

    }

}
