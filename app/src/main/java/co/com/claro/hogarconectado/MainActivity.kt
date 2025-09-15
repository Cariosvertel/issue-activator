package co.com.claro.hogarconectado

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import co.com.claro.hogarconectado.ui.theme.HogarConectadoTheme
import com.thingclips.smart.activator.plug.mesosphere.ThingDeviceActivatorManager
import com.thingclips.smart.activator.plug.mesosphere.api.IThingDeviceActiveListener
import com.thingclips.smart.android.user.api.IUidLoginCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.api.service.MicroServiceManager
import com.thingclips.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HogarConectadoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(text = "Hello $name!")
        Button(onClick = {
            println("Did press Login button")
            ThingHomeSdk.getUserInstance().loginOrRegisterWithUid(
                "57",
                "1005089131",
                "1234",
                true,
                object : IUidLoginCallback {
                    override fun onSuccess(user: User, homeId: Long) {
                        Log.d("#db AuthChannelHandler", "login onSuccess user=${user.uid} homeId=$homeId")
                    }

                    override fun onError(code: String, error: String) {
                        Log.w("#db AuthChannelHandler", "login onError code=$code error=$error")
                    }
                })

        }) {
            Text("Login")
        }
        Button(onClick = {
            println("Did press get home list")
            ThingHomeSdk.getHomeManagerInstance().queryHomeList(object : IThingGetHomeListCallback {
                override fun onSuccess(homeBeans: List<HomeBean>?) {
                    Log.d("HomeChannelHandler", "getHomeList: Successfully retrieved ${homeBeans?.size ?: 0} homes")

                    // Log detailed information about each home
                    homeBeans?.forEachIndexed { index, homeBean ->
                        Log.d("HomeChannelHandler", "Home[$index] - ID: ${homeBean.homeId}, Name: '${homeBean.name}', GeoName: '${homeBean.geoName}', Lat: ${homeBean.lat}, Lon: ${homeBean.lon}, Rooms: ${homeBean.rooms?.size ?: 0}")
                    }
                    val homes = homeBeans ?: emptyList()
                    setFirstHomeAsDefaultIfNeeded(homes)
                }

                override fun onError(errorCode: String, errorMessage: String) {
                    Log.w("HomeChannelHandler", "getHomeList: SDK error code=$errorCode message=$errorMessage")
                }
            })


        }) {
            Text("get Homes")
        }
        Button(onClick = {
            println("Sample print from button click!")
            ThingDeviceActivatorManager.startDeviceActiveAction(context as MainActivity)

            ThingDeviceActivatorManager.addListener(object : IThingDeviceActiveListener {
                override fun onDevicesAdd(list: List<String>) {
                    Log.d("#db BizBundleChannelHandler", "onDevicesAdd: ${list.size} devices added")
                }

                override fun onRoomDataUpdate() {
                    Log.d("#db BizBundleChannelHandler", "onRoomDataUpdate: Room data updated")
                }

                override fun onOpenDevicePanel(deviceId: String) {
                    Log.d("#db BizBundleChannelHandler", "onOpenDevicePanel: Opening panel for device $deviceId")
                }
            })
        }) {
            Text("Open Activator")
        }
    }
}
private fun setFirstHomeAsDefaultIfNeeded(homes: List<HomeBean>) {
    if (homes.isEmpty()) {
        Log.d("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: No homes available")
        return
    }

    try {
        // Get BizBundleFamilyService instance using the registered service
        val familyService = MicroServiceManager.getInstance().findServiceByInterface(
            AbsBizBundleFamilyService::class.java.name) as? AbsBizBundleFamilyService

        if (familyService == null) {
            Log.e("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: BizBundleFamilyService not found")
            return
        }

        // Check if default home is already set (getCurrentHomeId > 0)
        val currentHomeId = familyService.getCurrentHomeId()
        Log.d("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: Current default home ID: $currentHomeId")

        if (currentHomeId > 0) {
            Log.d("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: Default home already set, skipping auto-set")
            return
        }

        // Set first home as default
        val firstHome = homes[0]
        val homeName = firstHome.name ?: "Home_${firstHome.homeId}"
        Log.d("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: Auto-setting first home as default: homeId=${firstHome.homeId}, name=$homeName")

        familyService.shiftCurrentFamily(firstHome.homeId, homeName)
        Log.d("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: Successfully auto-set first home as default")

    } catch (e: Exception) {
        Log.e("HomeChannelHandler", "setFirstHomeAsDefaultIfNeeded: Error auto-setting first home", e)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HogarConectadoTheme {
        Greeting("Android")
    }
}