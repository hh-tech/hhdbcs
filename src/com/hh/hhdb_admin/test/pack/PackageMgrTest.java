package com.hh.hhdb_admin.test.pack;

import com.hh.frame.common.base.DBTypeEnum;
import com.hh.frame.common.base.JdbcBean;
import com.hh.frame.common.util.ClassLoadUtil;
import com.hh.frame.common.util.DriverUtil;
import com.hh.frame.common.util.db.ConnUtil;
import com.hh.frame.json.Json;
import com.hh.frame.json.JsonObject;
import com.hh.frame.swingui.engine.GuiEngine;
import com.hh.frame.swingui.engine.GuiJsonUtil;
import com.hh.frame.swingui.view.ui.HHSwingUi;
import com.hh.frame.swingui.view.util.PopPaneUtil;
import com.hh.hhdb_admin.CsMgrEnum;
import com.hh.hhdb_admin.common.icon.IconFileUtil;
import com.hh.hhdb_admin.common.util.StartUtil;
import com.hh.hhdb_admin.mgr.login.LoginBean;
import com.hh.hhdb_admin.mgr.login.LoginComp;
import com.hh.hhdb_admin.mgr.login.LoginMgr;
import com.hh.hhdb_admin.mgr.login.LoginUtil;
import com.hh.hhdb_admin.test.MainTestMgr;
import com.hh.hhdb_admin.test.MgrTestUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author YuSai
 */
public class PackageMgrTest {

    public static void main(String[] args) throws Exception {
        //初始化自定义UI
        HHSwingUi.init();
        IconFileUtil.setIconBaseDir(new File("etc/icon/"));
        String jStr = ClassLoadUtil.loadTextRes(PackageMgrTest.class, "conf.json");
        JsonObject jObj = Json.parse(jStr).asObject();
        GuiEngine eng;
        StartUtil.eng = eng = new GuiEngine(CsMgrEnum.class, jObj);
        JdbcBean jdbcBean = MgrTestUtil.getJdbcBean();
        if (jdbcBean != null) {
            DBTypeEnum dbTypeEnum = DriverUtil.getDbType(jdbcBean);
            if (!DBTypeEnum.oracle.equals(dbTypeEnum)) {
                assert dbTypeEnum != null;
                PopPaneUtil.info(dbTypeEnum.name() + "--不支持该功能");
                return;
            }
            String name = StringUtils.isEmpty(jdbcBean.getSchema()) ? jdbcBean.getUser() : jdbcBean.getSchema();
            String schema = LoginUtil.getRealName(name, dbTypeEnum.name());
            jdbcBean.setUser(LoginUtil.getRealName(jdbcBean.getUser(), dbTypeEnum.name()));
            jdbcBean.setSchema(schema);
            LoginBean loginBean = new LoginBean();
            loginBean.setJdbc(jdbcBean);
            loginBean.setConn(ConnUtil.getConn(jdbcBean));
            LoginComp.loginBean = loginBean;
            LoginMgr.loginBeanId = StartUtil.eng.push2SharedMap(loginBean);
            eng.doPush(CsMgrEnum.MAIN_FRAME, GuiJsonUtil.toJsonCmd(MainTestMgr.CMD_SHOW));
        }
    }

}