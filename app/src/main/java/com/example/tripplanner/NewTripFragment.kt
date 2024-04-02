package com.example.tripplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class NewTripFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_trip, container, false)
        setupSeekBar(view)
        return view
    }

    private fun setupSeekBar(view: View) {
        val seekBar = view.findViewById<SeekBar>(R.id.priceRangeSeekBar)
        val textViewCurrentPrice = view.findViewById<TextView>(R.id.textViewCurrentPrice)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Assume the max price is 1000 and the SeekBar max is set accordingly
                val maxPrice = 1000
                val currentPrice = progress * maxPrice / seekBar.max
                textViewCurrentPrice.text = "$$currentPrice"

                // Adjust the position of the textViewCurrentPrice based on the thumb position
                val thumbPosX = (seekBar.width - seekBar.paddingLeft - seekBar.paddingRight) * progress / seekBar.max
                textViewCurrentPrice.x = seekBar.x + thumbPosX + seekBar.paddingLeft - textViewCurrentPrice.width / 2.0f
            }



            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Handle interaction start
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Handle interaction end
            }
        })
    }



    /*

    companion object {
        fun newInstance(param1: String, param2: String) =
            NewTripFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
     */
}
