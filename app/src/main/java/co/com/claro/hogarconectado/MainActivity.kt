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
            println("Sample print from button click!")
            ThingDeviceActivatorManager.startDeviceActiveAction(context as MainActivity)

            ThingDeviceActivatorManager.addListener(object : IThingDeviceActiveListener {
                override fun onDevicesAdd(list: List<String>) {
                    Log.d("BizBundleChannelHandler", "onDevicesAdd: ${list.size} devices added")
                }

                override fun onRoomDataUpdate() {
                    Log.d("BizBundleChannelHandler", "onRoomDataUpdate: Room data updated")
                }

                override fun onOpenDevicePanel(deviceId: String) {
                    Log.d("BizBundleChannelHandler", "onOpenDevicePanel: Opening panel for device $deviceId")
                }
            })


        }) {
            Text("Click Me")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HogarConectadoTheme {
        Greeting("Android")
    }
}