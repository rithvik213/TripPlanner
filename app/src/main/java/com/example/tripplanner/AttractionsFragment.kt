package com.example.tripplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager


class AttractionsFragment : Fragment() {
    private lateinit var viewModel: AttractionsViewModel
    private var locationId: String? = null
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AttractionsViewModel::class.java)
        Log.d("FragmentName", "Using ViewModel instance: $viewModel")
        arguments?.let {
            locationId = it.getString("locationID")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_attractions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationId?.let {
            fetchAttractionDetails(it)
        }

        backButton = view.findViewById<ImageButton>(R.id.backbutton)
        backButton.setOnClickListener {
            findNavController().popBackStack(R.id.discoverPageFragment, false)

        }

        val seeMoreButton = view.findViewById<Button>(R.id.seeMoreButton)
        val descriptionTextView = view.findViewById<TextView>(R.id.attractiondescription)
        seeMoreButton.setOnClickListener {
            toggleDescription(descriptionTextView, seeMoreButton)
        }

        val websiteButton = view.findViewById<ImageButton>(R.id.getWebsite)
        websiteButton.setOnClickListener {
            viewModel.attractionDetails.value?.website?.let { url ->
                if (URLUtil.isValidUrl(url)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val directionsButton = view.findViewById<ImageButton>(R.id.getDirections)
        directionsButton.setOnClickListener {
            viewModel.attractionDetails.value?.address_obj?.address_string?.let { address ->
                val uri = Uri.parse("http://maps.google.co.in/maps?q=${Uri.encode(address)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }


    }

    private fun fetchAttractionDetails(locationId: String) {
        // Use ViewModel to fetch attraction details
        viewModel.getCachedAttractionDetails(locationId)?.let {
            // If details are already cached, use them to update UI
            updateUI(it)
        } ?: run {
            // If not cached, fetch from API
            viewModel.fetchAttractionDetails(requireContext(),locationId)
            observeAttractionDetails()
        }
    }


    private fun toggleDescription(descriptionTextView: TextView, seeMoreButton: Button) {
        if (descriptionTextView.maxLines == Integer.MAX_VALUE) {
            descriptionTextView.maxLines = 4
            descriptionTextView.ellipsize = TextUtils.TruncateAt.END
            seeMoreButton.text = "See More"
        } else {
            descriptionTextView.maxLines = Integer.MAX_VALUE
            descriptionTextView.ellipsize = null
            seeMoreButton.text = "See Less"
        }
    }




    private fun observeAttractionDetails() {
        viewModel.attractionDetails.observe(viewLifecycleOwner) { details ->
            details?.let {
                updateUI(it)
            }
        }
    }

    private fun updateUI(detail: TripAdvisorManager.AttractionDetailsResponse) {
        view?.findViewById<TextView>(R.id.attractiontitle)?.text = detail.name
        view?.findViewById<TextView>(R.id.attractiondescription)?.text = detail.description ?: "No description available. Please see website for details!"

        // Use the image URL from the cached or newly fetched data
        val imageUrl = locationId?.let { viewModel.getAttractionImage(it) }
        val imageView = view?.findViewById<ImageView>(R.id.attractionimage)
        imageView?.let {
            Glide.with(this).load(imageUrl).into(it)
        }
    }

    companion object {
        private const val ARG_LOCATION_ID = "location_id"

        @JvmStatic
        fun newInstance(locationId: String): AttractionsFragment {
            return AttractionsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOCATION_ID, locationId)
                }
            }
        }
    }
}
