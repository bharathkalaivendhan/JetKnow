package com.reignscanary.jetknow.composables


import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.reignscanary.jetknow.MainScreenViewModel
import com.reignscanary.jetknow.R
import com.reignscanary.jetknow.listOfLatLng
import java.util.HashMap

lateinit var marker :Marker
lateinit var   serviceLocator : Marker
@Composable
fun CustomMapView(
    DEFAULT_LOCATION: LatLng,
    savedInstanceState: Bundle?,
    modifier: Modifier = Modifier
)
{
 val darkTheme : Boolean = isSystemInDarkTheme()
      val mainViewModel : MainScreenViewModel= viewModel()
    val context  = LocalContext.current
    val cameraPosition = CameraPosition.Builder()
        .target(DEFAULT_LOCATION) // Sets the center of the map to Mountain View
        .zoom(17f)            // Sets the zoom
        .bearing(90f)         // Sets the orientation of the camera to east
        .tilt(45f)            // Sets the tilt of the camera to 30 degrees
        .build()              // Creates a CameraPosition from the builder
    val openDialog  by mainViewModel.dialogStatus.observeAsState(initial = false)
    val infoDialog  by mainViewModel.infoDialog.observeAsState(initial = false)
    val contributeLatLng by mainViewModel.contributeLatLng.observeAsState(LatLng(0.0,0.0))
    val mapView = MapView(context)



    //Composing mapView using AndroidView()

    AndroidView(
        modifier = modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(0.8f),
        factory = { mapView.apply {

            this.onCreate(savedInstanceState)
            this.getMapAsync{
                //setting a darker mapview using styling

                  marker =
           it.addMarker(
               MarkerOptions().
               position(DEFAULT_LOCATION)
                   .title("Location Of you")
           )
                serviceLocator = it.addMarker(

                    MarkerOptions().position(LatLng(0.0,0.0)).title("My Office ;)").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                )
                if(darkTheme)
                {  it.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.style_json))}
                else{
                    it.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        context, R.raw.style_json_light))

                }

                //The gestures and zoom features are enabled here
                it.uiSettings.setAllGesturesEnabled(true)
                it.animateCamera(CameraUpdateFactory.zoomIn())
                it.animateCamera(CameraUpdateFactory.zoomTo(10f), 2000, null)
                 it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    it.setMaxZoomPreference(17f)
                it.setMinZoomPreference(5f)



               it.setOnMapLongClickListener {
                   latlng ->
                 mainViewModel.onDialogStatusChanged(true)
                  mainViewModel.onNewContributeLatLng(latlng) //  Toast.makeText(context,"$latlng",Toast.LENGTH_SHORT).show()


               }


            }

            // this helps in loading the map faster on app startup
            this.onResume()
        }




        },
        update = {
            //When the location changes like when clicking the Fab,the new location is updated in the map
            it.getMapAsync{
                it.clear()
                it.apply {
                 marker.remove()
                animateCamera(CameraUpdateFactory.zoomIn())
                animateCamera(CameraUpdateFactory.zoomTo(10f), 2000, null)
               animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
 marker = addMarker(

     MarkerOptions().
     position(DEFAULT_LOCATION)
         .title("Location Of you")
 )
                }

                for(serviceLocation in listOfLatLng){
                    serviceLocator =
                        it.addMarker(
                            MarkerOptions()
                                .position(serviceLocation.key)
                                .title(serviceLocation.value)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        )
                    it.setOnMarkerClickListener{

                        mainViewModel.onInfoDialogStatusChanged(true)
                        true

                    }

                }



            }






        }
    )

    if(infoDialog){

        InfoPopup(mainViewModel)
    }

    if(openDialog) {

        AlertDialogComponent(context = context,mainViewModel,contributeLatLng)

    }



}

