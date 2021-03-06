package cn.zsk.sys.core.shiro;


import cn.zsk.core.base.CurrentUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * @author zsk
 * @date 2017/12/28.
 *
 */
public class ShiroUtil {

    public static Subject getSubject(){
        return SecurityUtils.getSubject();
    }

    public static Session getSession(){
        return getSubject().getSession();
    }
    public static CurrentUser getCurrentUse(){
        return (CurrentUser) getSession().getAttribute("curentUser");
    }

    /*清空权限信息*/
    public static void clearAuth(){
        RealmSecurityManager rsm = (RealmSecurityManager)SecurityUtils.getSecurityManager();
        LoginRealm realm = (LoginRealm)rsm.getRealms().iterator().next();
        realm.clearCachedAuthorization();
    }

}
