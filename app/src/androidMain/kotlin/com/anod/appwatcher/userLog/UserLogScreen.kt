package com.anod.appwatcher.userLog

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BackArrowIconButton
import com.anod.appwatcher.compose.ShareIconButton
import com.anod.appwatcher.preferences.AndroidPreferences
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.notification.NotificationManager
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLogScreen(screenState: UserLogState, onEvent: (UserLogEvent) -> Unit, prefs: Preferences = KoinJavaComponent.getKoin().get()) {
    AppTheme(
            theme = prefs.theme
    ) {
        Surface {
            Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                                title = { Text(text = stringResource(id = R.string.user_log)) },
                                navigationIcon = {
                                    BackArrowIconButton(onClick = { onEvent(UserLogEvent.OnBackNav) })
                                },
                                actions = {
                                    ShareIconButton(onClick = { onEvent(UserLogEvent.Share) })
                                },
                        )
                    }
            ) { contentPadding ->
                UserLogMessages(
                        messages = screenState.messages,
                        contentPadding = contentPadding
                )
            }
        }
    }
}

@Composable
fun UserLogMessages(
        messages: List<Message>,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            reverseLayout = true
    ) {
        items(messages.size) { idx ->
            UserLogMessageItem(position = idx, size = messages.size, message = messages[idx])
        }
    }
}

@Composable
fun UserLogMessageItem(position: Int, size: Int, message: Message) {
    val fontSize = with(LocalDensity.current) { 12.dp.toSp() }
    val textColor = if (message.level > Log.WARN) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        Text(
                text = "${size - position}",
                fontSize = fontSize,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(36.dp)
                    .padding(start = 4.dp, end = 4.dp)
        )
        Text(
                text = "${message.timestamp} ${message.message}",
                fontSize = fontSize,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
        )
    }
}

@Preview
@Composable
fun UserLogScreenPreview() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    UserLogScreen(
            screenState = UserLogState(
                    messages =
                    """
10-05 22:43:10.892 V/DeviceStatisticsService( 2919): chargerType=1 batteryLevel=100 totalBatteryCapacity=4654800
10-05 22:43:10.893 D/DeviceInfoHidlClient( 2919): isRadioOn()=true
10-05 22:43:10.893 I/DeviceInfoHidlClient( 2919): isPowerInfoNeverSent=false batteryLevel=100 batteryLevelIndex=3 chargingMode=1 totalCapacity=4654800 powerSaveMode=false
10-05 22:43:10.893 D/KeyguardUpdateMonitor( 2562): handleBatteryUpdate
10-05 22:43:10.898 D/KeyguardUpdateMonitor( 2562): isUdfpsEnrolled:  = true
10-05 22:43:10.898 D/KeyguardUpdateMonitor( 2562): isUdfpsEnrolled:  = true
10-05 22:43:10.899 D/QSAnimator( 2562): updateAnimators: update normally
10-05 22:43:10.899 D/QSAnimator( 2562): updateAnimators: update normally
10-05 22:43:28.479 D/ReactionClub(19084): StepsService run
10-05 22:43:28.483 D/CompatibilityInfo( 1575): mCompatibilityFlags - 0
10-05 22:43:28.483 D/CompatibilityInfo( 1575): applicationDensity - 420
10-05 22:43:28.484 D/CompatibilityInfo( 1575): applicationScale - 1.0
10-05 22:43:28.500 D/SensorManager(19084): listenerName:com.reactnativepedometer.StepCounterRecord,delayUs:0,addus:20000
10-05 22:43:44.442 W/AppOpsControllerImpl( 2562): Noted op: 24 with result default for package com.alibaba.aliexpresshd
10-05 22:43:44.450 W/AppOpsControllerImpl( 2562): Noted op: 24 with result default for package com.alibaba.aliexpresshd
10-05 22:43:48.050 D/WireGuard/GoBackend/SurfsharkWireguardTunnel(22616): peer(kEAQ…26zY) - Sending keepalive packet
10-05 22:43:58.492 D/ReactionClub(19084): StepsService run
10-05 22:43:58.496 D/CompatibilityInfo( 1575): mCompatibilityFlags - 0
10-05 22:43:58.496 D/CompatibilityInfo( 1575): applicationDensity - 420
10-05 22:43:58.496 D/CompatibilityInfo( 1575): applicationScale - 1.0
10-05 22:43:58.513 D/SensorManager(19084): listenerName:com.reactnativepedometer.StepCounterRecord,delayUs:0,addus:20000
10-05 22:44:07.861 W/TelephonyPermissions( 2932): reportAccessDeniedToReadIdentifiers:com.alibaba.aliexpresshd:getSubscriberIdForSubscriber:1
10-05 22:44:09.134 D/KeyguardUpdateMonitor( 2562): received broadcast android.intent.action.BATTERY_CHANGED
10-05 22:44:09.134 D/KeyguardUpdateMonitor( 2562): received broadcast ACTION_BATTERY_CHANGED:, time=1664999049134
10-05 22:44:09.136 I/BatteryController( 2562): updateBatteryStateExt: BatteryStateExt{status=2, temperature=280, voltage=4486}
10-05 22:44:09.137 D/DataSyncManager( 2982): onReceive: level = 100 batteryStatus = 2
10-05 22:44:09.138 D/QtiCarrierConfigHelper( 2861): WARNING, no carrier configs on phone Id: 1
10-05 22:44:09.141 D/KeyguardUpdateMonitor( 2562): handleBatteryUpdate
                            """.trimIndent().split("\n").map { UserLogMessage.from(it) }
            ),
            onEvent = {},
            prefs = AndroidPreferences(context, NotificationManager.NoOp(), scope)
    )
}