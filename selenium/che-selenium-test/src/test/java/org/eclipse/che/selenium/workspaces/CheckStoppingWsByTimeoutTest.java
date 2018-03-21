package org.eclipse.che.selenium.workspaces;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Named;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.*;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.testng.AssertJUnit.assertEquals;

public class CheckStoppingWsByTimeoutTest {

  private static int TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC = 20000;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestUser testUser;

  @Inject
  @Named("che.workspace_agent_dev_inactive_stop_timeout_ms")
  private int stoppingTimeoutInactiveWorkspace;

  @Inject
  @Named("che.workspace.activity_check_scheduler_period_s")
  private int cheWorkspaceActivityCheckSchedulerPeriodInSecond;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    // We should invoke delay without any action for triggering workspace activity checker
    sleepQuietly(getCommonTimeout(), MILLISECONDS);
  }

  @Test
  public void checkStoppingByApi() throws Exception {
    Workspace workspace =
        workspaceServiceClient.getByName(
            testWorkspace.getName(), testUser.getName(), testUser.getAuthToken());
    assertEquals(workspace.getStatus(), STOPPED);
  }

  @Test
  public void checkLoadToasterAfterStopping() {
    toastLoader.waitStartButtonInToastLoader();
  }

  private int getCommonTimeout() {
    return stoppingTimeoutInactiveWorkspace
        + TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC
        + cheWorkspaceActivityCheckSchedulerPeriodInSecond * 1000;
  }
}
