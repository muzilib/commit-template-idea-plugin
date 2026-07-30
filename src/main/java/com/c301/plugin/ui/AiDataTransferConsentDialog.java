package com.c301.plugin.ui;

import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.domain.ai.AiDataTransferConsent;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * 首次传输代码差异前的明确授权。拒绝后立即关闭 AI，且不读取任何变更内容。
 */
public final class AiDataTransferConsentDialog {
    private AiDataTransferConsentDialog() {
    }

    public static boolean ensureAccepted(Project project) {
        AiPreferencesState preferences = AiPreferencesState.getInstance();
        if (preferences.getDataTransferConsent() == AiDataTransferConsent.ACCEPTED) {
            return true;
        }
        if (preferences.getDataTransferConsent() == AiDataTransferConsent.DECLINED) {
            return false;
        }
        var bundle = CommUtil.i18nResourceBundle(null);
        int result = Messages.showDialog(project, bundle.getString("plugin.ai.consent.message"),
                bundle.getString("plugin.ai.consent.title"),
                new String[]{bundle.getString("plugin.ai.consent.accept"), bundle.getString("plugin.ai.consent.decline")},
                0, Messages.getWarningIcon());
        if (result == 0) {
            preferences.setDataTransferConsent(AiDataTransferConsent.ACCEPTED);
            return true;
        }
        preferences.setDataTransferConsent(AiDataTransferConsent.DECLINED);
        preferences.setEnabled(false);
        return false;
    }
}
