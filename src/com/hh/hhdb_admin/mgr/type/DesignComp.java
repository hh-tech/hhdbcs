package com.hh.hhdb_admin.mgr.type;

import com.hh.frame.common.base.AlignEnum;
import com.hh.frame.common.base.DBTypeEnum;
import com.hh.frame.common.base.JdbcBean;
import com.hh.frame.common.util.DriverUtil;
import com.hh.frame.common.util.db.SqlExeUtil;
import com.hh.frame.common.util.db.SqlQueryUtil;
import com.hh.frame.create_dbobj.treeMr.base.TreeMrType;
import com.hh.frame.dbobj2.base.AbsSessionObj;
import com.hh.frame.dbobj2.kword.KeyWordUtil;
import com.hh.frame.dbobj2.ora.OraSessionEnum;
import com.hh.frame.dbobj2.ora.OraSessionObj;
import com.hh.frame.json.JsonArray;
import com.hh.frame.json.JsonObject;
import com.hh.frame.swingui.engine.GuiJsonUtil;
import com.hh.frame.swingui.view.container.HBarPanel;
import com.hh.frame.swingui.view.container.HPanel;
import com.hh.frame.swingui.view.container.HSplitPanel;
import com.hh.frame.swingui.view.container.LastPanel;
import com.hh.frame.swingui.view.ctrl.HButton;
import com.hh.frame.swingui.view.hmenu.HMenuItem;
import com.hh.frame.swingui.view.hmenu.HPopMenu;
import com.hh.frame.swingui.view.input.TextAreaInput;
import com.hh.frame.swingui.view.layout.bar.HBarLayout;
import com.hh.frame.swingui.view.tree.HTree;
import com.hh.frame.swingui.view.tree.HTreeNode;
import com.hh.frame.swingui.view.util.PopPaneUtil;
import com.hh.hhdb_admin.CsMgrEnum;
import com.hh.hhdb_admin.common.util.StartUtil;
import com.hh.hhdb_admin.common.util.textEditor.QueryEditUtil;
import com.hh.hhdb_admin.common.util.textEditor.QueryEditorTextArea;
import com.hh.hhdb_admin.common.util.textEditor.tooltip.Tooltip;
import com.hh.hhdb_admin.mgr.attribute.AttributeComp;
import com.hh.hhdb_admin.mgr.login.LoginComp;
import com.hh.hhdb_admin.mgr.login.LoginUtil;
import com.hh.hhdb_admin.mgr.query.util.QuerUtil;
import com.hh.hhdb_admin.mgr.tree.TreeMgr;
import org.apache.commons.lang3.StringUtils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YuSai
 */
public class DesignComp extends CommonComp {

    private final Connection conn;
    private final String schema;
    private final String name;
    private final OraSessionEnum sessionEnum;

    private final HPanel panel;
    private HTree tree;
    private HTreeNode rootNode;
    private QueryEditorTextArea textArea;
    //提示工具
    private Tooltip tip;
    private final TextAreaInput consoleArea;
    private final HSplitPanel portraitPanel;
    private HButton execBtn;
    private String oldText;

    public DesignComp(Connection conn, String schema, String name, OraSessionEnum sessionEnum) throws Exception {
        this.conn = conn;
        this.schema = schema;
        this.name = name;
        this.sessionEnum = sessionEnum;
        HSplitPanel horizontalPanel = new HSplitPanel();
        horizontalPanel.setSplitWeight(0.2);
        horizontalPanel.setLastComp4One(getTreePanel());
        horizontalPanel.setLastComp4Two(getTextPanel());

        consoleArea = new TextAreaInput();
        consoleArea.setEnabled(false);
        LastPanel consoleLast = new LastPanel();
        consoleLast.set(consoleArea.getComp());
        portraitPanel = new HSplitPanel(false);
        portraitPanel.getComp().setResizeWeight(1);
        portraitPanel.setPanelOne(horizontalPanel);
        portraitPanel.setLastComp4Two(consoleLast);
        LastPanel lastPanel = new LastPanel();
        lastPanel.set(portraitPanel.getComp());
        lastPanel.setHead(getBarPanel().getComp());
        panel = new HPanel();
        panel.setLastPanel(lastPanel);
        refreshTree();
    }

    public HPanel getPanel() throws Exception {
        return panel;
    }

    protected void setText(String text) {
        textArea.setText(text);
        refreshTree();
    }

    private void refreshTree() {
        rootNode.removeAllChildren();
        tree.addHTreeNode(rootNode, getTreeNodes(), true);
    }

    private List<HTreeNode> getTreeNodes() {
        List<HTreeNode> treeNodes = new ArrayList<>();
        if (StringUtils.isNotBlank(textArea.getText())) {
//            parserTool = new PackParserTool(textArea.getText());
//            treeNodes.addAll(queryTreeNode(parserTool.getTypeNames(), "type"));
//            treeNodes.addAll(queryTreeNode(parserTool.getFunNames(), "function"));
//            treeNodes.addAll(queryTreeNode(parserTool.getProcNames(), "procedure"));
        }
        return treeNodes;
    }

    private HBarPanel getBarPanel() {
        HBarLayout barLayout = new HBarLayout();
        barLayout.setAlign(AlignEnum.LEFT);
        HBarPanel barPanel = new HBarPanel(barLayout);
        execBtn = new HButton(getLang("execute")) {
            @Override
            protected void onClick() {
                execute();
            }
        };
        execBtn.setEnabled(false);
        execBtn.setIcon(getIcon("execute"));
        HButton formatBtn = new HButton(getLang("format")) {
            @Override
            protected void onClick() {
                format();
            }
        };
        formatBtn.setIcon(getIcon("format"));
        HButton saveBtn = new HButton(getLang("saveToSqlBook")) {
            @Override
            protected void onClick() {
                saveToSqlBook(textArea.getText(), OraSessionEnum.type.equals(sessionEnum) ? "tps" : "tpb");
            }
        };
        saveBtn.setIcon(getIcon("save"));
        barPanel.add(execBtn, formatBtn, saveBtn);
        return barPanel;
    }

    protected boolean execute() {
        boolean result = true;
        DBTypeEnum dbTypeEnum = null;
        try {
            portraitPanel.setDividerLocation(600);
            String sql = textArea.getText();
            oldText = sql;
            SqlExeUtil.executeUpdate(conn, sql);
            dbTypeEnum = DriverUtil.getDbType(conn);
            String errorMsg = getErrorMsg(sessionEnum);
            if (StringUtils.isEmpty(errorMsg)) {
                consoleArea.setValue(getLang("saveSuccess"));
                refreshTree();
                if (execBtn != null) {
                    execBtn.setEnabled(false);
                }
                StartUtil.eng.doPush(CsMgrEnum.TREE, GuiJsonUtil.toJsonCmd(TreeMgr.CMD_REFRESH).add(TreeMgr.PARAM_NODE_TYPE, TreeMrType.PACKAGE_GROUP.name()));
            } else {
                consoleArea.setValue(getLang("compileFailed") + "\n" + errorMsg);
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (DBTypeEnum.oracle.equals(dbTypeEnum)) {
                PopPaneUtil.error(StartUtil.parentFrame.getWindow(), e);
            } else {
                consoleArea.setValue(getLang("failed") + "\n" + e.getMessage());
            }
            result = false;
        }
        return result;
    }

    protected void format() {
        try {
            QuerUtil.formatSql(DriverUtil.getDbType(conn),textArea);
            rootNode.removeAllChildren();
            tree.addHTreeNode(rootNode, getTreeNodes(), true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getErrorMsg(OraSessionEnum oraSessionEnum) throws Exception {
        String pType = oraSessionEnum.getDbType();
//        String packageName = parserTool.getName();
        String sql = "select line, type, text from SYS.USER_ERRORS where type = '%s' and name = '%s' order by type, line";
        List<Map<String, Object>> maps = SqlQueryUtil.select(conn, String.format(sql, pType, LoginUtil.getRealName(name, DBTypeEnum.oracle.name())));
        StringBuilder errorText = new StringBuilder();
        for (Map<String, Object> map : maps) {
            String line = "0".equals((map.get("line") + "")) ? "1" : (map.get("line") + "");
            errorText.append(map.get("type")).append("  ").append(line).append("  ")
                    .append((map.get("text") + "").replace("\n", "")).append("\n");
        }
        return errorText.toString();
    }

    private LastPanel getTreePanel() {
        rootNode = new HTreeNode();
        rootNode.setName("Declaration");
        rootNode.setType("root");
        rootNode.setOpenIcon(getIcon("type"));
        tree = new HTree(rootNode) {
            @Override
            protected void selectTreeNode(HTreeNode treeNode) {
//                if (!"root".equals(treeNode.getType())) {
//                    int startLine;
//                    int endLine;
//                    int index = Integer.parseInt(treeNode.getId());
//                    if ("type".equals(treeNode.getType())) {
//                        startLine = parserTool.getTypePosList().get(index).getBeginLine();
//                        endLine = parserTool.getTypePosList().get(index).getEndLine();
//                    } else if ("function".equals(treeNode.getType())) {
//                        startLine = parserTool.getFunPosList().get(index).getBeginLine();
//                        endLine = parserTool.getFunPosList().get(index).getEndLine();
//                    } else {
//                        startLine = parserTool.getProcPosList().get(index).getBeginLine();
//                        endLine = parserTool.getProcPosList().get(index).getEndLine();
//                    }
//                    searchContext(textArea, treeNode);
//                    heightLight(textArea, startLine, endLine);
//                }
            }

            @Override
            protected void rightClickTreeNode(HTreeNode treeNode, MouseEvent e) {
                if ("root".equals(treeNode.getType())) {
                    HPopMenu hp = new HPopMenu();
                    HMenuItem refreshMenu = new HMenuItem(getLang("refresh")) {
                        @Override
                        protected void onAction() {
                            treeNode.removeAllChildren();
                            tree.addHTreeNode(treeNode, getTreeNodes(), true);
                        }
                    };
                    refreshMenu.setIcon(getIcon("refresh"));
                    hp.addItem(refreshMenu);
                    hp.showPopup(e);
                }
            }
        };
        LastPanel lastPanel = new LastPanel();
        lastPanel.setWithScroll(tree.getComp());
        return lastPanel;
    }

    private LastPanel getTextPanel() throws Exception {
        textArea = QueryEditUtil.getQueryEditor(true);
        tip = QueryEditUtil.getQueryTooltip(textArea.getTextArea(),textArea.type);
        textArea.getTextArea().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (execBtn != null && oldText != null) {
                    execBtn.setEnabled(!oldText.equals(textArea.getText()));
                }
            }
        });
        HMenuItem attrMenuItem = new HMenuItem(getLang("attribute")) {
            @Override
            protected void onAction() {
                try {
                    List<Map<String, String>> maps = queryObject();
                    if (maps.size() > 0) {
                        Map<String, String> map = maps.get(0);
                        JsonObject data = new JsonObject();
                        data.add("tableName", map.get("object_name"));
                        data.add("schemaName", map.get("owner"));
                        data.add("name", map.get("object_name"));
                        data.add("nodeType", map.get("object_type"));
                        JdbcBean jdbcBean = LoginComp.loginBean.getJdbc();
                        jdbcBean.setSchema(map.get("owner"));
                        new AttributeComp().showAttr(data, jdbcBean, conn);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        };
        textArea.getTextArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getModifiers() == InputEvent.META_MASK) {
                    String selectText = textArea.getSelectedText();
                    if (selectText != null) {
                        if (queryObject().size() > 0) {
                            textArea.getTextArea().getPopupMenu().add(attrMenuItem.getComp());
                        } else {
                            textArea.getTextArea().getPopupMenu().remove(attrMenuItem.getComp());
                        }
                    }
                }
            }
        });
        textArea.getTextArea().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textArea.getTextArea().getHighlighter().removeAllHighlights();
            }
        });
        textArea.setText(queryText());
        oldText = textArea.getText();
        LastPanel lastPanel = new LastPanel();
        lastPanel.set(textArea.getComp());
        setKeyWord();
        return lastPanel;
    }

    private List<Map<String, String>> queryObject() {
        String objName = textArea.getSelectedText().trim();
        String currSchema = schema;
        if (objName.contains(".")) {
            currSchema = objName.split("\\.")[0];
            objName = objName.split("\\.")[1];
        }
        if (!StringUtils.startsWith(currSchema, "\"") || !StringUtils.endsWith(currSchema, "\"")) {
            currSchema = currSchema.toUpperCase();
        } else {
            currSchema = currSchema.replace("\"", "");
        }
        if (!StringUtils.startsWith(objName, "\"") || !StringUtils.endsWith(objName, "\"")) {
            objName = objName.toUpperCase();
        } else {
            objName = objName.replace("\"", "");
        }
        String sql = "select owner, object_type, object_name\n" +
                "from all_objects\n" +
                "where object_type in ('TABLE', 'VIEW', 'TYPE')\n" +
                "AND status = 'VALID'\n" +
                "AND owner = '%s'\n" +
                "AND object_name = '%s'\n";
        try {
            return SqlQueryUtil.selectStrMapList(conn, String.format(sql, currSchema, objName));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ArrayList<>();
    }

    private String queryText() {
        String text = "";
        try {
            AbsSessionObj  sessionObj = new OraSessionObj(conn, schema, sessionEnum, name);
            text = sessionObj.getCreateSql();
            if ("create or replace ".equals(text) || StringUtils.isEmpty(text)) {
                if (OraSessionEnum.type.equals(sessionEnum)) {
                    text = String.format("create or replace type %s.%s as object (\n\n);", schema, name);
                } else {
                    text = String.format("create or replace type body %s.%s is \n\nend;", schema, name);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return text;
    }

    private JsonArray jsonValues;

    private void setKeyWord() throws Exception {
        //强制关闭之前查询的连接，防止表过多查询过久的情况
        jsonValues = new JsonArray();
        //先设置常用的关键字
        jsonValues = KeyWordUtil.getKeyWordJson(conn);
        tip.setkeyword(jsonValues);
        new Thread(() -> {
            try {
                KeyWordUtil.getDbObjectJson(jsonValues, conn, schema, "table"); //先设置表提示
                tip.setkeyword(jsonValues);
                KeyWordUtil.getDbObjectJson(jsonValues, conn, schema, "view");
                tip.setkeyword(jsonValues);
                KeyWordUtil.getDbObjectJson(jsonValues, conn, schema, "function");
                tip.setkeyword(jsonValues);
                KeyWordUtil.getDbObjectJson(jsonValues, conn, schema, "synonym");
                tip.setkeyword(jsonValues);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                jsonValues = new JsonArray();
            }
        }).start();
    }

}
