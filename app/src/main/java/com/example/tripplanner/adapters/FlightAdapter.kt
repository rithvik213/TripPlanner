package com.example.tripplanner.adapters

import android.graphics.Color
import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.R
import com.example.tripplanner.apis.amadeus.data.FlightOffer
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
class FlightAdapter(private var flightInfoList: List<FlightOffer>) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    private var selectedItem = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flights, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flightInfo = flightInfoList[position]
        holder.bindFlightInfo(flightInfo, position == selectedItem)
    }

    override fun getItemCount(): Int {
        return flightInfoList.size
    }

    fun updateData(flightOffers: List<FlightOffer>) {
        flightInfoList = flightOffers
        notifyDataSetChanged()
    }

    fun getSelectedFlightInfo(): FlightOffer? {
        return if (selectedItem != RecyclerView.NO_POSITION) {
            flightInfoList[selectedItem]
        } else {
            null
        }
    }

    inner class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                selectedItem = position
                notifyDataSetChanged()
            }
        }
        fun bindFlightInfo(flightInfo: FlightOffer, isSelected: Boolean) {
            val departAirport = flightInfo.itineraries[0].segments[0].departure.iataCode
            val arrivalAirport = flightInfo.itineraries[0].segments[0].arrival.iataCode
            val departAirport2 = flightInfo.itineraries[1].segments[0].departure.iataCode
            val arrivalAirport2 = flightInfo.itineraries[1].segments[0].arrival.iataCode
            val price = flightInfo.price.total

            val departTime = dateFormat.parse(flightInfo.itineraries[0].segments[0].departure.dateTime)!!
            val arrivalTime = dateFormat.parse(flightInfo.itineraries[0].segments[0].arrival.dateTime)!!
            val departTime2 = dateFormat.parse(flightInfo.itineraries[1].segments[0].departure.dateTime)!!
            val arrivalTime2 = dateFormat.parse(flightInfo.itineraries[1].segments[0].arrival.dateTime)!!
            val departTerminal = flightInfo.itineraries[0].segments[0].departure.terminal ?: "N/A"
            val arrivalTerminal = flightInfo.itineraries[0].segments[0].arrival.terminal ?: "N/A"
            val departTerminal2 = flightInfo.itineraries[1].segments[0].departure.terminal ?: "N/A"
            val arrivalTerminal2 = flightInfo.itineraries[1].segments[0].arrival.terminal ?: "N/A"

            itemView.findViewById<TextView>(R.id.departingOutbound).text = departAirport
            itemView.findViewById<TextView>(R.id.departingInbound).text = arrivalAirport
            itemView.findViewById<TextView>(R.id.returningOutbound).text = departAirport2
            itemView.findViewById<TextView>(R.id.returningInbound).text = arrivalAirport2
            itemView.findViewById<TextView>(R.id.departingFlightDate).text = dateFormatter.format(departTime)
            itemView.findViewById<TextView>(R.id.returningFlightDate).text = dateFormatter.format(arrivalTime2)
            itemView.findViewById<TextView>(R.id.departingOutboundTime).text = timeFormatter.format(departTime)
            itemView.findViewById<TextView>(R.id.departingInboundTime).text = timeFormatter.format(arrivalTime)
            itemView.findViewById<TextView>(R.id.returningOutboundTime).text = timeFormatter.format(departTime2)
            itemView.findViewById<TextView>(R.id.returningInboundTime).text = timeFormatter.format(arrivalTime2)
            itemView.findViewById<TextView>(R.id.departingOutboundTer).text = "Terminal " + departTerminal
            itemView.findViewById<TextView>(R.id.departingInboundTer).text = "Terminal " + arrivalTerminal
            itemView.findViewById<TextView>(R.id.returningOutboundTer).text = "Terminal " + departTerminal2
            itemView.findViewById<TextView>(R.id.returningInboundTer).text = "Terminal " + arrivalTerminal2
            itemView.findViewById<TextView>(R.id.flightPrice).text = "$" + price

            loadAirlineLogo(flightInfo.itineraries[0].segments[0].carrierCode, itemView.findViewById(
                R.id.departingLogo
            ))
            loadAirlineLogo(flightInfo.itineraries[1].segments[0].carrierCode, itemView.findViewById(
                R.id.returningLogo
            ))

            if (isSelected) {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                itemView.setBackgroundColor(Color.BLACK)
            }
        }

        private fun loadAirlineLogo(airlineCode: String, imageView: ImageView) {
            val imageSize = "_350_100_r"
            val secret = "VDjfGgv8mxiTvvLLwGicD6V2eq"
            val apiKey = md5(airlineCode + imageSize + "_" + secret)
            val imageURL = "https://content.airhex.com/content/logos/airlines_$airlineCode$imageSize.png?md5apikey=$apiKey"

            Glide.with(itemView.context)
                .load(imageURL)
                .into(imageView)
        }

        private fun md5(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val hexString = StringBuilder()
            for (b in messageDigest) {
                hexString.append(String.format("%02x", b))
            }
            return hexString.toString()
        }

    }
}
