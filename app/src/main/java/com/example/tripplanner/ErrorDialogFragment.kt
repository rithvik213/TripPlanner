package com.example.tripplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.error_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.error_message).text = arguments?.getString("errorMessage")

        view.findViewById<Button>(R.id.go_back_button).setOnClickListener {
            dismiss()
            activity?.onBackPressed()
        }
    }
    companion object {
        fun newInstance(errorMessage: String): ErrorDialogFragment {
            val fragment = ErrorDialogFragment()
            val args = Bundle()
            args.putString("errorMessage", errorMessage)
            fragment.arguments = args
            return fragment
        }
    }
}