package com.anushka.locationapp

import android.content.Context
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anushka.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel:LocationViewModel=viewModel()
            LocationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        MyApp(viewModel)
                    }
                }
            }
        }

    }
}

@Composable
fun MyApp(viewModel:LocationViewModel){
    val context=LocalContext.current
    val locationUtils=LocationUtils(context)

    LocationDisplay (locationUtils=locationUtils,viewModel,context=context)
}

@Composable
fun LocationDisplay(

    locationUtils:LocationUtils,
    viewModel: LocationViewModel,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()

    val location = viewModel.location.value
    var address by remember { mutableStateOf("")  }
    LaunchedEffect(location) {

        location?.let {
           address= locationUtils.reverseGeocodeLocation(it)

        }
    }

        //rememberLauncherForActivityResult function is used to register a request to start an activity
        //and handle the result,which is crucial for handling permissions in the app
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permission ->
                if (permission[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    && permission[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    //i have access to location
                    locationUtils.requestLocationUpdates(viewModel = viewModel)


                } else {
                    //ask for permission
                    val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (rationaleRequired) {
                        Toast.makeText(
                            context,
                            "Location Permission is required for this feature to work",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Location Permission is required . Please enable it in the android settings",
                            Toast.LENGTH_LONG
                        )
                            .show()

                    }
                }
            })


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (location != null) {
                Text("Latitude: ${location.latitude}")
                Text("Longitude: ${location.longitude}")
                Text("Address: $address")
            } else {
                Text("Location not available")
            }


            Button(onClick = {
                if (locationUtils.hasLocationPermission(context)) {
                    //permission already granted and update the location
                    locationUtils.requestLocationUpdates(viewModel)

                } else {
                    //Request location permission
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION

                        )
                    )
                }
            }) {
                Text(text = "Get Location")
            }
        }
    }
