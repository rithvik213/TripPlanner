package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverPage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_discover_page, container, false)

        val destinationsrecyclerView: RecyclerView = view.findViewById(R.id.recyclerviewdestinations)
        val attractionsrecyclerView: RecyclerView = view.findViewById(R.id.nearbydestinationsrecycler)

        // Set LinearLayoutManager with horizontal orientation
        val attractionsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val destinationslayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        destinationsrecyclerView.layoutManager = destinationslayoutManager
        attractionsrecyclerView.layoutManager = attractionsLayoutManager


        val destinations = getDestinations()
        val attractions = getAttractions()
        destinationsrecyclerView.adapter = PopularDestinationsAdapter(destinations)
        attractionsrecyclerView.adapter = NearbyAttractionsAdapter(attractions)
        return view
    }

    private fun getAttractions(): List<Attraction> {
        // TO DO
        return listOf(
            Attraction(
                title = "Attraction",
                imageUrl = null
            ),
            Attraction(
                title = "Birthday",
                imageUrl = null
            )

        )
    }


    private fun getDestinations(): List<Destination> {
        return listOf(
            Destination(
                title = "New York City",
                imageUrl = "https://images.unsplash.com/photo-1546436836-07a91091f160?q=80&w=2948&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Paris",
                imageUrl = "https://images.unsplash.com/photo-1522093007474-d86e9bf7ba6f?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Tokyo",
                imageUrl = "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "London",
                imageUrl = "https://images.unsplash.com/photo-1529655683826-aba9b3e77383?q=80&w=2865&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Sydney",
                imageUrl = "https://images.unsplash.com/photo-1523428096881-5bd79d043006?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Los Angeles",
                imageUrl = "https://images.unsplash.com/flagged/photo-1575555201693-7cd442b8023f?q=80&w=3000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Rome",
                imageUrl = "https://images.unsplash.com/photo-1491566102020-21838225c3c8?q=80&w=3000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Beijing",
                imageUrl = "https://images.unsplash.com/photo-1590301729964-23833732ee04?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Mumbai",
                imageUrl = "https://images.unsplash.com/photo-1570168007204-dfb528c6958f?q=80&w=2835&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )

        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DiscoverPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DiscoverPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}