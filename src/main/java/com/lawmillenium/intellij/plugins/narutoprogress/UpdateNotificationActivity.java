package com.lawmillenium.intellij.plugins.narutoprogress;

import java.util.Objects;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressConfigurable;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import icons.ShurikenIcons;
import org.jetbrains.annotations.NotNull;

public class UpdateNotificationActivity implements StartupActivity.DumbAware {
    private static final String PLUGIN_ID = "com.lawmillenium.narutoprogress";
    private static final String NOTIFICATION_GROUP = "Naruto Progress Update";

    public static IdeaPluginDescriptor getPluginDescriptor() {
        return PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));
    }

    @Override
    public void runActivity(@NotNull final Project project) {
        final IdeaPluginDescriptor descriptor = getPluginDescriptor();
        final NarutoProgressState state = NarutoProgressState.getInstance();
        if (descriptor != null && state != null) {
            final String version = descriptor.getVersion();
            if (!Objects.equals(version, state.version)) {
                state.version = descriptor.getVersion();
                if (state.showUpdateNotification) {
                    sendNotification(project, version);
                }
            }
        }
    }

    @SuppressWarnings("DialogTitleCapitalization")
    private static void sendNotification(final Project project, final String version) {
        String notificationMessage = "You're now using version " + version +
            " of <a href=\"https://github.com/lawmillenium/naruto-progress\">Naruto Progress</a>! \uD83C\uDF89";
        final Notification notification = NotificationGroupManager.getInstance() //
            .getNotificationGroup(NOTIFICATION_GROUP) //
            .createNotification(notificationMessage, NotificationType.INFORMATION);
        notification.setIcon(ShurikenIcons.SPINNING_SHURIKENS);
        notification.setListener(new NotificationListener.UrlOpeningListener(false));
        notification.addAction(new DumbAwareAction("Configuration...") {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, NarutoProgressConfigurable.class);
            }
        }).addAction(new DumbAwareAction("Changenotes") {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {

                new NarutoProgressChangenotesDialog(project).show();
            }
        }).addAction(new DumbAwareAction("Don't show again", "Disable this notification in the future", null) {
            @Override
            public void actionPerformed(@NotNull final AnActionEvent e) {
                NarutoProgressState.getInstance().showUpdateNotification = false;
            }
        }).notify(project);
    }
}
