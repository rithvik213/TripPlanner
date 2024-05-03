package com.example.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide


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
        view?.findViewById<TextView>(R.id.attractionaddress)?.text = detail.address_obj.address_string
        view?.findViewById<TextView>(R.id.attractionwebsite)?.text = detail.website

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
