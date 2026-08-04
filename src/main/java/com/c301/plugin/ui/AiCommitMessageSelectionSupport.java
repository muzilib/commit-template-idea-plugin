package com.c301.plugin.ui;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;

/**
 * 直接更新 IDEA 提交消息编辑器，避免平台适配器写入后自动全选文本。
 */
final class AiCommitMessageSelectionSupport {


    private AiCommitMessageSelectionSupport() {
    }

    /**
     * 直接写入 Commit 面板的编辑器，避免 CommitProjectPanelAdapter 在写入后自动全选文本。
     */
    static void setCommitMessage(CommitMessageI commitMessage, String message) {
        EditorTextField editorField = findCommitMessageEditor(commitMessage);
        if (editorField != null) {
            if (commitMessage instanceof CheckinProjectPanel panel) {
                VcsConfiguration.getInstance(panel.getProject()).saveCommitMessage(editorField.getText());
            }
            editorField.setText(message);
            clearFullSelection(editorField);
            return;
        }

        commitMessage.setCommitMessage(message);
        SwingUtilities.invokeLater(() -> {
            EditorTextField fallbackEditorField = findCommitMessageEditor(commitMessage);
            if (fallbackEditorField != null) {
                clearFullSelection(fallbackEditorField);
            }
        });
    }

    private static EditorTextField findCommitMessageEditor(CommitMessageI commitMessage) {
        if (!(commitMessage instanceof CheckinProjectPanel panel)) {
            return null;
        }
        Component component = panel.getPreferredFocusedComponent();
        return component instanceof EditorTextField editorField ? editorField : null;
    }

    private static void clearFullSelection(EditorTextField editorField) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> clearFullSelection(editorField));
            return;
        }
        var editor = editorField.getEditor();
        if (editor == null) {
            return;
        }
        int textLength = editor.getDocument().getTextLength();
        var selection = editor.getSelectionModel();
        selection.removeSelection();
        editor.getCaretModel().moveToOffset(textLength);
    }
}
