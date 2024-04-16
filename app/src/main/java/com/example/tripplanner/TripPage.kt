package com.example.tripplanner

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TripPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class TripPage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var imageView: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExcursionAdapter
    private lateinit var viewModel: ExcursionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trip_page, container, false)

        recyclerView = view.findViewById(R.id.excursionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ExcursionAdapter(mutableListOf())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity()).get(ExcursionsViewModel::class.java)  // Use requireActivity() for shared ViewModel across fragments
        viewModel.excursions.observe(viewLifecycleOwner) { excursions ->
            adapter.updateExcursions(ArrayList(excursions))  // Update adapter
        }

        imageView = view.findViewById(R.id.trippagepicture) // Bind ImageView
        fetchImage("Austin")  // Example city name
        return view    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TripPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TripPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun fetchImage(cityName: String){
        val tripAdvisorManager = TripAdvisorManager(
            requireContext(),
            cityName, null,
            object : TripAdvisorManager.ImageFetchListener {
                override fun onImageFetched(imageUrl: String) {
                    activity?.runOnUiThread {
                        // Use Glide to load the image
                        Glide.with(this@TripPage)
                            .load(imageUrl)
                            .into(imageView)
                        // Show the image URL in a toast
                        Log.d("TripPage", "Fetched image URL: $imageUrl")
                        Toast.makeText(context, "Image URL: $imageUrl", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onImageFetchFailed(errorMessage: String) {
                    // Handle failure
                }
            },
        )
        tripAdvisorManager.fetchData()

    }
}