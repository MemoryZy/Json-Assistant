package cn.memoryzy.json.extension;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.util.NlsActions;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Memory
 * @since 2024/9/19
 */
public class ErrorReporter extends ErrorReportSubmitter {

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {

        return "Report to Memory";
    }

    /**
     * 执行提交逻辑
     *
     * @return true，报告程序可用；false，报告程序不可用
     */
    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {


        return true;
    }
}
