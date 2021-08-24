package com.hh.hhdb_admin.mgr.db_task;

import com.hh.frame.dbtask.*;
import com.hh.frame.dbtask.sql_file_imp.SqlFileImpTool;
import com.hh.frame.swingui.view.container.HBarPanel;
import com.hh.frame.swingui.view.container.HDialog;
import com.hh.frame.swingui.view.container.HPanel;
import com.hh.frame.swingui.view.container.LastPanel;
import com.hh.frame.swingui.view.ctrl.HButton;
import com.hh.frame.swingui.view.textEditor.HTextArea;
import com.hh.hhdb_admin.common.icon.IconFileUtil;
import com.hh.hhdb_admin.common.util.StartUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class TaskUtil {
    private static final Map<String, String> taskMap = new LinkedHashMap<>();
    private static final HDialog dig = new HDialog(StartUtil.parentFrame, 800, 600);
    private static DbTask task = null;
    private static final HTextArea hArea = new HTextArea(false, false);

    static {
        taskMap.put(TaskType.SQL_FILE.name(), "添加SQL文件");
        taskMap.put(TaskType.CLEAN.name(), "清除数据库/模式");
        taskMap.put(TaskType.RESTORE.name(), "恢复数据库/模式");
        taskMap.put(TaskType.BACKUP.name(), "备份数据库/模式");
        taskMap.put(TaskType.GEN_TAB_DATA.name(), "生成表格数据");
        taskMap.put(TaskType.EXP_QUERY_AS_XLS.name(), "导出查询数据");
        taskMap.put(TaskType.GEN_TEST_DATA.name(), "生成测试数据");
        HPanel rootPanel = new HPanel();
        LastPanel lastPanel = new LastPanel();
        lastPanel.setWithScroll(hArea.getComp());
        rootPanel.setLastPanel(lastPanel);
        HBarPanel toolbar = new HBarPanel();
        HButton refreshBtn = new HButton("刷新") {
            public void onClick() {
                hArea.setText(task.getInfo());
            }
        };
        HButton closeBtn = new HButton("关闭") {
            public void onClick() {
                dig.hide();
                TaskComp.refresh();
            }
        };

        toolbar.add(refreshBtn, closeBtn);
        dig.setIconImage(IconFileUtil.getLogo());
        dig.setToolBar(toolbar);
        dig.setRootPanel(rootPanel);
    }

    public static void showTaskInfo(DbTask t) {
        task = t;
        hArea.setText(task.getInfo());
        dig.show();
    }

    public static Map<String, String> getTypeMap() {
        return taskMap;
    }

    public static String generateTaskName(TaskType type) {
        return type.name() + "_" + new Random().nextInt(1000);
    }

    public static TaskType getTypeLabel(DbTask t) {
        if (t instanceof SqlFileImpTool) {
            return TaskType.SQL_FILE;
        }
        if (t instanceof BackupTask) {
            return TaskType.BACKUP;
        }
        if (t instanceof RestoreTask) {
            return TaskType.RESTORE;
        }
        if (t instanceof CleanTask) {
            return TaskType.CLEAN;
        }
        if (t instanceof GenTabDataTask) {
            return TaskType.GEN_TAB_DATA;
        }
        if (t instanceof ExpQueryAsInsertTask || t instanceof ExpQueryAsCsvOrXlsTask) {
            return TaskType.EXP_QUERY_AS_XLS;
        }
        if (t instanceof GenTestDataTask) {
            return TaskType.GEN_TEST_DATA;
        }
        return null;
    }

}
