package com.example.progettoembedded

import android.R.attr.name
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class RealTimeFragment : Fragment() {
    /**
     * TextView showing Longitude. We are keeping this as variable in order to prevent to ask the UI for this textView
     * everytime we want to update the UI
     */
    private lateinit var tvLong : TextView

    /**
     * TextView showing Latitude. We are keeping this as variable in order to prevent to ask the UI for this textView
     * everytime we want to update the UI
     */
    private lateinit var tvLat : TextView

    /**
     * TextView showing Altitude. We are keeping this as variable in order to prevent to ask the UI for this textView
     * everytime we want to update the UI
     */
    private lateinit var tvAlt : TextView

    private lateinit var tvDis : TextView
    private lateinit var tvDis2 : TextView
    private lateinit var tvDis3 : TextView

    private lateinit var tvTreeCard1_dist : MutableList<TextView>
    private lateinit var tvTreeCard1_bear : MutableList<TextView>
    private lateinit var tvTreeCard1_spec : MutableList<TextView>
    private lateinit var tvTreeCard1_local : MutableList<TextView>
    private lateinit var tvTreeCard1_public : MutableList<TextView>
    private lateinit var tvTreeCard1_link : MutableList<TextView>
    private lateinit var tvTreeCard1_image : MutableList<ImageView>
    private lateinit var tvTreeCard1_compass : MutableList<ImageView>



//    private lateinit var tvTreeCard2_dist : TextView
//    private lateinit var tvTreeCard2_bear : TextView
//    private lateinit var tvTreeCard2_spec : TextView
//    private lateinit var tvTreeCard2_link : TextView
//    private lateinit var tvTreeCard2_image : ImageView
//
//    private lateinit var tvTreeCard3_dist : TextView
//    private lateinit var tvTreeCard3_bear : TextView
//    private lateinit var tvTreeCard3_spec : TextView
//    private lateinit var tvTreeCard3_link : TextView
//    private lateinit var tvTreeCard3_image : ImageView

    /**
     * It tells if we should center the camera of the map every time a position is retrieved. If the user has moved the map, we want to keep
     * the settings made to the map by the user themselves instead of re-centering
     */
    private var moveCamera = true

    /**
     * Reference to the map shown on the screen
     */
    private lateinit var map : GoogleMap

    /**
     * ActivityViewModel shared with Activity and Fragment
     */
    private val model: ActivityViewModel by activityViewModels()

    /**
     * True if the map has already been initialized, false otherwise
     */
    private var initialized = false

    /**
     * Variable used to display just one toast at once.
     * When we the user clicks on the "Center" button multiple times, this would cause many toasts to queue up. Therefore,
     * we are saving the current toast and if the user generates another toast, we cancel the current toast and display a new one.
     * This way, there will always be one toast in the queue
     */
    private var toast: Toast? = null

    private val receiverData : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ReceiverSample","ReceivedData")

            //If the activity is bound to the user, we update the ui
            if(model.mBound) {
                updateUI()
            }
        }
    }

    /**
     * Updates the Interface: So both the value displayed on the cards and on the map
     */
    private fun updateUI()
    {
        if(!model.mBound)
            return

        val sample = model.readerService!!.currentSample
        //Update the 3 cards shown at the top of the screen
        updateCards(sample)
//        //If latitude and longitude are valid
//        if (sample.latitude != null && sample.longitude != null) {
//            //if the map has been initialized
//            if(initialized) {
//                //Insert the marker with the current position of the user
//                insertMarker()
//                //Center the camera to where the marker is placed if we should do so (the user has not moved the map camera)
//                if (moveCamera) {
//                    moveCameraToCurrentPosition()
//                }
//            }
//        }
//        else if(initialized){
//            //if a valid position is not available, we will not show anything on the map. The information about NoData available
//            //is handled already by the function updateCards()
//            map.clear()
//        }
    }

    /**
     * Inserts a marker in the current position if the app is collecting locations.
     *
     */
    private fun insertMarker(){
        if(model.mBound && model.readerService!!.isCollectingLocation && initialized) {

            val sample = model.readerService!!.currentSample
            //Latitude and longitude cannot be null if the app is collecting location. There is no need to check
            val pos = LatLng(sample.latitude!!.toDouble(), sample.longitude!!.toDouble())

            //Creating the icon object to show as marker
            var bitmap =
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_marker3)!!.toBitmap()
            bitmap = Bitmap.createScaledBitmap(bitmap, 130, 130, false)
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)

            //Creating the marker
            val marker = MarkerOptions()
                .position(pos)
                .title(getString(R.string.you_are_here))
                .snippet("Lat:" + pos.latitude.toString() + ", Lng:" + pos.longitude.toString())
                .icon(icon)
            //Clear the marker previously positioned
            map.clear()
            //Add the new marker to the map
            map.addMarker(marker)
        }
    }

//    private val callback = OnMapReadyCallback { googleMap ->
//
//        /**
//         * Manipulates the map once available.
//         * This callback is triggered when the map is ready to be used.
//         * This is where we can add markers or lines, add listeners or move the camera.
//         * In this case, we just add a marker near Sydney, Australia.
//         * If Google Play services is not installed on the device, the user will be prompted to
//         * install it inside the SupportMapFragment. This method will only be triggered once the
//         * user has installed Google Play services and returned to the app.
//         */
//
//        map = googleMap
//
//        //Disabling zoom +/- controls at the bottom right-hand side of the screen
//        map.uiSettings.isZoomControlsEnabled = false
//        //The user cannot tilt the map
//        map.uiSettings.isTiltGesturesEnabled = false
//
//        map.setOnCameraMoveStartedListener {
//            //If the camera has been moved by the user with a gesture, we have to stop to recenter the map every time a new location
//            //is available
//            if (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//                Log.d("moveCamera", "Camera moving")
//                moveCamera = false
//            }
//        }
//
//        //insert the marker in the current position
//        insertMarker()
//
//        //The map has been initialized
//        initialized = true
//
//        updateUI()
//    }

    /**
     * When the fragments is in the foreground and receives the input from the user we subscribe for updates from the service again and
     * we update the UI if possible.
     *
     */
    override fun onResume() {
        super.onResume()

        //Using LocalBroadcastManager instead of simple BroadcastManager. This has several advantages:
        // - You know that the data you are broadcasting won't leave your app, so don't need to worry about leaking private data.
        // - It is not possible for other applications to send these broadcasts to your app, so you don't need to worry about having security holes they can exploit.
        // - It is more efficient than sending a global broadcast through the system.
        //we are subscribing for updates about the latest sample
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiverData, IntentFilter(ReaderService.ACTION_NEW_SAMPLE))

        //We want to update the UI but move the camera to current position without any animation (animate set to false)
        updateUI()
    }

    /**
     * Moves the camera to the last retrieved position in the readerService
     */
    private fun moveCameraToCurrentPosition(){
        val sample = model.readerService!!.currentSample
        if(sample.latitude != null && sample.longitude != null) {
            val pos = LatLng(sample.latitude.toDouble(), sample.longitude.toDouble())
            val update = CameraUpdateFactory.newLatLngZoom(pos, 15f)
            //We cannot use animateCamera() as this would cause many messages I/Counters: exceeded sample count in FrameTime
            //in the debug log
            map.moveCamera(update)
        }
    }

    /**
     * UnRegisters the receiver when the fragment is not visible, there is no need to keep receiving the updates if we do not need to update the
     * interface.
     */
    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiverData)
        //remove pending broadcasts, they must not be handled when the receiver is restarted
        receiverData.abortBroadcast
    }


    /**
     * Creates the view the first time the fragment is created.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState : Bundle?): View?{
        //Inflating layout from the resources
        val view = inflater.inflate(R.layout.fragment_real_time, container,false)

//        //Requires the map asynchronously (operation done in the main thread)
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(callback)


//        //Setting up cards programmatically
//        val listLabel : List<String> = listOf(getString(R.string.label_distance),getString(R.string.label_distance2),getString(R.string.label_distance3))
//        val listImg : List<Int> = listOf(R.drawable.ic_altitude,R.drawable.ic_altitude,R.drawable.ic_altitude)
//        val list : List<FrameLayout> = listOf(view.findViewById(R.id.distance),view.findViewById(R.id.distance2),view.findViewById(R.id.distance3))
//        //Iterate through the frameLayouts and inserts the labels, icons, etc.
//        //With a loop the initialization is smoother and easier to understand
//        for ((index, e) in list.withIndex()) {
//            val tvLabel = e.findViewById<TextView>(R.id.label)
//            tvLabel.text = listLabel[index]
//
//            val imgView = e.findViewById<ImageView>(R.id.imageView)
//            //Requiring Drawable for better performance
//            val myImage: Drawable? = ResourcesCompat.getDrawable(requireContext().resources, listImg[index], null)
//            imgView.setImageDrawable(myImage)
//        }
//
//        //Gets the textViews
////        tvLong = list[0].findViewById(R.id.value)
////        tvLat = list[1].findViewById(R.id.value)
////        tvAlt = list[2].findViewById(R.id.value)
//        tvDis = list[0].findViewById(R.id.value)
//        tvDis2 = list[1].findViewById(R.id.value)
//        tvDis3 = list[2].findViewById(R.id.value)

        val list_treeCard_views : List<FrameLayout> = listOf(view.findViewById(R.id.tree_card1),
            view.findViewById(R.id.tree_card2), view.findViewById(R.id.tree_card3),
            view.findViewById(R.id.tree_card4), view.findViewById(R.id.tree_card5))
        tvTreeCard1_dist = mutableListOf()
        tvTreeCard1_bear = mutableListOf()
        tvTreeCard1_spec = mutableListOf()
        tvTreeCard1_local = mutableListOf()
        tvTreeCard1_public = mutableListOf()
        tvTreeCard1_link = mutableListOf()
        tvTreeCard1_image = mutableListOf()
        tvTreeCard1_compass = mutableListOf()

        for (i in 0 until 5) {
            tvTreeCard1_dist.add(list_treeCard_views[i].findViewById(R.id.textView4))
            tvTreeCard1_bear.add(list_treeCard_views[i].findViewById(R.id.textView5))
            tvTreeCard1_spec.add(list_treeCard_views[i].findViewById(R.id.textView6))
            tvTreeCard1_local.add(list_treeCard_views[i].findViewById(R.id.textView7))
            tvTreeCard1_public.add(list_treeCard_views[i].findViewById(R.id.textView8))
            tvTreeCard1_link.add(list_treeCard_views[i].findViewById(R.id.textView9))
            tvTreeCard1_image.add(list_treeCard_views[i].findViewById(R.id.imageView1))
            tvTreeCard1_compass.add(list_treeCard_views[i].findViewById(R.id.imageView2))

        }



//
//
//        val list_treecard2 : List<FrameLayout> = listOf(view.findViewById(R.id.tree_card2))
//        tvTreeCard2_dist = list_treecard2[0].findViewById(R.id.textView4)
//        tvTreeCard2_bear = list_treecard2[0].findViewById(R.id.textView5)
//        tvTreeCard2_spec = list_treecard2[0].findViewById(R.id.textView6)
//        tvTreeCard2_link = list_treecard2[0].findViewById(R.id.textView7)
//        tvTreeCard2_image = list_treecard2[0].findViewById(R.id.imageView1)
//
//        val list_treecard3 : List<FrameLayout> = listOf(view.findViewById(R.id.tree_card3))
//        tvTreeCard3_dist = list_treecard3[0].findViewById(R.id.textView4)
//        tvTreeCard3_bear = list_treecard3[0].findViewById(R.id.textView5)
//        tvTreeCard3_spec = list_treecard3[0].findViewById(R.id.textView6)
//        tvTreeCard3_link = list_treecard3[0].findViewById(R.id.textView7)
//        tvTreeCard3_image = list_treecard3[0].findViewById(R.id.imageView1)



//        //Button to center the map
//        val button = view.findViewById<Button>(R.id.center_button)
//        button.setOnClickListener {
//            if(model.mBound && model.readerService!!.isCollectingLocation){
//                if(initialized)
//                    //If the map was initialized and the service is bound, then move to the current position
//                    moveCameraToCurrentPosition()
//            }
//            else {
//                //The following code prevents from many toasts to be queued when the user clicks several times on the button center
//                //when the map is not ready (for example the GPS is deactivated or we are just waiting for the map to load).
//
//                //Cancel the previous toast from the queue
//                toast?.cancel()
//
//                //Create a new toast
//                toast = Toast.makeText(
//                    requireContext(),
//                    getString(R.string.last_location_missing),
//                    Toast.LENGTH_SHORT
//                )
//
//                //Show the new toast
//                toast?.show()
//            }
//
//            moveCamera = true
//        }

        return view
    }


    data class GPSLocation(val latitude: Double, val longitude: Double, val species: String,
                           val localName: String, val veteranStatus: String, val publicAccessibilityStatus: String,
                            val TNSI: String, val heritageTree: String, val TotY: String, val championTree: String,
                           val treeID: String
                           )



    private fun calculateDistance(location1: GPSLocation, location2: GPSLocation): Double {
        val earthRadius = 6371 // Radius of the earth in kilometers
        val latDifference = Math.toRadians(location2.latitude - location1.latitude)
        val lonDifference = Math.toRadians(location2.longitude - location1.longitude)
        val a = sin(latDifference / 2) * sin(latDifference / 2) +
                cos(Math.toRadians(location1.latitude)) * cos(Math.toRadians(location2.latitude)) *
                sin(lonDifference / 2) * sin(lonDifference / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun findClosestLocations(referenceLocation: GPSLocation, allLocations: List<GPSLocation>, count: Int): List<GPSLocation> {
        val sortedLocations = allLocations.sortedBy { calculateDistance(referenceLocation, it) }
        return sortedLocations.subList(0, minOf(count, sortedLocations.size))
    }

    private fun readGPSLocationsFromAssets(context: Context, fileName: String): List<GPSLocation> {
        val gpsLocations = mutableListOf<GPSLocation>()

        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val fileContent = String(buffer, Charsets.UTF_8)
            val lines = fileContent.split("\n")

            for (line in lines) {
                val parts = line.split(",")
//                if (parts.size == 3) {
                    val latitude = parts[0].toDouble()
                    val longitude = parts[1].toDouble()
                    val species = parts[2].toString()
                    val localName = parts[3].toString()
                    val veteranStatus = parts[4].toString()
                    val publicAccessibilityStatus = parts[5].toString()
                    val TNSI = parts[6].toString()
                    val heritageTree = parts[7].toString()
                    val TotY = parts[8].toString()
                    val championTree = parts[9].toString()
                    val treeID = parts[10].toString()
                    val gpsLocation = GPSLocation(latitude, longitude, species,
                        localName, veteranStatus, publicAccessibilityStatus, TNSI,
                        heritageTree, TotY, championTree, treeID)
                    gpsLocations.add(gpsLocation)
//                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return gpsLocations
    }

    private fun calculateBearing(location1: Location, location2: Location): Float {
        return location1.bearingTo(location2)
    }

    private fun roundBearingToNearestTenDegrees(bearing: Double): Double {
        val degrees = bearing % 360.0
        val remainder = degrees % 10.0

        return when {
            remainder < 5.0 -> degrees - remainder
            else -> degrees + (10.0 - remainder)
        }
    }


    /**
     * Update the cards showing the last positions after an update has been sent by the service.
     *
     * @param location location details to update the cards with. If any of the 3 components is null, that means that it was impossible to retrieve
     * the location. Therefore, we show that No data is available.
     */
    private fun updateCards(location : LocationDetails)
    {
        if (location.longitude != null && location.latitude != null && location.altitude != null) {
            //Keeping always 7 decimal digits. Without this conversion a number such as 7.2, would be printed as 7.2 instead of
                //7.2000000 (better to show the same number of digits for every number, it is also nicer to see by the user themselves)
//            tvLong.text = String.format("%.7f", location.longitude.toDouble())
//            tvLat.text = String.format("%.7f", location.latitude.toDouble())
//            tvAlt.text = String.format("%.7f", location.altitude.toDouble())


//            val current_latitude = location.latitude.toDouble()
//            val current_longitude = location.longitude.toDouble()
            val current_latitude = 51.3781
            val current_longitude = -2.3597

            val referenceLocation = GPSLocation(current_latitude, current_longitude, "None", "None", "None",
                "None","None","None","None","None","None") // Example reference location (San Francisco)
            val fileName = "trees.txt"
            val gpsLocations = readGPSLocationsFromAssets(requireContext(), fileName)

            val closestLocations = findClosestLocations(referenceLocation, gpsLocations, 5)

            var location1 = Location("provider")
            location1.latitude = current_latitude
            location1.longitude = current_longitude

            var location2 = Location("provider")
            var results = FloatArray(1)

            for (i in 0 until 5) {
                location2.latitude = closestLocations[i].latitude
                location2.longitude = closestLocations[i].longitude
                android.location.Location.distanceBetween(
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    location2.latitude,
                    location2.longitude,
                    results
                )

                val bearing = calculateBearing(location1, location2)
                var bearing_Double = roundBearingToNearestTenDegrees(bearing.toDouble())
                var bearing_Double_file_name = bearing_Double
                if (bearing_Double < 0) {
                    bearing_Double_file_name = bearing_Double_file_name + 360
                }
                val bearing_Double_file_name_int = bearing_Double_file_name.toInt()
                val compass_resourceId = resources.getIdentifier(String.format("deg_"+bearing_Double_file_name_int.toString()), "drawable", requireContext().packageName)
                val myImage: Drawable? = ResourcesCompat.getDrawable(
                    requireContext().resources,
                    compass_resourceId, null
                )
                tvTreeCard1_compass[i].setImageDrawable(myImage)





                val dist = results[0].toDouble()

                val tree_species = closestLocations[i].species

                if (dist > 1000) {
                    val new_dist = dist / 1000
                    tvTreeCard1_dist[i].text = Html.fromHtml("<b>Distance: " + String.format("</b>%.1f km", new_dist))
                } else {
                    tvTreeCard1_dist[i].text = Html.fromHtml("<b>Distance: " + String.format("</b>%.1f meters", dist))

                }

                tvTreeCard1_bear[i].text = Html.fromHtml("<b>Bearing: " + String.format("</b>N%.1fºE", bearing_Double))
                tvTreeCard1_spec[i].text = Html.fromHtml("<b>Species: " + String.format("</b>%s", tree_species))


                tvTreeCard1_local[i].text = Html.fromHtml("<b>Local Name: " + String.format("</b>%s", closestLocations[i].localName))

                tvTreeCard1_public[i].text = Html.fromHtml("<b>Access: " + String.format("</b>%s", closestLocations[i].publicAccessibilityStatus))

                if (closestLocations[i].TNSI != "False" || closestLocations[i].heritageTree != "False" || closestLocations[i].TotY != "False" || closestLocations[i].championTree != "False") {
//                if (tree_species == "Scots pine") {
                    val myImage: Drawable? = ResourcesCompat.getDrawable(
                        requireContext().resources,
                        R.drawable.star, null
                    )
                    tvTreeCard1_image[i].setImageDrawable(myImage)
                } else {
                    val myImage: Drawable? = ResourcesCompat.getDrawable(
                        requireContext().resources,
                        R.drawable.empty, null
                    )
                    tvTreeCard1_image[i].setImageDrawable(myImage)
                }

                val link =
                    "<a href='https://ati.woodlandtrust.org.uk/tree-search/tree?treeid=" + closestLocations[i].treeID.dropLast(
                        2
                    ) + "'> Woodland Trust Link </a>"
                val google_link =
                    "<a href='https://www.google.com/maps/search/?api=1&query=" + location2.latitude + "%2C" + location2.longitude + "'> Google Maps Link </a>"

                tvTreeCard1_link[i].setClickable(true)
                tvTreeCard1_link[i].setMovementMethod(LinkMovementMethod.getInstance());
                tvTreeCard1_link[i].setText(Html.fromHtml(link + " - " + google_link))
            }







//
//            val referenceLocation = GPSLocation(location.latitude.toDouble(), location.longitude.toDouble(), "None", "None", "None",
//                "None","None","None","None","None","None") // Example reference location (San Francisco)
//            val fileName = "trees.txt"
//            val gpsLocations = readGPSLocationsFromAssets(requireContext(), fileName)
//            val closestLocations = findClosestLocations(referenceLocation, gpsLocations, 3)
//
//            var tree_lat = closestLocations[0].latitude
//            var tree_long = closestLocations[0].longitude
//            var tree_species = closestLocations[0].species
//            var results = FloatArray(1)
//            android.location.Location.distanceBetween(location.latitude.toDouble(), location.longitude.toDouble(), tree_lat, tree_long, results)
//
//            var location1 = Location("provider")
//            location1.latitude = location.latitude.toDouble()
//            location1.longitude = location.longitude.toDouble()
//
//            var location2 = Location("provider")
//            location2.latitude = closestLocations[0].latitude
//            location2.longitude = closestLocations[0].longitude
//
//            var bearing = calculateBearing(location1, location2)
//            var dist = results[0].toDouble()
//            if (dist > 1000) {
//                val new_dist = dist/1000
//                tvDis.text = String.format("%.1f km", new_dist)
//            } else {
//                tvDis.text = String.format("%.1f meters", dist)
//            }
//            tvDis2.text = String.format("%.1f degrees east of north", bearing.toDouble())
//            tvDis3.text = String.format("%s", tree_species)
//
//
//
//            ///////////////////////////////
//            if (dist > 1000) {
//                val new_dist = dist/1000
//                tvTreeCard1_dist.text = String.format("%.1f km", new_dist)
//            } else {
//                tvTreeCard1_dist.text = String.format("%.1f meters", dist)
//            }
//            tvTreeCard1_bear.text = String.format("%.1f degrees east of north", bearing.toDouble())
//            tvTreeCard1_spec.text = String.format("%s", tree_species)
//
//
//            if (closestLocations[0].TNSI != "False" || closestLocations[0].heritageTree != "False" || closestLocations[0].TotY != "False" || closestLocations[0].championTree != "False") {
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.star, null
//                )
//                tvTreeCard1_image.setImageDrawable(myImage)
//            }
//            else{
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.empty, null
//                )
//                tvTreeCard1_image.setImageDrawable(myImage)
//            }
//
//
//            var link = "<a href='https://ati.woodlandtrust.org.uk/tree-search/tree?treeid="+closestLocations[0].treeID.dropLast(2)+"'> Woodland Trust Link </a>"
//            tvTreeCard1_link.setClickable(true)
//            tvTreeCard1_link.setMovementMethod(LinkMovementMethod.getInstance());
//            tvTreeCard1_link.setText(Html.fromHtml(link))
//
//            ///////////////////////////////
//            location2.latitude = closestLocations[1].latitude
//            location2.longitude = closestLocations[1].longitude
//            results = FloatArray(1)
//            android.location.Location.distanceBetween(location.latitude.toDouble(), location.longitude.toDouble(), location2.latitude, location2.longitude, results)
//
//            bearing = calculateBearing(location1, location2)
//            dist = results[0].toDouble()
//
//            tree_species = closestLocations[1].species
//
//            if (dist > 1000) {
//                val new_dist = dist/1000
//                tvTreeCard2_dist.text = String.format("%.1f km", new_dist)
//            } else {
//                tvTreeCard2_dist.text = String.format("%.1f meters", dist)
//            }
//            tvTreeCard2_bear.text = String.format("%.1f degrees east of north", bearing.toDouble())
//            tvTreeCard2_spec.text = String.format("%s", tree_species)
//
//
//            if (closestLocations[1].TNSI != "False" || closestLocations[1].heritageTree != "False" || closestLocations[1].TotY != "False" || closestLocations[1].championTree != "False") {
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.star, null
//                )
//                tvTreeCard2_image.setImageDrawable(myImage)
//            }
//            else{
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.empty, null
//                )
//                tvTreeCard2_image.setImageDrawable(myImage)
//            }
//
//            link = "<a href='https://ati.woodlandtrust.org.uk/tree-search/tree?treeid="+closestLocations[1].treeID.dropLast(2)+"'> Woodland Trust Link </a>"
//            tvTreeCard2_link.setClickable(true)
//            tvTreeCard2_link.setMovementMethod(LinkMovementMethod.getInstance());
//            tvTreeCard2_link.setText(Html.fromHtml(link))
//
//            ///////////////////////////////
//
//
//
//            location2.latitude = closestLocations[2].latitude
//            location2.longitude = closestLocations[2].longitude
//            results = FloatArray(1)
//            android.location.Location.distanceBetween(location.latitude.toDouble(), location.longitude.toDouble(), location2.latitude, location2.longitude, results)
//
//            bearing = calculateBearing(location1, location2)
//            dist = results[0].toDouble()
//
//            tree_species = closestLocations[2].species
//
//            if (dist > 1000) {
//                val new_dist = dist/1000
//                tvTreeCard3_dist.text = String.format("%.1f km", new_dist)
//            } else {
//                tvTreeCard3_dist.text = String.format("%.1f meters", dist)
//            }
//            tvTreeCard3_bear.text = String.format("%.1f degrees east of north", bearing.toDouble())
//            tvTreeCard3_spec.text = String.format("%s", tree_species)
//
//            if (closestLocations[2].TNSI != "False" || closestLocations[2].heritageTree != "False" || closestLocations[2].TotY != "False" || closestLocations[2].championTree != "False") {
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.star, null
//                )
//                tvTreeCard3_image.setImageDrawable(myImage)
//            }
//            else{
//                val myImage: Drawable? = ResourcesCompat.getDrawable(
//                    requireContext().resources,
//                    R.drawable.empty, null
//                )
//                tvTreeCard3_image.setImageDrawable(myImage)
//            }
//
//            link = "<a href='https://ati.woodlandtrust.org.uk/tree-search/tree?treeid="+closestLocations[2].treeID.dropLast(2)+"'> Woodland Trust Link </a>"
//            tvTreeCard3_link.setClickable(true)
//            tvTreeCard3_link.setMovementMethod(LinkMovementMethod.getInstance());
//            tvTreeCard3_link.setText(Html.fromHtml(link))
//
//            ///////////////////////////////





            Log.d("Second", location.timestamp.seconds.toString())
        } else {
//            tvAlt.text = resources.getString(R.string.label_nodata)
//            tvLat.text = resources.getString(R.string.label_nodata)
//            tvLong.text = resources.getString(R.string.label_nodata)
//            tvDis.text = resources.getString(R.string.label_nodata)
//            tvDis2.text = resources.getString(R.string.label_nodata)
//            tvDis3.text = resources.getString(R.string.label_nodata)

//            tvTreeCard1_dist.text = resources.getString(R.string.label_nodata)
//            tvTreeCard1_bear.text = resources.getString(R.string.label_nodata)
//            tvTreeCard1_spec.text = resources.getString(R.string.label_nodata)
//
//            tvTreeCard2_dist.text = resources.getString(R.string.label_nodata)
//            tvTreeCard2_bear.text = resources.getString(R.string.label_nodata)
//            tvTreeCard2_spec.text = resources.getString(R.string.label_nodata)
//
//            tvTreeCard3_dist.text = resources.getString(R.string.label_nodata)
//            tvTreeCard3_bear.text = resources.getString(R.string.label_nodata)
//            tvTreeCard3_spec.text = resources.getString(R.string.label_nodata)

            Log.d("Second", "No data")
        }
    }
}

